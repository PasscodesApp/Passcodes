import { Directory, File, Paths } from "expo-file-system";
import * as SQLite from "expo-sqlite";

const ROOM_DB_NAME = "master";

const SQLITE_NOW_SECONDS = "CAST(strftime('%s', 'now') AS INTEGER)";

const MIN_TIMESTAMP_SECONDS = 0;
const MAX_TIMESTAMP_SECONDS = 253_402_300_799; // 9999-12-31 23:59:59 UTC

export default async function roomDrizzleMigration(
  expoDb: SQLite.SQLiteDatabase,
) {
  // ==========================================
  // STEP 1: RoomDB Discovery: locate database & backup directories
  // ==========================================

  const androidNativeDbDir = new Directory(
    Paths.document.parentDirectory,
    "databases",
  );
  const roomDbFile = new File(androidNativeDbDir.uri, ROOM_DB_NAME);

  if (!roomDbFile.exists) {
    console.warn(
      "[RoomDB Discovery]: Room database file not found at:",
      roomDbFile.uri,
    );
    return "Data Migration Was Not Need, What-so-Ever As roomdb file not found";
  }

  console.log(
    "[RoomDB Discovery]: room database file found at:",
    roomDbFile.uri,
  );

  // ==========================================
  // STEP 2: Expo-SQLite Workspace: move to expo-sqlite workspace
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
        "[Expo-SQLite Workspace]: expo-sqlite directory not found. Creating it at:",
        expoSqliteDir.uri,
      );
      expoSqliteDir.create();
    }

    for (const [fileName, targetName] of filesToProcess) {
      const sourceFile = new File(androidNativeDbDir.uri, fileName);
      const targetFile = new File(expoSqliteDir.uri, targetName);

      if (sourceFile.exists) {
        await sourceFile.copy(targetFile);
        console.log(
          `[Expo-SQLite Workspace]: Placed copy of (' ${fileName} '): ${targetFile.uri}`,
        );
      } else {
        console.warn(
          `[Expo-SQLite Workspace]: Skipping (' ${fileName} ') for copy as file not found!!`,
        );
      }
    }
  } catch (error) {
    console.error(
      "[Expo-SQLite Workspace]: CRITICAL ERROR DURING MOVE TO WORKSPACE PHASE:",
      error,
    );
    // Halt execution immediately to prevent data loss or manipulation.
    throw new Error(
      "[Expo-SQLite Workspace]: Migration stopped: file move to expo-sqlite workspace could not be verified safely.",
    );
  }

  // ==========================================
  // STEP 4: Extracting Data: open Room database & extract data
  // ==========================================

  let intermediateDataRepresentation: any[] = [];
  let sourceResult: { count: number } | null = null;

  let roomExpoSqliteDb: SQLite.SQLiteDatabase | undefined;
  try {
    console.log(
      "[Extracting Data]: Opening working Room database instance via Expo SQLite...",
    );
    roomExpoSqliteDb = await SQLite.openDatabaseAsync(EXPO_TEMP_ROOM_DB_NAME);

    // 1. Fetch Room database version via user_version PRAGMA.
    const versionResult = await roomExpoSqliteDb.getFirstAsync<{
      user_version: number;
    }>("PRAGMA user_version;");
    let dbVersion = versionResult?.user_version;

    if (!dbVersion) {
      dbVersion = 1;
      console.log(
        `[Extracting Data] Room User Version Not Detected, So Assuming Database as: v${dbVersion}`,
      );
    }

    console.log(`[Extracting Data] Room User Version Detected: v${dbVersion}`);

    // 2. Total count of passwords.
    sourceResult = await roomExpoSqliteDb.getFirstAsync<{ count: number }>(
      "SELECT COUNT(*) as count FROM passwords;",
    );

    // 3. Conditional extraction routing.
    if (dbVersion === 2) {
      console.log(
        "[Extracting Data]: Processing Version 2 database structure extraction...",
      );
      intermediateDataRepresentation =
        await extractVersion2Data(roomExpoSqliteDb);
    } else if (dbVersion === 1) {
      console.log(
        "[Extracting Data]: Processing Version 1 database structure extraction...",
      );
      intermediateDataRepresentation =
        await extractVersion1Data(roomExpoSqliteDb);
    } else {
      throw new Error(
        `[Extracting Data]: Unexpected database schema version encountered: v${dbVersion}`,
      );
    }

    console.log(
      "[Extracting Data]: Successfully extracted intermediate data representation.",
    );
  } catch (error) {
    console.error(
      "[Extracting Data]: CRITICAL ERROR ENCOUNTERED DURING DATA REPRESENTATION PHASE:",
      error,
    );

    throw new Error(
      "[Extracting Data]: Migration stopped: Room database's intermediate representation could not be verified safely.",
    );
  } finally {
    await roomExpoSqliteDb?.closeAsync();
  }

  // ==========================================
  // STEP 5: Migrating Data: Room database -> Expo database
  // ==========================================
  console.log("[Migrating Data]: Moving data... it might take a few minutes.");

  let insertStatement: SQLite.SQLiteStatement | undefined;

  try {
    const rowsBefore = await expoDb.getFirstAsync<{ count: number }>(
      "SELECT COUNT(*) as count FROM passwords;",
    );

    await expoDb.withTransactionAsync(async () => {
      insertStatement = await expoDb.prepareAsync(
        `INSERT INTO passwords
          (domain, username, password, notes, url, created_at, updated_at)
        VALUES (
          $domain, $username, $password, $notes, $url,
          COALESCE($created_at, ${SQLITE_NOW_SECONDS}),
          COALESCE($updated_at, $created_at, ${SQLITE_NOW_SECONDS})
        )`,
      );

      for (const data of intermediateDataRepresentation) {
        // Room may have stored timestamps as epoch seconds, epoch milliseconds,
        // or SQLite datetime strings. Normalize them before inserting so the
        // destination INTEGER columns always receive Unix epoch seconds.
        const createdAt = normalizeRoomTimestamp(data.created_at);
        const updatedAt = normalizeRoomTimestamp(data.updated_at);

        await insertStatement.executeAsync({
          $domain: data.domain,
          $username: data.username,
          $password: data.password,
          $notes: data.notes,
          $url: data.url,
          $created_at: createdAt,
          $updated_at: updatedAt,
        });
      }
    });

    const rowsAfter = await expoDb.getFirstAsync<{ count: number }>(
      "SELECT COUNT(*) as count FROM passwords;",
    );

    const sourceRows = sourceResult?.count ?? 0;
    const inserted = (rowsAfter?.count ?? 0) - (rowsBefore?.count ?? 0);

    if (sourceRows !== inserted) {
      throw new Error(
        `[Migrating Data]: Data migration verification failed. Source: ${sourceRows}, Inserted: ${inserted}`,
      );
    }
  } catch (error) {
    console.error(
      "[Migrating Data]: CRITICAL ERROR ENCOUNTERED WHILE MOVING DATA TO EXPO SQLITE:",
      error,
    );

    throw new Error(
      "[Migrating Data]: Migration stopped: Expo database data migration could not be verified safely.",
    );
  } finally {
    await insertStatement?.finalizeAsync();
  }

  console.log("[Migrating Data]: Successfully moved data to Expo SQLite.");
  console.log(
    "[Almost Success]: Data migration succeeded. Cleaning up temporary files...",
  );

  // ==========================================
  // STEP 6: Clean up temporary database files
  // ==========================================

  console.log("[CLEAN MESS]: Cleaning...");

  for (const [fileName, targetName] of filesToProcess) {
    const targetFile = new File(expoSqliteDir.uri, targetName);

    if (targetFile.exists) {
      targetFile.delete();
      console.log(`[CLEAN MESS]: Deleted (' ${fileName} '): ${targetFile.uri}`);
    }
  }

  console.log("[CLEAN MESS]: Cleaned all temporary database files.");

  // ==========================================
  // SUCCESS
  // ==========================================
  const successMessage = "SUCCESSFULLY MIGRATED ALL OLD DATA!";
  console.log(successMessage);
  console.log("task exit 0;");

  return successMessage;
}

