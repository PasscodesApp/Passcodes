import { defineConfig } from "drizzle-kit";

export default defineConfig({
  schema: "./db/schema.ts",
  out: "./db/drizzle",
  dialect: "sqlite",
  driver: "expo", // <--- very important
});
