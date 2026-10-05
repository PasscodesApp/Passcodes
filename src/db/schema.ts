import { sql } from "drizzle-orm";
import {
  index,
  integer,
  sqliteTable,
  text,
  type AnySQLiteColumn,
} from "drizzle-orm/sqlite-core";

/**
 * Flat password categories (no hierarchy).
 *
 * "Uncategorized" is NOT a row here, it is the virtual state
 * `passwords.category_id IS NULL`.
 */
export const categories = sqliteTable("categories", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  name: text("name").notNull(),
  createdAt: text("created_at").default(sql`(CURRENT_TIMESTAMP)`),
  updatedAt: text("updated_at").default(sql`(CURRENT_TIMESTAMP)`),
});

export const passwords = sqliteTable(
  "passwords",
  {
    id: integer("id").primaryKey({ autoIncrement: true }),
    domain: text("domain").notNull(),
    username: text("username").notNull(),
    password: text("password").notNull(),
    notes: text("notes"),
    url: text("url"),
    createdAt: text("created_at").default(sql`(CURRENT_TIMESTAMP)`),
    updatedAt: text("updated_at").default(sql`(CURRENT_TIMESTAMP)`),

    /*
     * NULL = Uncategorized.
     *
     * NOTE: SQLite only enforces foreign keys when `PRAGMA foreign_keys = ON`.
     * The app does not rely on it, CategoryRepository.delete() explicitly
     * handles the passwords of a deleted category inside a transaction.
     */
    categoryId: integer("category_id").references(
      (): AnySQLiteColumn => categories.id,
      { onDelete: "set null" },
    ),

    /*
     * Suggested Updates:-
     *
     *  createdAt: text("created_at")
     *    .notNull()
     *    .default(sql`CURRENT_TIMESTAMP`),
     *
     *  updatedAt: text("updated_at")
     *    .notNull()
     *    .default(sql`CURRENT_TIMESTAMP`)
     *    .$onUpdateFn(() => sql`CURRENT_TIMESTAMP`),
     */
  },
  (table) => [index("passwords_category_id_idx").on(table.categoryId)],
);
