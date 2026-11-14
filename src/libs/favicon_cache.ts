/**
 * Persistent website-icon cache.
 *
 * An icon is downloaded once and kept as a file in the app's own storage, so
 * it survives app restarts and is reused for many days (a site's icon rarely
 * changes). Rules:
 *
 * - found icon: reused for 30 days, then quietly refreshed once;
 * - failed lookup: not retried for 1 day (and retried at once if the user
 *   picks another provider);
 * - never downloaded while the provider is "off" (cached icons still show);
 * - at most a few downloads at a time, and one per host.
 *
 * Only the host name is ever sent, and only to the chosen provider.
 */

import { Directory, File, Paths } from "expo-file-system";
import AsyncStorage from "expo-sqlite/kv-store";

import { faviconUrl, type FaviconProvider } from "./favicon";

const DAY_MS = 86_400_000;
const FRESH_FOR_MS = 30 * DAY_MS;
const RETRY_FAILED_AFTER_MS = 1 * DAY_MS;
const MAX_PARALLEL_DOWNLOADS = 4;

type Meta = { at: number; ok: boolean; provider: FaviconProvider };

function metaKey(host: string) {
  return `favicon_meta_${host}`;
}

function iconFile(host: string): File {
  return new File(new Directory(Paths.document, "favicons"), `${host}.png`);
}

function readMeta(host: string): Meta | null {
  try {
    const raw = AsyncStorage.getItemSync(metaKey(host));
    return raw ? (JSON.parse(raw) as Meta) : null;
  } catch {
    return null;
  }
}

function writeMeta(host: string, meta: Meta) {
  AsyncStorage.setItemAsync(metaKey(host), JSON.stringify(meta)).catch(
    () => undefined,
  );
}

/** What is stored on the device right now (synchronous, no network). */
export function readCachedFavicon(
  host: string,
  provider: FaviconProvider,
): {
  uri: string | null;
  /** `true` when a download should be attempted for this provider. */
  needsFetch: boolean;
} {
  const meta = readMeta(host);
  const file = iconFile(host);
  const hasFile = meta?.ok === true && file.exists;
  const age = meta ? Date.now() - meta.at : Infinity;

  let needsFetch: boolean;

  if (hasFile) {
    needsFetch = age > FRESH_FOR_MS; // refresh an old icon once
  } else if (!meta) {
    needsFetch = true; // never tried
  } else {
    // Failed: try again tomorrow, or at once with a different provider.
    needsFetch = age > RETRY_FAILED_AFTER_MS || meta.provider !== provider;
  }

  return { uri: hasFile ? file.uri : null, needsFetch };
}

/**
 * Downloads (or refreshes) the icon. Resolves with the local file address, or
 * `null` when it could not be fetched. Never throws.
 */
export function fetchFavicon(
  host: string,
  provider: FaviconProvider,
): Promise<string | null> {
  const source = faviconUrl(host, provider);

  if (source === null) {
    return Promise.resolve(null);
  }

  const existing = inFlight.get(host);

  if (existing) {
    return existing;
  }

  const job = enqueue(async () => {
    try {
      const folder = new Directory(Paths.document, "favicons");

      if (!folder.exists) {
        folder.create();
      }

      const target = iconFile(host);
      const temporary = new File(folder, `${host}.download`);

      if (temporary.exists) {
        await temporary.delete();
      }

      await File.downloadFileAsync(source, temporary);

      // An empty body is a failed download, never an icon.
      if (!temporary.exists || temporary.size === 0) {
        throw new Error("Empty icon.");
      }

      if (target.exists) {
        await target.delete();
      }

      await temporary.move(target);

      writeMeta(host, { at: Date.now(), ok: true, provider });

      return target.uri;
    } catch {
      // Keep an older icon if we have one: a failed refresh must not lose it.
      const kept = iconFile(host);

      if (kept.exists && readMeta(host)?.ok) {
        writeMeta(host, { at: Date.now(), ok: true, provider });
        return kept.uri;
      }

      writeMeta(host, { at: Date.now(), ok: false, provider });

      return null;
    } finally {
      inFlight.delete(host);
    }
  });

  inFlight.set(host, job);

  return job;
}

/**
 * The stored image could not be displayed (damaged, or the provider answered
 * with something that isn't an image): forget it so it is not shown again.
 */
export async function discardFavicon(
  host: string,
  provider: FaviconProvider,
): Promise<void> {
  try {
    const file = iconFile(host);

    if (file.exists) {
      await file.delete();
    }
  } catch {
    // Best effort.
  }

  writeMeta(host, { at: Date.now(), ok: false, provider });
}

// ---- small download queue ------------------------------------------------

const inFlight = new Map<string, Promise<string | null>>();
const waiting: Array<() => void> = [];
let running = 0;

function enqueue<T>(task: () => Promise<T>): Promise<T> {
  return new Promise<T>((resolve) => {
    const start = () => {
      running += 1;

      task()
        .then(resolve)
        .finally(() => {
          running -= 1;
          waiting.shift()?.();
        });
    };

    if (running < MAX_PARALLEL_DOWNLOADS) {
      start();
    } else {
      waiting.push(start);
    }
  });
}
