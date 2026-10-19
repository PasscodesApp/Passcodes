/**
 * Native file operations, using `@react-native-documents/picker`:
 *  - `pick()` + `keepLocalCopy()` to read a file the user selected (works with
 *    Android `content://` URIs, no storage permission needed),
 *  - `saveDocuments()` for the system Save As dialog.
 *
 * This package contains native code: it does not run in Expo Go and needs a
 * development build / native rebuild.
 *
 * Never log file contents or URIs here.
 */

import {
  errorCodes,
  isErrorWithCode,
  keepLocalCopy,
  pick,
  saveDocuments,
  types,
  type DocumentPickerResponse,
} from "@react-native-documents/picker";
import { File, Paths } from "expo-file-system";

import type { SaveRequest } from "./services/exportService";
import type { TransferFormat } from "./types";

function isCancellation(error: unknown): boolean {
  return isErrorWithCode(error) && error.code === errorCodes.OPERATION_CANCELED;
}

/**
 * Types offered in the picker. They are deliberately lenient: file managers
 * and cloud apps often label CSV/JSON files as plain text, Excel or a generic
 * binary type, and a strict filter would hide them. The parsers validate the
 * content anyway.
 */
function pickerTypes(format: TransferFormat) {
  return format === "google-csv"
    ? [types.csv, types.plainText, "application/vnd.ms-excel"]
    : [types.json, types.plainText, "application/octet-stream"];
}

function safeFileName(name: string | null): string {
  const cleaned = (name ?? "").replace(/[^\w.\- ]/g, "_").trim();
  return cleaned === "" ? "import-file" : cleaned;
}

/**
 * Opens the file picker and returns the file's text, or `null` if the user
 * cancelled. Throws if the file can't be read.
 *
 * The picked file is copied into the app cache, read, and the copy is deleted
 * again so no credentials are left behind.
 */
export async function pickImportText(
  format: TransferFormat,
): Promise<string | null> {
  let picked: DocumentPickerResponse;

  try {
    const results = await pick({
      type: pickerTypes(format),
      allowMultiSelection: false,
    });

    picked = results[0];
  } catch (error) {
    if (isCancellation(error)) {
      return null;
    }

    throw error;
  }

  if (picked.error) {
    throw new Error("The file's details couldn't be read.");
  }

  const [copy] = await keepLocalCopy({
    files: [{ uri: picked.uri, fileName: safeFileName(picked.name) }],
    destination: "cachesDirectory",
  });

  if (copy.status !== "success") {
    throw new Error("The file couldn't be copied for reading.");
  }

  const localUri = copy.localUri.startsWith("file://")
    ? copy.localUri
    : `file://${copy.localUri}`;
  const localFile = new File(localUri);

  try {
    return await localFile.text();
  } finally {
    try {
      localFile.delete();
    } catch {
      // Best effort: it is in the cache directory anyway.
    }
  }
}

/**
 * Writes the content to a temporary cache file, opens the Save As dialog and
 * always deletes the temporary file afterwards.
 *
 * Resolves `"saved"` only after the system reports the file as written,
 * `"cancelled"` if the user closed the dialog, and throws otherwise.
 */
export async function saveTextFile({
  fileName,
  mimeType,
  content,
}: SaveRequest): Promise<"saved" | "cancelled"> {
  const temporary = new File(Paths.cache, fileName);

  if (temporary.exists) {
    temporary.delete();
  }

  temporary.write(content);

  try {
    const [response] = await saveDocuments({
      sourceUris: [temporary.uri],
      // iOS: copy instead of move, the temporary file is deleted below.
      copy: true,
      mimeType,
      fileName,
    });

    if (response.error) {
      throw new Error("The file couldn't be written.");
    }

    return "saved";
  } catch (error) {
    if (isCancellation(error)) {
      return "cancelled";
    }

    throw error;
  } finally {
    try {
      temporary.delete();
    } catch {
      // Best effort.
    }
  }
}
