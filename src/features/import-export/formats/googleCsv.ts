/**
 * Google Password Manager CSV (`name,url,username,password,note`).
 *
 * Pure functions (no React Native / Expo imports) so they run in plain Node.
 *
 * Field mapping:
 *   name     <-> domain
 *   url      <-> url        (the `https://local.<domain>` placeholder used
 *                            by Passcodes for "no URL" is written on export
 *                            and turned back into `null` on import)
 *   username <-> username
 *   password <-> password
 *   note     <-> notes
 *
 * The CSV cannot carry categories or timestamps; they are not exported and
 * imported rows get the import time and no category.
 */

import Papa from "papaparse";

import type {
  CandidatePassword,
  FailureReason,
  ParseResult,
  RecordFailure,
} from "../types";

export const GOOGLE_CSV_COLUMNS = [
  "name",
  "url",
  "username",
  "password",
  "note",
] as const;

/** `url` and `note` are optional, a row without them is still a password. */
const REQUIRED_COLUMNS = ["name", "username", "password"] as const;

export interface GoogleCsvExportRow {
  domain: string;
  url: string | null;
  username: string;
  password: string;
  notes: string | null;
}

/** Stable placeholder Passcodes uses for entries without a real URL. */
export function placeholderUrl(domain: string): string {
  return `https://local.${domain}`;
}

export function serializeGoogleCsv(
  rows: readonly GoogleCsvExportRow[],
): string {
  // Papa quotes a field only when needed (comma, quote, newline, edge
  // whitespace) and doubles embedded quotes. `escapeFormulae` stays off on
  // purpose: it would rewrite passwords that start with `=`, `+`, `-` or `@`.
  return Papa.unparse(
    {
      fields: [...GOOGLE_CSV_COLUMNS],
      data: rows.map((row) => [
        row.domain,
        row.url || placeholderUrl(row.domain),
        row.username,
        row.password,
        row.notes ?? "",
      ]),
    },
    { newline: "\r\n" },
  );
}

/** "https://www.Example.com/login" -> "example.com", "" when there is no host. */
function domainFromUrl(url: string): string {
  const match = /^[a-z][a-z0-9+.-]*:\/\/(?:[^@/?#]*@)?([^/?#:]+)/i.exec(url);
  const host = match?.[1];

  return host ? host.toLowerCase().replace(/^www\./, "") : "";
}

export function parseGoogleCsv(text: string): ParseResult {
  // Papa strips a leading BOM itself, but only for string input it controls.
  const content = text.replace(/^﻿/, "");

  if (content.trim() === "") {
    return { ok: false, error: "The file is empty." };
  }

  const headers: string[] = [];

  const result = Papa.parse<Record<string, string>>(content, {
    header: true,
    delimiter: ",",
    // Credentials must stay strings ("007" must not become 7).
    dynamicTyping: false,
    // "greedy" also drops lines that contain only separators/whitespace.
    skipEmptyLines: "greedy",
    transformHeader: (header: string) => {
      const normalized = header.trim().toLowerCase();
      headers.push(normalized);
      return normalized;
    },
  });

  if (new Set(headers).size !== headers.length) {
    return {
      ok: false,
      error: "The file has duplicate column names, so it can't be read safely.",
    };
  }

  const missing = REQUIRED_COLUMNS.filter(
    (column) => !headers.includes(column),
  );

  if (missing.length > 0) {
    return {
      ok: false,
      error: `This doesn't look like a Google Password Manager CSV. Missing column(s): ${missing.join(", ")}.`,
    };
  }

  const rowErrors = new Map<number, FailureReason>();

  for (const error of result.errors) {
    if (error.code === "MissingQuotes") {
      // An unclosed quote swallows the rest of the file into one field.
      return {
        ok: false,
        error:
          "The file has a quote that is never closed, so it can't be read reliably.",
      };
    }

    if (error.row !== undefined) {
      rowErrors.set(
        error.row,
        error.type === "FieldMismatch" ? "column-mismatch" : "malformed-row",
      );
    }
  }

  const passwords: CandidatePassword[] = [];
  const failures: RecordFailure[] = [];

  result.data.forEach((row, index) => {
    const position = index + 1;
    const rowError = rowErrors.get(index);

    if (rowError) {
      failures.push({ table: "passwords", position, reason: rowError });
      return;
    }

    const url = (row["url"] ?? "").trim();
    const domain = (row["name"] ?? "").trim() || domainFromUrl(url);
    const username = (row["username"] ?? "").trim();
    const password = row["password"] ?? "";
    const note = row["note"] ?? "";

    const reason: FailureReason | null =
      domain === ""
        ? "missing-domain"
        : username === ""
          ? "missing-username"
          : password === ""
            ? "missing-password"
            : null;

    if (reason) {
      failures.push({ table: "passwords", position, reason });
      return;
    }

    const isPlaceholder =
      url.toLowerCase() === placeholderUrl(domain).toLowerCase();

    passwords.push({
      domain,
      url: url === "" || isPlaceholder ? null : url,
      username,
      password,
      notes: note === "" ? null : note,
      categorySourceId: null,
      createdAt: null,
      updatedAt: null,
    });
  });

  return {
    ok: true,
    data: {
      passwords,
      categories: [],
      failures,
      warnings: [],
      detected: passwords.length + failures.length,
    },
  };
}
