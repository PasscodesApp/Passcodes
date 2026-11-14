const DAY_SECONDS = 86_400;

/**
 * "Deleted today", "Deleted yesterday", "Deleted 5 days ago" from a Unix
 * timestamp in seconds. Pure, so it is easy to reason about.
 */
export function describeDeletedAt(
  deletedAtSeconds: number,
  nowMs: number = Date.now(),
): string {
  const days = Math.floor((nowMs / 1000 - deletedAtSeconds) / DAY_SECONDS);

  if (days <= 0) {
    return "Deleted today";
  }

  if (days === 1) {
    return "Deleted yesterday";
  }

  return `Deleted ${days} days ago`;
}
