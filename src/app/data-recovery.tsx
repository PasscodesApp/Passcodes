import ScreenHeading from "@/components/ScreenHeading";
import * as SQLite from "expo-sqlite";
import { useEffect, useState } from "react";
import { Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

const OLD_DATABASE_NAME = "test2.db";

type MigrationResult = {
  state: "Running" | "Error" | "Success";
  message: string;
};

export default function DataRecoveryScreen() {
  const expoDb = SQLite.useSQLiteContext();
  const [result, setResult] = useState<MigrationResult>({
    state: "Running",
    message: "please wait...",
  });

  useEffect(() => {
    async function runMigration() {
      await migrateTestDbToMasterDb(expoDb);
    }

    runMigration()
      .then(() => {
        setResult({
          state: "Success",
          message: "Done!! PROBLEM FIXED",
        });
      })
      .catch((e) => {
        setResult({
          state: "Error",
          message: e.toString(),
        });
      });
  }, []);

  return (
    <SafeAreaView
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        padding: 20,
        gap: 10,
      }}
    >
      <ScreenHeading title="Data Recovery" />

      <Text style={{ fontSize: 16 }}>{result.state}</Text>
      <Text style={{ color: result.state === "Error" ? "#ef1713" : "#1aadf1" }}>
        {result.message}
      </Text>
    </SafeAreaView>
  );
}

export async function migrateTestDbToMasterDb(expoDb: SQLite.SQLiteDatabase) {
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

      try {
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
      } finally {
        await insertStatement.finalizeAsync();
      }
    });
  } catch (error) {
    console.error("Migration failed:", error);
    throw error;
  } finally {
    oldDb?.closeAsync();
  }
}
