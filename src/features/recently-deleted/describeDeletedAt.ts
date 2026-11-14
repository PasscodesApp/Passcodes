import { normalizeTimestamp } from "@/libs/timestamp_unixepoch_utils";

const DAY_SECONDS = 86_400;

/**
 * "Deleted today", "Deleted yesterday", "Deleted 5 days ago" from a stored
 * deletion time.
 *
 * Passwords deleted by older app versions stored a broken value (text instead
 * of a time), so anything unreadable falls back to a neutral label instead of
 * showing "NaN".
 */
export function describeDeletedAt(
  deletedAt: unknown,
  nowMs: number = Date.now(),
): string {
  const seconds = normalizeTimestamp(deletedAt);

  if (seconds === null) {
    return "Deleted earlier";
  }

  const days = Math.floor((nowMs / 1000 - seconds) / DAY_SECONDS);

  if (days <= 0) {
    return "Deleted today";
  }

  if (days === 1) {
    return "Deleted yesterday";
  }

  return `Deleted ${days} days ago`;
}
