import ScreenHeading from "@/components/ScreenHeading";
import roomDrizzleMigration from "@/libs/room_drizzle_migration";
import * as SQLite from "expo-sqlite";
import AsyncStorage from "expo-sqlite/kv-store";
import { useEffect, useState } from "react";
import { Button, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

enum MIGRATION_STATUS {
  IDLE = "idle",
  RUNNING = "running",
  FAILED = "failed",
  SUCCESS = "success",
}

export default function GetBackPasswordsScreen() {
  const MIGRATION_KEY = "room_expo_migration_complete_v1";
  const [taskStatus, setTaskStatus] = useState({
    message: "Task Idle",
    isError: false,
  });

  const expoDb = SQLite.useSQLiteContext();

  useEffect(() => {
    async function runMigration() {
      try {
        // Check migration marker
        let alreadyMigrated = await AsyncStorage.getItem(MIGRATION_KEY);

        if (!alreadyMigrated) {
          alreadyMigrated = MIGRATION_STATUS.IDLE;
        }

        if (alreadyMigrated === MIGRATION_STATUS.SUCCESS) {
          setTaskStatus({
            message: "Migration already completed.",
            isError: false,
          });

          return;
        }

        await AsyncStorage.setItem(MIGRATION_KEY, MIGRATION_STATUS.RUNNING);

        setTaskStatus({
          message: "Running Task....",
          isError: false,
        });

        // Run migration
        const result = await roomDrizzleMigration(expoDb);

        await AsyncStorage.setItem(MIGRATION_KEY, MIGRATION_STATUS.SUCCESS);

        setTaskStatus({
          message: result,
          isError: false,
        });
      } catch (error: any) {
        await AsyncStorage.setItem(MIGRATION_KEY, MIGRATION_STATUS.FAILED);

        setTaskStatus({
          message: error?.message ?? "Unknown Error: migration failure",
          isError: true,
        });
      }
    }

    runMigration();
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
      <ScreenHeading title="GetBack Data" />
      <Text
        style={{
          color: taskStatus.isError ? "#d1120f" : "#06a3eb",
          fontSize: 16,
        }}
      >
        {taskStatus.message}
      </Text>

      <Button
        title="Clear Previous Result (Unsafe)"
        onPress={() => {
          AsyncStorage.removeItem(MIGRATION_KEY);

          setTaskStatus({
            message: "Result is cleared. migration will re-run nexttime..",
            isError: true,
          });
        }}
      />
    </SafeAreaView>
  );
}