/**
 * Convert a legacy Room timestamp into Unix epoch seconds.
 *
 * Accepts seconds, milliseconds, or SQLite/ISO datetime strings. Missing or
 * invalid values return null so the INSERT statement can use the current time.
 */
function normalizeRoomTimestamp(raw: unknown): number | null {
  if (raw === null || raw === undefined || raw === "") {
    return null;
  }

  let seconds: number;

  if (typeof raw === "number") {
    if (!Number.isFinite(raw) || raw < 0) {
      return null;
    }

    seconds = raw >= 100_000_000_000 ? Math.floor(raw / 1000) : Math.floor(raw);
  } else if (typeof raw === "string") {
    const value = raw.trim();
    if (value === "") return null;

    // Numeric strings may represent seconds or milliseconds.
    if (/^\d+(?:\.\d+)?$/.test(value)) {
      const numeric = Number(value);
      if (!Number.isFinite(numeric) || numeric < 0) return null;
      seconds =
        numeric >= 100_000_000_000
          ? Math.floor(numeric / 1000)
          : Math.floor(numeric);
    } else {
      // SQLite CURRENT_TIMESTAMP strings have no timezone; interpret them as UTC.
      const normalized = !/(?:Z|[+-]\d{2}:?\d{2})$/i.test(value)
        ? `${value.replace(" ", "T")}Z`
        : value.replace(" ", "T");
      const milliseconds = Date.parse(normalized);
      if (!Number.isFinite(milliseconds) || milliseconds < 0) return null;
      seconds = Math.floor(milliseconds / 1000);
    }
  } else {
    return null;
  }

  if (
    !Number.isSafeInteger(seconds) ||
    seconds < MIN_TIMESTAMP_SECONDS ||
    seconds > MAX_TIMESTAMP_SECONDS
  ) {
    return null;
  }

  return seconds;
}

async function extractVersion2Data(db: SQLite.SQLiteDatabase) {
  const rows: any[] = await db.getAllAsync(
    `SELECT domain, username, password, notes, url, created_at, updated_at FROM passwords`,
  );
  console.log(`Extracted rows from v2 passwords table: ${rows.length}`);
  return rows;
}

async function extractVersion1Data(db: SQLite.SQLiteDatabase) {
  const dataRows: any[] = await db.getAllAsync(
    `SELECT domain, username, password, notes, created_at, updated_at FROM passwords`,
  );
  console.log(`Extracted rows from v1 passwords table: ${dataRows.length}`);

  const updatedRows = dataRows.map((dataRow) => ({
    ...dataRow,
    url: `https://local.${dataRow.domain}`,
  }));

  console.log("Successfully generated URLs for v1 compatibility.");
  return updatedRows;
}
