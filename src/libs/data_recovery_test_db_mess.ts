import * as SQLite from "expo-sqlite";

const OLD_DATABASE_NAME = "test2.db";

export default async function dataRecoveryFromTestDB(
  expoDb: SQLite.SQLiteDatabase,
) {
  let oldDb: SQLite.SQLiteDatabase | undefined = undefined;

  try {
    oldDb = await SQLite.openDatabaseAsync(OLD_DATABASE_NAME);

    // Verify passwords table exists
    const tableExists = await oldDb.getFirstAsync(
      `SELECT name
        FROM sqlite_master
        WHERE type='table'
        AND name='passwords'`,
    );

    if (!tableExists) {
      await oldDb.closeAsync();
      await SQLite.deleteDatabaseAsync(OLD_DATABASE_NAME);
      return;
    }

    const rows = await oldDb.getAllAsync<{
      domain: string | null;
      username: string | null;
      password: string;
      notes: string | null;
      url: string | null;
      created_at: number;
      updated_at: number;
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

    await expoDb.withTransactionAsync(async () => {
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
          $created_at,
          $updated_at
        )
      `);

      for (const row of rows) {
        await insertStatement.executeAsync({
          $domain: row.domain,
          $username: row.username,
          $password: row.password,
          $notes: row.notes,
          $url: row.url,
          $created_at: row.created_at,
          $updated_at: row.updated_at,
        });
      }

      await insertStatement.finalizeAsync();
    });
  } catch (error) {
    console.error("Migration failed:", error);
    throw error;
  } finally {
    oldDb?.closeAsync();
  }
}
