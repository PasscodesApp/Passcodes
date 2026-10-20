import type { DrizzleDatabase } from "@/db/provider";
import { passwords } from "@/db/schema";
import { and, eq, inArray, isNull, sql } from "drizzle-orm";

import { pickEditablePasswordFields } from "./passwordChanges";

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
   * Observes active passwords only.
   */
  observeAll() {
    return this.db.select().from(passwords).where(isNull(passwords.deletedAt));
  }

  /**
   * Observes active passwords in ONE category.
   *
   * null = Uncategorized.
   */
  observeByCategory(categoryId: number | null) {
    return this.db
      .select()
      .from(passwords)
      .where(
        and(
          isNull(passwords.deletedAt),
          categoryId === null
            ? isNull(passwords.categoryId)
            : eq(passwords.categoryId, categoryId),
        ),
      );
  }

  /**
   * Returns category IDs for active passwords only.
   */
  observeCategoryIds() {
    return this.db
      .select({ categoryId: passwords.categoryId })
      .from(passwords)
      .where(isNull(passwords.deletedAt));
  }

  async getAll() {
    return this.observeAll();
  }

  /** Total number of active passwords. */
  async count() {
    const [row] = await this.db
      .select({ count: sql<number>`count(*)` })
      .from(passwords)
      .where(isNull(passwords.deletedAt));

    return Number(row?.count ?? 0);
  }

  /**
   * Returns an active password, or undefined if it doesn't exist
   * or has been soft-deleted.
   */
  async getById(id: number) {
    const [password] = await this.db
      .select()
      .from(passwords)
      .where(and(eq(passwords.id, id), isNull(passwords.deletedAt)));

    return password;
  }

  async create(data: CreatePasswordInput) {
    const [createdPassword] = await this.db
      .insert(passwords)
      .values({ source: "APPLICATION", ...data })
      .returning();

    return createdPassword;
  }

  /**
   * Imports passwords in chunks, preserving their timestamps.
   * Newly imported rows use the default NULL deletedAt.
   */
  async importMany(
    rows: readonly ImportPasswordRow[],
  ): Promise<ImportManyResult> {
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
            tx.insert(passwords)
              .values({ source: "IMPORTED", ...toValues(row) })
              .run();
          }
        });

        slice.forEach((_, offset) => insertedIndexes.push(start + offset));
      } catch {
        slice.forEach((row, offset) => {
          try {
            this.db
              .insert(passwords)
              .values({ source: "IMPORTED", ...toValues(row) })
              .run();
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

  /**
   * Updates active passwords only.
   */
  async update(id: number, data: UpdatePasswordInput) {
    const [updatedPassword] = await this.db
      .update(passwords)
      // TODO:
      // `updated_at` is refreshed by the schema's `$onUpdateFn`. Only the
      // editable fields are passed on: callers may hand over more (e.g. the
      // form state with its formatted "Updated" text), and that must never be
      // written into a column.
      .set(pickEditablePasswordFields(data))
      .where(and(eq(passwords.id, id), isNull(passwords.deletedAt)))
      .returning();

    return updatedPassword;
  }

  /**
   * Soft-deletes one password.
   *
   * Returns the updated row, or undefined if it isn't active.
   */
  async delete(id: number) {
    const [deletedPassword] = await this.db
      .update(passwords)
      .set({ deletedAt: sql`CURRENT_TIMESTAMP` })
      .where(and(eq(passwords.id, id), isNull(passwords.deletedAt)))
      .returning();

    return deletedPassword;
  }

  /** Number of active passwords in ONE category. */
  async countByCategory(categoryId: number) {
    const [row] = await this.db
      .select({ count: sql<number>`count(*)` })
      .from(passwords)
      .where(
        and(eq(passwords.categoryId, categoryId), isNull(passwords.deletedAt)),
      );

    return Number(row?.count ?? 0);
  }

  /**
   * Moves active passwords to a category (null = Uncategorized).
   */
  async moveToCategory(ids: number[], categoryId: number | null) {
    if (ids.length === 0) {
      return;
    }

    this.db.transaction((tx) => {
      for (const idChunk of chunk(ids, BATCH_SIZE)) {
        tx.update(passwords)
          .set({ categoryId })
          .where(
            and(inArray(passwords.id, idChunk), isNull(passwords.deletedAt)),
          )
          .run();
      }
    });
  }

  /**
   * Soft-deletes many active passwords in one transaction.
   */
  async deleteMany(ids: number[]) {
    if (ids.length === 0) {
      return;
    }

    this.db.transaction((tx) => {
      for (const idChunk of chunk(ids, BATCH_SIZE)) {
        tx.update(passwords)
          .set({ deletedAt: sql`CURRENT_TIMESTAMP` })
          .where(
            and(inArray(passwords.id, idChunk), isNull(passwords.deletedAt)),
          )
          .run();
      }
    });
  }
}
