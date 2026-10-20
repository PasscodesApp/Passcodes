import Config from "@/config";
import * as SQLite from "expo-sqlite";

const MIN_TIMESTAMP_SECONDS = 0;
const MAX_TIMESTAMP_SECONDS = 253_402_300_799;
const CURRENT_TIMESTAMP_SQL = "CAST(strftime('%s', 'now') AS INTEGER)";

/**
 * Normalize timestamps from the old test database to Unix epoch seconds.
 * Supports epoch seconds, epoch milliseconds, and legacy SQLite/ISO date strings.
 * Invalid or missing values return null so SQLite can apply the current-time fallback.
 */
function normalizeRoomTimestamp(raw: unknown): number | null {
  if (raw === null || raw === undefined) {
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

    if (!value) {
      return null;
    }

    // Numeric strings may come from SQLite affinity or an older export.
    if (/^\d+(?:\.\d+)?$/.test(value)) {
      const numeric = Number(value);
      if (!Number.isFinite(numeric) || numeric < 0) {
        return null;
      }

      seconds =
        numeric >= 100_000_000_000
          ? Math.floor(numeric / 1000)
          : Math.floor(numeric);
    } else {
      // SQLite CURRENT_TIMESTAMP has no timezone suffix and represents UTC.
      const hasTimezone = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(value);
      const normalized = value.includes(" ") ? value.replace(" ", "T") : value;
      const dateText = hasTimezone ? normalized : `${normalized}Z`;
      const milliseconds = Date.parse(dateText);

      if (!Number.isFinite(milliseconds) || milliseconds < 0) {
        return null;
      }

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
          const createdAt = normalizeRoomTimestamp(row.created_at);
          const updatedAt = normalizeRoomTimestamp(row.updated_at);

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
