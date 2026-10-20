import Config from "@/config";
import * as SQLite from "expo-sqlite";
import {
  CURRENT_TIMESTAMP_SQL,
  normalizeTimestamp,
} from "./timestamp_unixepoch_utils";

export default async function dataRecoveryFromTestDB(
  expoDb: SQLite.SQLiteDatabase,
) {
  let oldDb: SQLite.SQLiteDatabase | undefined;

  try {
    oldDb = await SQLite.openDatabaseAsync(Config.OLD_MISTAKEN_DATABASE_NAME);

    // Verify passwords table exists.
    const tableExists = await oldDb.getFirstAsync<{ name: string }>(
      `SELECT name
       FROM sqlite_master
       WHERE type = 'table'
         AND name = 'passwords'`,
    );

    if (!tableExists) {
      await oldDb.closeAsync();
      oldDb = undefined;
      await SQLite.deleteDatabaseAsync(Config.OLD_MISTAKEN_DATABASE_NAME);
      return;
    }

    const rows = await oldDb.getAllAsync<{
      domain: string | null;
      username: string | null;
      password: string;
      notes: string | null;
      url: string | null;
      created_at: unknown;
      updated_at: unknown;
    }>(
      `SELECT
          domain,
          username,
          password,
          notes,
          url,
          created_at,
          updated_at
       FROM passwords`,
    );

    const insertStatement = await expoDb.prepareAsync(`
      INSERT INTO passwords (
        domain,
        username,
        password,
        notes,
        url,
        created_at,
        updated_at
      )
      VALUES (
        $domain,
        $username,
        $password,
        $notes,
        $url,
        COALESCE($created_at, ${CURRENT_TIMESTAMP_SQL}),
        COALESCE($updated_at, $created_at, ${CURRENT_TIMESTAMP_SQL})
      )
    `);

    try {
      await expoDb.withTransactionAsync(async () => {
        for (const row of rows) {
          const createdAt = normalizeTimestamp(row.created_at);
          const updatedAt = normalizeTimestamp(row.updated_at);

          await insertStatement.executeAsync({
            $domain: row.domain,
            $username: row.username,
            $password: row.password,
            $notes: row.notes,
            $url: row.url,
            $created_at: createdAt,
            $updated_at: updatedAt,
          });
        }
      });
    } finally {
      await insertStatement.finalizeAsync();
    }
  } catch (error) {
    console.error("Data recovery from test database failed:", error);
    throw error;
  } finally {
    await oldDb?.closeAsync();
  }
}
