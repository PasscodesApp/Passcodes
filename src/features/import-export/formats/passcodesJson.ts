/**
 * Passcodes JSON backup, format version 1.
 *
 * Pure functions (no React Native / Expo imports) so they run in plain Node.
 *
 * ```json
 * {
 *   "format": "passcodes",
 *   "version": 1,
 *   "exported-at": "2026-10-15T16:00:00.000Z",
 *   "counts": { "passwords": 1, "categories": 1 },
 *   "data": {
 *     "passwords": [
 *       { "_id": 1, "domain": "...", "url": null, "username": "...",
 *         "password": "...", "notes": null, "category-id": 2,
 *         "created-at": 1767268800, "updated-at": 1769947200 }
 *     ],
 *     "categories": [
 *       { "_id": 2, "name": "...", "created-at": 1767268800, "updated-at": 1769947200 }
 *     ]
 *   }
 * }
 * ```
 *
 * Rules:
 * - Every table is an array under `data`, every item has an `_id` that is
 *   unique across the WHOLE file (passwords are numbered 1..n, categories
 *   continue at n+1). `_id`s are file-local: they are never database ids.
 * - References use the target's `_id` (`category-id`), `null` = Unsorted.
 * - Record timestamps are Unix epoch seconds represented as JSON numbers.
 *   Missing timestamps remain null; invalid timestamps are reported as warnings.
 * - New tables = new keys under `data` plus a `counts` entry. Additive
 *   changes keep `version`; breaking changes increase it.
 * - The importer is lenient about cosmetic problems (bad timestamp, dangling
 *   category reference: the password is still imported and a warning is
 *   added) and strict about structure (format, version, counts).
 */

import {
  MAX_UNIX_TIMESTAMP_SECONDS,
  MIN_UNIX_TIMESTAMP_SECONDS,
} from "@/libs/timestamp_unixepoch_utils";
import { UNCATEGORIZED_LABEL } from "@/repositories/CategoryRepository";
import type {
  CandidateCategory,
  CandidatePassword,
  FailureReason,
  ParseResult,
  RecordFailure,
} from "../types";

export const PASSCODES_FORMAT = "passcodes";

// the format will be change v1 after stablity of feature...
// and, also make v0 invalid in code. refer `parsePasscodesJson()`
export const PASSCODES_JSON_VERSION = 0;

export interface PasswordExportRow {
  id: number;
  domain: string;
  username: string;
  password: string;
  notes: string | null;
  url: string | null;
  createdAt: number | null;
  updatedAt: number | null;
  categoryId: number | null;
}

export interface CategoryExportRow {
  id: number;
  name: string;
  createdAt: number | null;
  updatedAt: number | null;
}

export function serializePasscodesJson(
  passwords: readonly PasswordExportRow[],
  categories: readonly CategoryExportRow[],
  exportedAt: Date,
): string {
  const fileIdByCategoryId = new Map<number, number>();

  categories.forEach((category, index) => {
    fileIdByCategoryId.set(category.id, passwords.length + index + 1);
  });

  const document = {
    format: PASSCODES_FORMAT,
    version: PASSCODES_JSON_VERSION,
    "exported-at": exportedAt.toISOString(),
    counts: { passwords: passwords.length, categories: categories.length },
    data: {
      passwords: passwords.map((row, index) => ({
        _id: index + 1,
        domain: row.domain,
        url: row.url,
        username: row.username,
        password: row.password,
        notes: row.notes,
        "category-id":
          row.categoryId === null
            ? null
            : (fileIdByCategoryId.get(row.categoryId) ?? null),
        "created-at": row.createdAt,
        "updated-at": row.updatedAt,
      })),
      categories: categories.map((row, index) => ({
        _id: passwords.length + index + 1,
        name: row.name,
        "created-at": row.createdAt,
        "updated-at": row.updatedAt,
      })),
    },
  };

  return JSON.stringify(document, null, 2);
}

// ---------------------------------------------------------------------------
// Parsing & validation
// ---------------------------------------------------------------------------

const KNOWN_TABLES = ["passwords", "categories"];

function asRecord(value: unknown): Record<string, unknown> | null {
  return typeof value === "object" && value !== null && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

function isPositiveInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value > 0;
}

type Read<T> = { ok: true; value: T } | { ok: false; reason: FailureReason };

/** Absent / null -> null, a string -> the string, anything else -> invalid. */
function readOptionalString(raw: unknown): Read<string | null> {
  if (raw === undefined || raw === null) {
    return { ok: true, value: null };
  }

  if (typeof raw !== "string") {
    return { ok: false, reason: "invalid-field" };
  }

  return { ok: true, value: raw.trim() === "" ? null : raw };
}

/**
 * Read a Unix epoch timestamp in seconds.
 *
 * Missing/null values remain null. Invalid values (including strings,
 * fractional seconds, milliseconds, and dates outside the supported range)
 * become null and increment the warning counter.
 */
function readTimestamp(raw: unknown, counter: { bad: number }): number | null {
  if (raw === undefined || raw === null) {
    return null;
  }

  if (
    typeof raw !== "number" ||
    !Number.isSafeInteger(raw) ||
    raw < MIN_UNIX_TIMESTAMP_SECONDS ||
    raw > MAX_UNIX_TIMESTAMP_SECONDS
  ) {
    counter.bad += 1;
    return null;
  }

  return raw;
}

