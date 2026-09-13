import * as SQLite from "expo-sqlite";

/**
 * Opens a SQLite database, passes the connection to the callback,
 * returns the callback result, and closes the database afterward.
 *
 * The callback can return either a value or a Promise.
 *
 * @example
 * ```ts
 * const result = await withSQLiteDatabase(
 *   "master.db",
 *   (db) => query(db),
 * );
 * ```
 */
export async function withSQLiteDatabase<T>(
  databaseName: string,
  callback: (db: SQLite.SQLiteDatabase) => T | Promise<T>,
): Promise<T> {
  const db = await SQLite.openDatabaseAsync(databaseName);

  try {
    return await callback(db);
  } finally {
    await db.closeAsync();
  }
}
