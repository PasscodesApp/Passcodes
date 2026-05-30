import ScreenHeading from "@/components/ScreenHeading";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { Directory, File, Paths } from "expo-file-system";
import * as SQLite from "expo-sqlite";
import { useEffect, useState } from "react";
import { Button, Platform, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

const ROOM_DB_NAME = "master";

enum MIGRATION_STATUS {
  IDLE = "idle",
  RUNNING = "running",
  FAILED = "failed",
  SUCCESS = "success",
}

export default function GetBackPasswords() {
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
        const result = await migrateOldAndroidData(expoDb);

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

    // Only Android supports this Room migration
    if (Platform.OS !== "android") {
      setTaskStatus({
        message: "Migration not required on this platform.",
        isError: false,
      });

      return;
    }
    runMigration();
  }, []);

  return (
    <SafeAreaView
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        gap: 10,
      }}
    >
      <ScreenHeading title="GetBack Data" />
      <Text
        style={{
          color: taskStatus.isError ? "#ef1713" : "#1aadf1",
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

async function migrateOldAndroidData(expoDb: SQLite.SQLiteDatabase) {
  // ==========================================
  // STEP 1: locate database & backup directories
  // ==========================================

  const androidNativeDbDir = new Directory(
    Paths.document.parentDirectory,
    "databases",
  );
  const roomDbFile = new File(androidNativeDbDir.uri, ROOM_DB_NAME);

  if (!roomDbFile.exists) {
    console.warn("Room database file not found at:", roomDbFile.uri);
    return "Data Migration Was Not Need, What-so-Ever As roomdb file not found";
  }

  const backUpDir = new Directory(Paths.cache, "backup_room_migration");
  if (!backUpDir.exists) {
    console.warn("Backup directory not found. Creating it at:", backUpDir.uri);
    backUpDir.create();
  }

  // ==========================================
  // STEP 2. execute the copy/backup operation
  // ==========================================

  // We must track all 3 files that make up a Room database instance
  const filesToBackup = [
    ROOM_DB_NAME,
    `${ROOM_DB_NAME}-wal`,
    `${ROOM_DB_NAME}-shm`,
  ];

  console.log("Starting secure backup sequence...");

  try {
    for (const fileName of filesToBackup) {
      // Construct exact source and destination URIs for each file
      const sourceFile = new File(androidNativeDbDir.uri, fileName);
      const destinationFile = new File(backUpDir.uri, fileName);

      if (sourceFile.exists) {
        sourceFile.copy(destinationFile);
        console.log(`[Backup Success] Copied: ${destinationFile.uri}`);
      } else {
        console.log(
          `[Backup Info] ${sourceFile.uri} does not exist.. (skipping)`,
        );
      }
    }

    console.log(
      "Backup Complete: Persistent backup secured at:",
      backUpDir.uri,
    );
  } catch (error) {
    console.error("CRITICAL ERROR DURING BACKUP PHASE:", error);
    // Halt execution immediately to prevent data loss or manipulation
    throw new Error("Migration stopped: Backup could not be verified safely.");
  }

  // ==========================================
  // STEP 3: copy to expo-sqlite workspace
  // ==========================================

  // Temporary name for the old Room database inside Expo's workspace
  // to keep it isolated from your new Drizzle database file.
  const EXPO_TEMP_ROOM_DB_NAME = "old_room_source";

  const filesToProcess = new Map([
    [ROOM_DB_NAME, EXPO_TEMP_ROOM_DB_NAME],
    [`${ROOM_DB_NAME}-wal`, `${EXPO_TEMP_ROOM_DB_NAME}-wal`],
    [`${ROOM_DB_NAME}-shm`, `${EXPO_TEMP_ROOM_DB_NAME}-shm`],
  ]);

  // Expo SQLite strictly expects databases to be in standard application files/SQLite/
  const expoSqliteDir = new Directory(Paths.document, "SQLite");

  try {
    if (!expoSqliteDir.exists) {
      console.warn(
        "expo-sqlite directory not found. Creating it at:",
        expoSqliteDir.uri,
      );
      expoSqliteDir.create();
    }

    for (const fileName of filesToProcess.keys()) {
      const sourceFile = new File(androidNativeDbDir.uri, fileName);
      let targetName = filesToProcess.get(fileName);

      if (!targetName) {
        // This is likely unreachable point, but just satisfy typescript..
        throw new Error(
          `Mapkey (' ${targetName} ') not found in map (' ${filesToProcess} ')`,
        );
      }

      const targetFile = new File(expoSqliteDir.uri, targetName);

      if (sourceFile.exists) {
        sourceFile.copy(targetFile);
        console.log(
          `[Expo-SQLite Workspace] Placed copy of (' ${fileName} '): ${targetFile.uri}`,
        );
      } else {
        console.warn(
          `[Expo-SQLite Workspace] Skipping (' ${fileName} ') for copy as file not found!!`,
        );
      }
    }
  } catch (error) {
    console.error("CRITICAL ERROR DURING MOVE TO WORKSPACE PHASE:", error);
    // Halt execution immediately to prevent data loss or manipulation
    throw new Error(
      "Migration stopped: file move to expo-sqlite workspace could not be verified safely.",
    );
  }

  // ==========================================
  // STEP 4: open room database & extracted data
  // ==========================================

  let intermediateDataRepresentation: any[] = [];
  let sourceResult: { count: number } | null = null;

  let roomExpoSqliteDb: SQLite.SQLiteDatabase | undefined = undefined;
  try {
    console.log("Opening working room database instance via Expo SQLite...");
    roomExpoSqliteDb = await SQLite.openDatabaseAsync(EXPO_TEMP_ROOM_DB_NAME);

    // 1. Fetch Room database version via user_version PRAGMA
    const versionResult = await roomExpoSqliteDb.getFirstAsync<{
      user_version: number;
    }>("PRAGMA user_version;");
    let dbVersion = versionResult?.user_version;

    if (!dbVersion) {
      dbVersion = 1;
      console.log(
        `[Database Discovery] Room User Version Not Detected, So Assuming Database as: v${dbVersion}`,
      );
    }

    console.log(
      `[Database Discovery] Room User Version Detected: v${dbVersion}`,
    );

    // 2. Total Count Of Passswords
    sourceResult = await roomExpoSqliteDb.getFirstAsync<{
      count: number;
    }>("SELECT COUNT(*) as count FROM passwords;");

    // 3. Conditional Extraction Routing
    if (dbVersion === 2) {
      console.log("Processing Version 2 database structure extraction...");
      intermediateDataRepresentation =
        await extractVersion2Data(roomExpoSqliteDb);
    } else if (dbVersion === 1) {
      console.log("Processing Version 1 database structure extraction...");
      intermediateDataRepresentation =
        await extractVersion1Data(roomExpoSqliteDb);
    } else {
      // This is likely unreachable point, but just satisfy typescript correctness..
      throw new Error(
        `Unexpected database schema version encountered: v${dbVersion}`,
      );
    }

    console.log(
      "[Database Discovery] Successfully extracted intermediate data representation.",
    );
  } catch (error) {
    console.error(
      "CRITICAL ERROR ENCOUNTERED DURING DATA MID-REPRESENTATION PHRASE:",
      error,
    );

    throw new Error(
      "Migration stopped: room database's intermediate representation could not be verified safely.",
    );
  } finally {
    await roomExpoSqliteDb?.closeAsync();
  }

  // ==========================================
  // STEP 5: room database -> expo database
  // ==========================================
  console.log("Moving Data.... it might take few minutes");

  let insertStatement: SQLite.SQLiteStatement | undefined;

  try {
    const rowsBefore = await expoDb.getFirstAsync<{ count: number }>(
      "SELECT COUNT(*) as count FROM passwords;",
    );

    await expoDb.withTransactionAsync(async () => {
      insertStatement = await expoDb.prepareAsync(
        `INSERT INTO passwords (domain, username, password, notes, url, created_at, updated_at) 
        VALUES ($domain, $username, $password, $notes, $url, $created_at, $updated_at)`,
      );

      for (const data of intermediateDataRepresentation) {
        let statementData = {
          $domain: data.domain,
          $username: data.username,
          $password: data.password,
          $notes: data.notes,
          $url: data.url,
          $created_at: data.created_at,
          $updated_at: data.updated_at,
        };

        await insertStatement.executeAsync(statementData);
      }
    });

    const rowsAfter = await expoDb.getFirstAsync<{
      count: number;
    }>("SELECT COUNT(*) as count FROM passwords;");

    const sourceRows = sourceResult?.count ?? 0;
    const inserted = (rowsAfter?.count ?? 0) - (rowsBefore?.count ?? 0);

    if (sourceRows !== inserted) {
      throw new Error(
        `Data migration verification failed. Source: ${sourceRows}, Inserted: ${inserted}`,
      );
    }
  } catch (error) {
    console.error(
      "CRITICAL ERROR ENCOUNTERED DATA MOVING TO EXPO SQLITE PHRASE:",
      error,
    );

    throw new Error(
      "Migration stopped: expo database's data migrated could not be verified safely.",
    );
  } finally {
    await insertStatement?.finalizeAsync();
  }

  console.log("Successfully moved data to expo-sqlite db...");

  console.log("Successfully Migrated Data, Now just clean up is left...");

  // ==========================================
  // STEP 6: clean up
  // ==========================================

  console.log("[CLEAN MESS]: cleaning...");

  for (const fileName of filesToProcess.keys()) {
    let targetName = filesToProcess.get(fileName);

    if (!targetName) {
      // This is likely unreachable point, but just satisfy typescript..
      throw new Error(
        `Mapkey (' ${targetName} ') not found in map (' ${filesToProcess} ')`,
      );
    }

    const targetFile = new File(expoSqliteDir.uri, targetName);

    if (targetFile.exists) {
      targetFile.delete();
      console.log(`[CLEAN MESS]] Delete (' ${fileName} '): ${targetFile.uri}`);
    }
  }

  console.log("[CLEAN MESS]: cleaned all backup files...");

  const successMessage = "SUCCESSFULLY MIGRATED All OLD DATA!!";
  console.log(successMessage);
  console.log("task exit 0;");

  return successMessage;
}

async function extractVersion2Data(db: SQLite.SQLiteDatabase) {
  let rows: any[] = await db.getAllAsync(
    `SELECT domain, username, password, notes, url, created_at, updated_at FROM passwords`,
  );
  console.log(`Extracted rows from v2 passwords table: ${rows.length}`);
  return rows;
}

async function extractVersion1Data(db: SQLite.SQLiteDatabase) {
  let dataRows: any[] = await db.getAllAsync(
    `SELECT domain, username, password, notes, created_at, updated_at FROM passwords`,
  );
  console.log(`Extracted rows from v1 passwords table: ${dataRows.length}`);

  const updatedRows = dataRows.map((dataRow) => {
    let domain = dataRow.domain;
    return { ...dataRow, url: `https://local.${domain}` };
  });

  console.log("Successfully generated urls for v1 to make it compatible...");

  return updatedRows;
}
