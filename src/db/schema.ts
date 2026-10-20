import { sql } from "drizzle-orm";
import {
  index,
  integer,
  sqliteTable,
  text,
  type AnySQLiteColumn,
} from "drizzle-orm/sqlite-core";

/**
 * "Uncategorized" is NOT a row here, it is the virtual state
 * for, passwords.category_id IS NULL.
 */
export const categories = sqliteTable("categories", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  name: text("name").notNull(),

  createdAt: text("created_at")
    .notNull()
    .default(sql`CURRENT_TIMESTAMP`),

  updatedAt: text("updated_at")
    .notNull()
    .default(sql`CURRENT_TIMESTAMP`)
    .$onUpdateFn(() => sql`CURRENT_TIMESTAMP`),

  // TODO: soft delete category
  // deletedAt: text("deleted_at"),
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

    createdAt: text("created_at")
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),

    updatedAt: text("updated_at")
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`)
      .$onUpdateFn(() => sql`CURRENT_TIMESTAMP`),

    deletedAt: text("deleted_at"),

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
