/**
 * Native (Android / iOS) export of the user's raw SQLite database file.
 *
 * The original database is only ever READ:
 *
 * 1. The .db file and its side files (-wal = committed changes not yet merged
 *    into the .db, -journal = rollback journal) are copied into a private
 *    temporary folder. The copy is verified to be stable (sizes / timestamps
 *    unchanged while copying) and retried otherwise. The -shm file is a
 *    rebuildable index of the -wal file, so it is intentionally not needed.
 * 2. The COPY is opened, its write-ahead log is merged into it
 *    (`wal_checkpoint(TRUNCATE)`) and it is switched to a single-file mode,
 *    so the exported .db alone holds every committed row.
 * 3. The user picks a destination with the system Save dialog.
 * 4. The temporary folder (which holds passwords) is always deleted.
 *
 * Nothing is deleted, reset, checkpointed or rewritten in the original.
 * If the copy cannot be opened (e.g. a damaged file), the raw .db and -wal
 * files are saved as they are instead, so support can still recover them.
 */

import {
  errorCodes,
  isErrorWithCode,
  saveDocuments,
} from "@react-native-documents/picker";
import { Directory, File, Paths } from "expo-file-system";
import * as SQLite from "expo-sqlite";

import type { ExportDatabaseResult } from "./exportDatabase.types";

const SIDE_FILE_SUFFIXES = ["-wal", "-journal"] as const;
const COPY_ATTEMPTS = 3;
const MIME_TYPE = "application/vnd.sqlite3";

function signature(file: File): string {
  return file.exists ? `${file.size}:${file.modificationTime ?? 0}` : "missing";
}

function stamp(date: Date): string {
  return date.toISOString().slice(0, 19).replace(/[-:T]/g, "");
}

/** `file://` URI -> plain path, as `openDatabaseAsync` expects. */
function toPath(uri: string): string {
  return decodeURIComponent(uri.replace(/^file:\/\//, ""));
}

/**
 * Copies the database and its side files into `workDir`, retrying if any
 * source file changed while it was being copied.
 */
async function copyConsistently(
  sourceDir: Directory,
  databaseName: string,
  workDir: Directory,
  exportName: string,
): Promise<void> {
  const pairs: [File, File][] = [
    [new File(sourceDir, databaseName), new File(workDir, exportName)],
    ...SIDE_FILE_SUFFIXES.map(
      (suffix): [File, File] => [
        new File(sourceDir, `${databaseName}${suffix}`),
        new File(workDir, `${exportName}${suffix}`),
      ],
    ),
  ];

  const [mainPair] = pairs;

  if (!mainPair || !mainPair[0].exists) {
    throw new Error("The database file was not found.");
  }

  for (let attempt = 1; attempt <= COPY_ATTEMPTS; attempt += 1) {
    const before = pairs.map(([source]) => signature(source)).join("|");

    for (const [source, target] of pairs) {
      if (target.exists) {
        await target.delete();
      }

      // `copy` is asynchronous: it must finish before the next step.
      if (source.exists) {
        await source.copy(target);
      }
    }

    const after = pairs.map(([source]) => signature(source)).join("|");

    if (before === after) {
      return;
    }
  }

  throw new Error("The database kept changing while it was being copied.");
}

/** Merges the copy's write-ahead log into the copy and makes it one file. */
async function makeSelfContained(
  workDir: Directory,
  exportName: string,
): Promise<void> {
  const copy = await SQLite.openDatabaseAsync(
    exportName,
    undefined,
    toPath(workDir.uri),
  );

  try {
    await copy.execAsync("PRAGMA wal_checkpoint(TRUNCATE);");
    await copy.execAsync("PRAGMA journal_mode = DELETE;");
  } finally {
    await copy.closeAsync();
  }
}

function isCancellation(error: unknown): boolean {
  return isErrorWithCode(error) && error.code === errorCodes.OPERATION_CANCELED;
}

export async function exportDatabase(
  databaseName: string,
): Promise<ExportDatabaseResult> {
  const sourceDir = new Directory(Paths.document, "SQLite");
  const exportName = `passcodes-backup-${stamp(new Date())}.db`;
  const workDir = new Directory(Paths.cache, `passcodes-export-${Date.now()}`);

  workDir.create();

  try {
    await copyConsistently(sourceDir, databaseName, workDir, exportName);

    let includesWalFile = false;

    try {
      await makeSelfContained(workDir, exportName);
    } catch (error) {
      // Could not merge the log (damaged file?). Save the raw files as they
      // are rather than refusing: they contain everything that exists.
      console.warn("Database export: saving raw files instead.", error);
      includesWalFile = new File(workDir, `${exportName}-wal`).exists;
    }

    const sourceUris = [new File(workDir, exportName).uri];

    if (includesWalFile) {
      sourceUris.push(new File(workDir, `${exportName}-wal`).uri);
    }

    const [first] = await saveDocuments({
      sourceUris,
      // iOS: copy instead of move; the temporary folder is deleted below.
      copy: true,
      mimeType: MIME_TYPE,
      ...(sourceUris.length === 1 ? { fileName: exportName } : {}),
    });

    if (first?.error) {
      throw new Error("The file couldn't be written.");
    }

    return { status: "saved", includesWalFile };
  } catch (error) {
    if (isCancellation(error)) {
      return { status: "cancelled" };
    }

    throw error;
  } finally {
    try {
      await workDir.delete();
    } catch {
      // Best effort: it lives in the cache folder anyway.
    }
  }
}
