import { sql } from "drizzle-orm";
import { integer, sqliteTable, text } from "drizzle-orm/sqlite-core";

export const passwords = sqliteTable("passwords", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  domain: text("domain").notNull(),
  username: text("username").notNull(),
  password: text("password").notNull(),
  notes: text("notes"),
  url: text("url"),
  createdAt: text("created_at").default(sql`(CURRENT_TIMESTAMP)`),
  updatedAt: text("updated_at").default(sql`(CURRENT_TIMESTAMP)`),
});
