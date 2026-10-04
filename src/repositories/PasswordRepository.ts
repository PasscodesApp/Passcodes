import type { DrizzleDatabase } from "@/db/provider";
import { passwords } from "@/db/schema";
import { eq, inArray, isNull, sql } from "drizzle-orm";

/** SQLite limits the number of bound variables, so big batches are chunked. */
const BATCH_SIZE = 500;

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

  // TODO: temporary solution we will create a upsert all method
  async importAll(data: CreatePasswordInput[]) {
    this.db.transaction((tx) => {
      data.forEach((importablePassword) => {
        tx.insert(passwords)
          .values({
            domain: importablePassword.domain,
            username: importablePassword.username,
            password: importablePassword.password,
            notes: importablePassword.notes,
            url: importablePassword.url,
          })
          .execute();
      });
    });
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
