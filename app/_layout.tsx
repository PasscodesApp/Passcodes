import { drizzle } from "drizzle-orm/expo-sqlite";
import { useMigrations } from "drizzle-orm/expo-sqlite/migrator";
import { Directory, File, Paths } from "expo-file-system";
import { Stack } from "expo-router";
import * as SQLite from "expo-sqlite";
import { SQLiteProvider, openDatabaseSync } from "expo-sqlite";
import { Suspense } from "react";
import { ActivityIndicator, Text, View } from "react-native";
import migrations from "../db/drizzle/migrations";

async function migrateOldAndroidData() {
  const ROOM_DB_NAME = "master";

  // ==========================================
  // STEP 1: locate database & backup directiories
  // ==========================================

  const androidNativeDbDir = new Directory(
    Paths.document.parentDirectory,
    "databases",
  );
  const roomDbFile = new File(androidNativeDbDir.uri, ROOM_DB_NAME);

  if (!roomDbFile.exists) {
    console.warn("Room database file not found at:", roomDbFile.uri);
    return; // TODO: Throw a error
  }

  const backUpDir = new Directory(Paths.document, "backup_room_migration");
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
      const sourceDir = new Directory(androidNativeDbDir.uri, fileName);
      const destinationDir = new Directory(backUpDir.uri, fileName);

      if (sourceDir.exists) {
        sourceDir.copy(destinationDir);
        console.log(`[Backup Success] Copied: ${destinationDir.uri}`);
      } else {
        console.log(
          `[Backup Info] ${sourceDir.uri} does not exist.. (skipping)`,
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

  const EXPO_TEMP_ROOM_DB_NAME = "old_room_source.db";

  const filesToProcess = new Map([
    [ROOM_DB_NAME, EXPO_TEMP_ROOM_DB_NAME],
    [`${ROOM_DB_NAME}-wal`, `${EXPO_TEMP_ROOM_DB_NAME}-wal`],
    [`${ROOM_DB_NAME}-shm`, `${EXPO_TEMP_ROOM_DB_NAME}-shm`],
  ]);

  try {
    // Expo SQLite strictly expects databases to be in standard application files/SQLite/
    const expoSqliteDir = new Directory(Paths.document, "SQLite");

    if (!expoSqliteDir.exists) {
      console.warn(
        "expo-sqlite directory not found. Creating it at:",
        expoSqliteDir.uri,
      );
      expoSqliteDir.create();
    }

    // Temporary name for the old Room database inside Expo's workspace
    // to keep it isolated from your new Drizzle database file.

    for (const fileName of filesToProcess.keys()) {
      const sourceDir = new Directory(androidNativeDbDir.uri, fileName);

      // Map names so SQLite links the WAL files correctly:
      // old_room_source.db, old_room_source.db-wal, old_room_source.db-shm
      let targetName = filesToProcess.get(fileName);

      if (!targetName) {
        // This is likely unreachable point, but just satisfy typescript..
        throw new Error(
          `Mapkey (' ${targetName} ') not found in map (' ${filesToProcess} ')`,
        );
      }

      const targetDir = new Directory(expoSqliteDir, targetName);

      if (sourceDir.exists) {
        sourceDir.copy(targetDir);
        console.log(
          `[Expo-SQLite Workspace] Placed copy of (' ${fileName} '): ${targetDir.uri}`,
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
  // STEP 4: open database & check version/schema
  // ==========================================

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

    // 2. Conditional Extraction Routing
    if (dbVersion === 2) {
      console.log("Processing Version 2 database structure extraction...");
      // await extractVersion2Data(roomExpoSqliteDb);
    } else if (dbVersion === 1) {
      console.log("Processing Version 1 database structure extraction...");
      // await extractVersion1Data(roomExpoSqliteDb);
    } else {
      // This is likely unreachable point, but just satisfy typescript..
      throw new Error(
        `Unexpected database schema version encountered: v${dbVersion}`,
      );
    }
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
}

// // Separate logic structures to cleanly parse each unique historical version
// async function extractVersion2Data(db: SQLite.SQLiteDatabase) {
//   // Use your expected table names from your Room setup
//   // For example, if a table is named 'credentials' or 'vault':
//   try {
//     // Replace 'your_v2_table_name' with your actual table names once confirmed
//     // const rows = await db.getAllAsync('SELECT * FROM your_v2_table_name;');
//     // console.log(`Extracted rows from v2 table: ${rows.length}`);
//     // return rows;
//   } catch (err) {
//     console.error("Failed executing selection query on v2 target tables:", err);
//     throw err;
//   }
// }

// async function extractVersion1Data(db: SQLite.SQLiteDatabase) {
//   // Logic structure for processing the v1 database variation
// }

const DATABASE_NAME = "test2.db";

export default function RootLayout() {
  const expoDb = openDatabaseSync(DATABASE_NAME);

  // TODO: This function must be called only on android platform...
  console.log("----- Migrating Android Old Data To Expo -----");
  migrateOldAndroidData();

  const db = drizzle(expoDb);

  const { success, error } = useMigrations(db, migrations);

  if (error) {
    return (
      <View>
        <Text>Migration error: {error.message}</Text>
      </View>
    );
  }

  if (!success) {
    return (
      <View>
        <Text>Migration is in progress...</Text>
      </View>
    );
  }

  return (
    <Suspense fallback={<ActivityIndicator size="large" />}>
      <SQLiteProvider databaseName={DATABASE_NAME} useSuspense>
        <Stack screenOptions={{ headerShown: false }} />
      </SQLiteProvider>
    </Suspense>
  );
}
