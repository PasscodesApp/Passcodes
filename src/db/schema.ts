import { sql } from "drizzle-orm";
import {
  index,
  integer,
  sqliteTable,
  text,
  type AnySQLiteColumn,
} from "drizzle-orm/sqlite-core";

/**
 * Timestamps are Unix epoch seconds (UTC).
 */

/**
 * Uncategorized is a virtual state represented by
 * passwords.category_id IS NULL.
 */
export const categories = sqliteTable("categories", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  name: text("name").notNull(),

  createdAt: integer("created_at")
    .notNull()
    .default(sql`(CAST(strftime('%s', 'now') AS INTEGER))`),

  updatedAt: integer("updated_at")
    .notNull()
    .default(sql`(CAST(strftime('%s', 'now') AS INTEGER))`)
    .$onUpdateFn(() => sql`(CAST(strftime('%s', 'now') AS INTEGER))`),

  // TODO: soft delete category
  // deletedAt: integer("deleted_at"),
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

    createdAt: integer("created_at")
      .notNull()
      .default(sql`(CAST(strftime('%s', 'now') AS INTEGER))`),

    updatedAt: integer("updated_at")
      .notNull()
      .default(sql`(CAST(strftime('%s', 'now') AS INTEGER))`)
      .$onUpdateFn(() => sql`(CAST(strftime('%s', 'now') AS INTEGER))`),

    deletedAt: integer("deleted_at"),

    // Comma-separated Android package names and/or iOS bundle IDs.
    applicationIds: text("application_ids"),

    source: text("source", {
      enum: ["IMPORTED", "APPLICATION", "AUTOFILL"],
    }),

    /*
     * NULL = Uncategorized.
     * CategoryRepository.delete() handles reassignment
     * inside a transaction.
     */
    categoryId: integer("category_id").references(
      (): AnySQLiteColumn => categories.id,
      { onDelete: "set null" },
    ),
  },
  (table) => [index("passwords_category_id_idx").on(table.categoryId)],
);
