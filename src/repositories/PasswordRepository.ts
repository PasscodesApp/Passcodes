import type { DrizzleDatabase } from "@/db/provider";
import { passwords } from "@/db/schema";
import { eq, inArray, isNull, sql } from "drizzle-orm";

/** SQLite limits the number of bound variables, so big batches are chunked. */
const BATCH_SIZE = 500;

/** Rows per transaction when importing. */
const IMPORT_CHUNK_SIZE = 200;

function chunk<T>(items: T[], size: number): T[][] {
  const chunks: T[][] = [];

  for (let i = 0; i < items.length; i += size) {
    chunks.push(items.slice(i, i + size));
  }

  return chunks;
}

export interface CreatePasswordInput {
  domain: string;
  username: string;
  password: string;
  notes?: string | null;
  url?: string | null;
  /** `null` / omitted = Uncategorized. */
  categoryId?: number | null;
}

/** One password to import. Timestamps are kept as given; `null` = now. */
export interface ImportPasswordRow {
  domain: string;
  username: string;
  password: string;
  notes: string | null;
  url: string | null;
  categoryId: number | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface ImportManyResult {
  /** Indexes (into the rows passed in) that were committed. */
  insertedIndexes: number[];
  /** Indexes that could not be written. */
  failedIndexes: number[];
}

export interface UpdatePasswordInput {
  domain?: string;
  username?: string;
  password?: string;
  notes?: string | null;
  url?: string | null;
  /** `null` = move to Uncategorized. */
  categoryId?: number | null;
}

export class PasswordRepository {
  constructor(private readonly db: DrizzleDatabase) {}

  /**
   * Creates a query for observing all passwords.
   *
   * This does not execute the query. Pass the returned query
   * to a reactive consumer such as `useLiveQuery`.
   */
  observeAll() {
    return this.db.select().from(passwords);
  }

  /**
   * Creates a query for observing the passwords of ONE category.
   * `null` = Uncategorized (`category_id IS NULL`).
   */
  observeByCategory(categoryId: number | null) {
    return this.db
      .select()
      .from(passwords)
      .where(
        categoryId === null
          ? isNull(passwords.categoryId)
          : eq(passwords.categoryId, categoryId),
      );
  }

  /**
   * Lightweight query for counting passwords per category.
   * Selects ONLY `category_id`, so no password secrets are loaded.
   */
  observeCategoryIds() {
    return this.db.select({ categoryId: passwords.categoryId }).from(passwords);
  }

  async getAll() {
    return this.observeAll();
  }

  /** Total number of stored passwords. */
  async count() {
    const [row] = await this.db
      .select({ count: sql<number>`count(*)` })
      .from(passwords);

    return Number(row?.count ?? 0);
  }

  async getById(id: number) {
    const [password] = await this.db
      .select()
      .from(passwords)
      .where(eq(passwords.id, id));

    return password;
  }

  async create(data: CreatePasswordInput) {
    const [createdPassword] = await this.db
      .insert(passwords)
      .values(data)
      .returning();

    return createdPassword;
  }

  /**
   * Inserts imported passwords, preserving their timestamps (`null` = use
   * the database default, i.e. now).
   *
   * Rows are written in chunks, one transaction per chunk. If a chunk fails
   * it is rolled back and retried row by row, so one bad row never costs the
   * others. The result reports what was REALLY committed, as indexes into
   * `rows`.
   */
  async importMany(rows: readonly ImportPasswordRow[]): Promise<ImportManyResult> {
    const insertedIndexes: number[] = [];
    const failedIndexes: number[] = [];

    const toValues = (row: ImportPasswordRow) => ({
      domain: row.domain,
      username: row.username,
      password: row.password,
      notes: row.notes,
      url: row.url,
      categoryId: row.categoryId,
      createdAt: row.createdAt ?? undefined,
      updatedAt: row.updatedAt ?? undefined,
    });

    for (let start = 0; start < rows.length; start += IMPORT_CHUNK_SIZE) {
      const slice = rows.slice(start, start + IMPORT_CHUNK_SIZE);

      try {
        this.db.transaction((tx) => {
          for (const row of slice) {
            tx.insert(passwords).values(toValues(row)).run();
          }
        });

        slice.forEach((_, offset) => insertedIndexes.push(start + offset));
      } catch {
        slice.forEach((row, offset) => {
          try {
            this.db.insert(passwords).values(toValues(row)).run();
            insertedIndexes.push(start + offset);
          } catch {
            failedIndexes.push(start + offset);
          }
        });
      }

      // The SQLite driver is synchronous: give the UI thread a turn.
      await new Promise<void>((resolve) => setTimeout(resolve, 0));
    }

    return { insertedIndexes, failedIndexes };
  }

  async update(id: number, data: UpdatePasswordInput) {
    const [updatedPassword] = await this.db
      .update(passwords)
      .set({
        ...data,
        updatedAt: sql`CURRENT_TIMESTAMP`, // TODO: It will no longer be required, once drizzle `$onUpdate()` will be used.
      })
      .where(eq(passwords.id, id))
      .returning();

    return updatedPassword;
  }

  async delete(id: number) {
    const [deletedPassword] = await this.db
      .delete(passwords)
      .where(eq(passwords.id, id))
      .returning();

    return deletedPassword;
  }

  /** Number of passwords in ONE category. */
  async countByCategory(categoryId: number) {
    const [row] = await this.db
      .select({ count: sql<number>`count(*)` })
      .from(passwords)
      .where(eq(passwords.categoryId, categoryId));

    return Number(row?.count ?? 0);
  }

  /**
   * Moves many passwords to a category (`null` = Uncategorized) in one
   * transaction. Only `category_id` changes, `updated_at` is left alone
   * because the password itself did not change.
   */
  async moveToCategory(ids: number[], categoryId: number | null) {
    if (ids.length === 0) {
      return;
    }

    this.db.transaction((tx) => {
      for (const idChunk of chunk(ids, BATCH_SIZE)) {
        tx.update(passwords)
          .set({ categoryId })
          .where(inArray(passwords.id, idChunk))
          .run();
      }
    });
  }

  /** Deletes many passwords in one transaction. */
  async deleteMany(ids: number[]) {
    if (ids.length === 0) {
      return;
    }

    this.db.transaction((tx) => {
      for (const idChunk of chunk(ids, BATCH_SIZE)) {
        tx.delete(passwords).where(inArray(passwords.id, idChunk)).run();
      }
    });
  }
}
