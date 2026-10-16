/**
 * Export workflow. No React Native / Expo imports: the repositories and the
 * Save As function are passed in so it can be tested in plain Node.
 * Messages never contain password values.
 */

import type { CategoryRepository } from "@/repositories/CategoryRepository";
import type { PasswordRepository } from "@/repositories/PasswordRepository";

import { serializeGoogleCsv } from "../formats/googleCsv";
import { serializePasscodesJson } from "../formats/passcodesJson";
import type { ExportOutcome, TransferFormat } from "../types";

export interface ExportRepositories {
  passwords: Pick<PasswordRepository, "getAll">;
  categories: Pick<CategoryRepository, "getAll">;
}

export interface SaveRequest {
  fileName: string;
  mimeType: string;
  content: string;
}

/** Resolves once the user's chosen destination was actually written. */
export type SaveFile = (request: SaveRequest) => Promise<"saved" | "cancelled">;

function dateStamp(date: Date): string {
  return date.toISOString().slice(0, 10);
}

/** Never throws. */
export async function exportPasswords(
  format: TransferFormat,
  repositories: ExportRepositories,
  save: SaveFile,
  now: Date = new Date(),
): Promise<ExportOutcome> {
  let passwordRows: Awaited<ReturnType<ExportRepositories["passwords"]["getAll"]>>;

  try {
    passwordRows = await repositories.passwords.getAll();
  } catch {
    return {
      status: "failed",
      message: "Your passwords couldn't be read. Nothing was exported.",
    };
  }

  if (passwordRows.length === 0) {
    return { status: "empty" };
  }

  let request: SaveRequest;

  try {
    if (format === "google-csv") {
      request = {
        fileName: `passcodes-export-${dateStamp(now)}.csv`,
        mimeType: "text/csv",
        content: serializeGoogleCsv(passwordRows),
      };
    } else {
      const categoryRows = await repositories.categories.getAll();

      request = {
        fileName: `passcodes-backup-${dateStamp(now)}.json`,
        mimeType: "application/json",
        content: serializePasscodesJson(passwordRows, categoryRows, now),
      };
    }
  } catch {
    return {
      status: "failed",
      message: "The export file couldn't be created. Nothing was exported.",
    };
  }

  try {
    const result = await save(request);

    return result === "saved"
      ? {
          status: "saved",
          count: passwordRows.length,
          fileName: request.fileName,
        }
      : { status: "cancelled" };
  } catch {
    return {
      status: "failed",
      message: "The file couldn't be saved. Nothing was exported.",
    };
  }
}
