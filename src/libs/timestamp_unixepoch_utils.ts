export const CURRENT_TIMESTAMP_SQL = "CAST(strftime('%s', 'now') AS INTEGER)";

/**
 * Backup record timestamps must be Unix epoch seconds represented as JSON
 * numbers. The supported range is 1970-01-01 through 9999-12-31 UTC.
 */
export const MIN_UNIX_TIMESTAMP_SECONDS = 0;
export const MAX_UNIX_TIMESTAMP_SECONDS = 253_402_300_799; // 9999-12-31 23:59:59 UTC

/**
 * Normalize timestamps to Unix epoch seconds.
 *
 * Supports epoch seconds, epoch milliseconds, and legacy SQLite/ISO date strings.
 * Missing or invalid values return null.
 */
export function normalizeTimestamp(raw: unknown): number | null {
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

    if (!value) {
      return null;
    }

    // Numeric strings may represent seconds or milliseconds.
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
      // SQLite CURRENT_TIMESTAMP strings have no timezone; interpret them as UTC.
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
    seconds < MIN_UNIX_TIMESTAMP_SECONDS ||
    seconds > MAX_UNIX_TIMESTAMP_SECONDS
  ) {
    return null;
  }

  return seconds;
}
