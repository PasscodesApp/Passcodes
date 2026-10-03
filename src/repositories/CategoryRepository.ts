import type { DrizzleDatabase } from "@/db/provider";
import { categories, passwords } from "@/db/schema";
import { and, asc, eq, ne, sql } from "drizzle-orm";

export const CATEGORY_NAME_MAX_LENGTH = 40;

/**
 * Thrown for invalid user input (empty / too long / duplicate name).
 * The message is safe to show to the user in a toast.
 */
export class CategoryValidationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "CategoryValidationError";
  }
}

/**
 * What to do with the passwords of a category that is being deleted.
 *
 * - `uncategorize` (default, safe): passwords stay, `category_id` becomes NULL.
 * - `delete`: passwords are permanently deleted (destructive).
 */
export type DeleteCategoryPasswordsMode = "uncategorize" | "delete";

export class CategoryRepository {
  constructor(private readonly db: DrizzleDatabase) {}

  /**
   * Creates a query for observing all categories (sorted by name).
   *
   * Pass the returned query to `useLiveQuery`. Password counts are
   * intentionally NOT joined here: `useLiveQuery` only re-runs when the
   * `from()` table changes, so a join would give stale counts. Derive counts
   * from the (live) password list instead, see `countPasswordsByCategory`.
   */
  observeAll() {
    return this.db
      .select()
      .from(categories)
      .orderBy(asc(sql`lower(${categories.name})`), asc(categories.id));
  }

  async getAll() {
    return this.observeAll();
  }

  async getById(id: number) {
    const [category] = await this.db
      .select()
      .from(categories)
      .where(eq(categories.id, id));

    return category;
  }

  async create(name: string) {
    const cleanName = this.validateName(name);
    await this.assertNameAvailable(cleanName);

    const [created] = await this.db
      .insert(categories)
      .values({ name: cleanName })
      .returning();

    return created!;
  }

  /**
   * Renames a category. Passwords reference the category by id, so no
   * password row is touched.
   */
  async rename(id: number, name: string) {
    const cleanName = this.validateName(name);
    await this.assertNameAvailable(cleanName, id);

    const [updated] = await this.db
      .update(categories)
      .set({ name: cleanName, updatedAt: sql`CURRENT_TIMESTAMP` })
      .where(eq(categories.id, id))
      .returning();

    if (!updated) {
      throw new Error(`Category ${id} not found`);
    }

    return updated;
  }

  /**
   * Deletes a category inside a single transaction.
   *
   * Passwords are never deleted unless `mode === "delete"` is passed
   * explicitly.
   */
  async delete(
    id: number,
    mode: DeleteCategoryPasswordsMode = "uncategorize",
  ) {
    this.db.transaction((tx) => {
      if (mode === "delete") {
        tx.delete(passwords).where(eq(passwords.categoryId, id)).run();
      } else {
        tx.update(passwords)
          .set({ categoryId: null })
          .where(eq(passwords.categoryId, id))
          .run();
      }

      tx.delete(categories).where(eq(categories.id, id)).run();
    });
  }

  /**
   * Number of passwords per category id, with `null` = Uncategorized.
   * Pure helper so it can run on the already-loaded live password list
   * (no extra query, no N+1).
   */
  static countPasswordsByCategory(
    passwordList: ReadonlyArray<{ categoryId: number | null }>,
  ): Map<number | null, number> {
    const counts = new Map<number | null, number>();

    for (const { categoryId } of passwordList) {
      counts.set(categoryId, (counts.get(categoryId) ?? 0) + 1);
    }

    return counts;
  }

  // ---------- internals ----------

  private validateName(name: string): string {
    const cleanName = name.trim().replace(/\s+/g, " ");

    if (!cleanName) {
      throw new CategoryValidationError("Category name can't be empty.");
    }

    if (cleanName.length > CATEGORY_NAME_MAX_LENGTH) {
      throw new CategoryValidationError(
        `Category name can be at most ${CATEGORY_NAME_MAX_LENGTH} characters.`,
      );
    }

    // "Uncategorized" is reserved for the virtual NULL group.
    if (cleanName.toLowerCase() === "uncategorized") {
      throw new CategoryValidationError(
        '"Uncategorized" is reserved, please pick another name.',
      );
    }

    return cleanName;
  }

  private async assertNameAvailable(name: string, exceptId?: number) {
    const sameName = sql`lower(${categories.name}) = lower(${name})`;

    const [existing] = await this.db
      .select({ id: categories.id })
      .from(categories)
      .where(
        exceptId === undefined
          ? sameName
          : and(sameName, ne(categories.id, exceptId)),
      )
      .limit(1);

    if (existing) {
      throw new CategoryValidationError("A category with this name exists.");
    }
  }
}
