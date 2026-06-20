import ScreenHeading from "@/components/ScreenHeading";
import dataRecoveryFromTestDB from "@/libs/data_recovery_test_db_mess";
import * as SQLite from "expo-sqlite";
import { useEffect, useState } from "react";
import { Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

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
      await dataRecoveryFromTestDB(expoDb);
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
