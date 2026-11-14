/**
 * Application IDs: the Android package names and / or iOS bundle IDs a
 * password belongs to (e.g. `com.instagram.android`). Stored in one text
 * column, comma-separated, and used by Autofill to match an app.
 *
 * Pure functions (no React Native / Expo imports).
 */

/** Dot-separated name that starts with a letter: `com.example.app`. */
const APPLICATION_ID = /^[A-Za-z][A-Za-z0-9_-]*(\.[A-Za-z0-9_-]+)+$/;

export type ParsedApplicationIds = {
  /** What to store: IDs joined with "," (de-duplicated), or `null` if none. */
  value: string | null;
  ids: string[];
  /** Entries that are not valid application IDs. Save only when empty. */
  invalid: string[];
};

/**
 * Reads what the user typed (separated by commas, spaces or new lines) or a
 * stored value. Empty entries and repeats are dropped.
 */
export function parseApplicationIds(
  input: string | null | undefined,
): ParsedApplicationIds {
  const ids: string[] = [];
  const invalid: string[] = [];

  for (const part of (input ?? "").split(/[\s,;]+/)) {
    if (part === "" || ids.includes(part) || invalid.includes(part)) {
      continue;
    }

    (APPLICATION_ID.test(part) ? ids : invalid).push(part);
  }

  return { value: ids.length > 0 ? ids.join(",") : null, ids, invalid };
}

/** Stored value -> text for the form field ("a.b.c, d.e.f"). */
export function formatApplicationIds(value: string | null | undefined): string {
  return parseApplicationIds(value).ids.join(", ");
}

/** Message for the first invalid entry, or `null` when all are valid. */
export function describeInvalidApplicationIds(
  parsed: ParsedApplicationIds,
): string | null {
  const [first] = parsed.invalid;

  return first === undefined
    ? null
    : `"${first}" isn't a valid application ID. Use names like com.example.app, separated by commas.`;
}