export function parsePasscodesJson(text: string): ParseResult {
  const content = text.replace(/^﻿/, "");

  if (content.trim() === "") {
    return { ok: false, error: "The file is empty." };
  }

  let root: unknown;

  try {
    root = JSON.parse(content);
  } catch {
    return { ok: false, error: "The file isn't valid JSON." };
  }

  const document = asRecord(root);

  if (!document || document["format"] !== PASSCODES_FORMAT) {
    return { ok: false, error: "This file isn't a Passcodes backup." };
  }

  const version = document["version"];

  // TODO: we will make version < 1. zero will be invalid.
  // once we have stable import-export version.
  if (
    typeof version !== "number" ||
    !Number.isInteger(version) ||
    version < 0
  ) {
    return { ok: false, error: "The backup's version number is invalid." };
  }

  if (version > PASSCODES_JSON_VERSION) {
    return {
      ok: false,
      error: `This backup uses a newer format (version ${version}) than this app understands (version ${PASSCODES_JSON_VERSION}). Update Passcodes and try again.`,
    };
  }

  const data = asRecord(document["data"]);
  const counts = asRecord(document["counts"]);

  if (!data || !counts) {
    return {
      ok: false,
      error: "The backup is missing its data or item counts.",
    };
  }

  const rawPasswords = data["passwords"];
  const rawCategories = data["categories"];

  if (!Array.isArray(rawPasswords) || !Array.isArray(rawCategories)) {
    return {
      ok: false,
      error: "The backup is missing its passwords or categories list.",
    };
  }

  if (
    counts["passwords"] !== rawPasswords.length ||
    counts["categories"] !== rawCategories.length
  ) {
    return {
      ok: false,
      error:
        "The backup's item counts don't match its contents. The file may be incomplete or edited.",
    };
  }

  const warnings: string[] = [];
  const failures: RecordFailure[] = [];
  const passwords: CandidatePassword[] = [];
  const categories: CandidateCategory[] = [];
  const usedIds = new Set<number>();
  const badTimestamps = { bad: 0 };

  const unknownTables = Object.keys(data).filter(
    (key) => !KNOWN_TABLES.includes(key),
  ).length;

  if (unknownTables > 0) {
    warnings.push(
      `This backup contains ${unknownTables} section(s) this version of Passcodes can't restore. They were ignored.`,
    );
  }

  function claimId(raw: unknown): Read<number> {
    if (!isPositiveInteger(raw)) {
      return { ok: false, reason: "invalid-id" };
    }

    if (usedIds.has(raw)) {
      return { ok: false, reason: "duplicate-id" };
    }

    usedIds.add(raw);
    return { ok: true, value: raw };
  }

  rawPasswords.forEach((raw: unknown, index) => {
    const position = index + 1;
    const fail = (reason: FailureReason) =>
      failures.push({ table: "passwords", position, reason });

    const record = asRecord(raw);

    if (!record) {
      fail("invalid-record");
      return;
    }

    const domain = record["domain"];
    const username = record["username"];
    const password = record["password"];

    if (
      typeof domain !== "string" ||
      typeof username !== "string" ||
      typeof password !== "string"
    ) {
      fail("invalid-field");
      return;
    }

    if (domain.trim() === "") {
      fail("missing-domain");
      return;
    }

    if (username.trim() === "") {
      fail("missing-username");
      return;
    }

    if (password === "") {
      fail("missing-password");
      return;
    }

    const url = readOptionalString(record["url"]);
    const notes = readOptionalString(record["notes"]);
    const categoryRaw = record["category-id"];

    if (
      !url.ok ||
      !notes.ok ||
      (categoryRaw !== undefined &&
        categoryRaw !== null &&
        !isPositiveInteger(categoryRaw))
    ) {
      fail("invalid-field");
      return;
    }

    const id = claimId(record["_id"]);

    if (!id.ok) {
      fail(id.reason);
      return;
    }

    passwords.push({
      domain,
      url: url.value,
      username,
      password,
      notes: notes.value,
      categorySourceId: isPositiveInteger(categoryRaw) ? categoryRaw : null,
      createdAt: readTimestamp(record["created-at"], badTimestamps),
      updatedAt: readTimestamp(record["updated-at"], badTimestamps),
    });
  });

  rawCategories.forEach((raw: unknown, index) => {
    const position = index + 1;
    const fail = (reason: FailureReason) =>
      failures.push({ table: "categories", position, reason });

    const record = asRecord(raw);

    if (!record) {
      fail("invalid-record");
      return;
    }

    const name = record["name"];

    if (typeof name !== "string" || name.trim() === "") {
      fail("invalid-field");
      return;
    }

    const id = claimId(record["_id"]);

    if (!id.ok) {
      fail(id.reason);
      return;
    }

    categories.push({
      sourceId: id.value,
      name,
      createdAt: readTimestamp(record["created-at"], badTimestamps),
      updatedAt: readTimestamp(record["updated-at"], badTimestamps),
    });
  });

  // A password that points at a category that is not in the file is still a
  // valid password: import it as uncategorized instead of losing it.
  const knownCategoryIds = new Set(categories.map((c) => c.sourceId));
  let danglingReferences = 0;

  for (const candidate of passwords) {
    if (
      candidate.categorySourceId !== null &&
      !knownCategoryIds.has(candidate.categorySourceId)
    ) {
      candidate.categorySourceId = null;
      danglingReferences += 1;
    }
  }

  if (danglingReferences > 0) {
    warnings.push(
      `${danglingReferences} password(s) referred to a category that isn't in the file and will be imported as ${UNCATEGORIZED_LABEL}.`,
    );
  }

  if (badTimestamps.bad > 0) {
    warnings.push(
      `${badTimestamps.bad} timestamp(s) were invalid and need a fallback during import.`,
    );
  }

  return {
    ok: true,
    data: {
      passwords,
      categories,
      failures,
      warnings,
      detected: rawPasswords.length,
    },
  };
}
