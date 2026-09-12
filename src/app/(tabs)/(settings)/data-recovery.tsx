import ScreenHeading from "@/components/ScreenHeading";
import Text from "@/components/Text";
import Config from "@/config";
import dataRecoveryFromTestDB from "@/libs/data_recovery_test_db_mess";
import { withSQLiteDatabase } from "@/libs/withSQLiteDatabase";
import { useEffect, useState } from "react";
import { SafeAreaView } from "react-native-safe-area-context";

type MigrationResult = {
  state: "Running" | "Error" | "Success";
  message: string;
};

export default function DataRecoveryScreen() {
  const [result, setResult] = useState<MigrationResult>({
    state: "Running",
    message: "Please wait...",
  });

  useEffect(() => {
    async function runMigration() {
      await withSQLiteDatabase(Config.DATABASE_NAME, async (expoDb) => {
        await dataRecoveryFromTestDB(expoDb);
      });
    }

    runMigration()
      .then(() => {
        setResult({
          state: "Success",
          message: "Done!! PROBLEM FIXED",
        });
      })
      .catch((error) => {
        setResult({
          state: "Error",
          message: error instanceof Error ? error.message : String(error),
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

      <Text
        style={{
          color: result.state === "Error" ? "#ef1713" : "#1aadf1",
        }}
      >
        {result.message}
      </Text>
    </SafeAreaView>
  );
}
