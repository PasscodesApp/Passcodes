/**
 * Shared types for the import/export feature.
 *
 * Pure types and constants only (no React Native / Expo imports), so the
 * format, analysis and service modules can run in plain Node for tests.
 */

// ---------------------------------------------------------------------------
// Formats
// ---------------------------------------------------------------------------

/** Formats the Import and Export screens offer. */
export type TransferFormat = "google-csv" | "passcodes-json";

export interface TransferFormatOption {
  value: TransferFormat;
  label: string;
  description: string;
}

export const TRANSFER_FORMAT_OPTIONS: readonly TransferFormatOption[] = [
  {
    value: "google-csv",
    label: "Google CSV",
    description:
      "Google Password Manager CSV (name, url, username, password, note). Notes and categories are limited to what the CSV can hold; timestamps are not kept.",
  },
  {
    value: "passcodes-json",
    label: "Passcodes JSON",
    description:
      "Passcodes backup. Keeps notes, categories and timestamps. Not encrypted.",
  },
];

// ---------------------------------------------------------------------------
// Parsed (format independent) import data
// ---------------------------------------------------------------------------

/** One password as read from a file, before it is compared with the database. */
export interface CandidatePassword {
  domain: string;
  url: string | null;
  username: string;
  password: string;
  notes: string | null;
  /** `_id` of the category in the same file. `null` = Uncategorized. */
  categorySourceId: number | null;
  /** Raw stored timestamp, or `null` to use the import time. */
  createdAt: number | null;
  updatedAt: number | null;
}

export interface CandidateCategory {
  /** `_id` of the category in the file. */
  sourceId: number;
  name: string;
  createdAt: number | null;
  updatedAt: number | null;
}

/**
 * Why a record was rejected. These are fixed codes, never values from the
 * file, so they are safe to show and log.
 */
export type FailureReason =
  | "missing-domain"
  | "missing-username"
  | "missing-password"
  | "column-mismatch"
  | "malformed-row"
  | "invalid-record"
  | "invalid-field"
  | "invalid-id"
  | "duplicate-id"
  | "save-failed";

export const FAILURE_REASON_TEXT: Record<FailureReason, string> = {
  "missing-domain": "Missing a domain or site name",
  "missing-username": "Missing a username",
  "missing-password": "Missing a password",
  "column-mismatch": "Wrong number of columns",
  "malformed-row": "Malformed quoting",
  "invalid-record": "Not a valid record",
  "invalid-field": "A field has the wrong type",
  "invalid-id": "Missing or invalid _id",
  "duplicate-id": "Repeats an _id used earlier in the file",
  "save-failed": "Could not be saved",
};

export interface RecordFailure {
  table: "passwords" | "categories";
  /** 1-based position of the record (CSV data row / JSON array index + 1). */
  position: number;
  reason: FailureReason;
}

export interface ParsedImport {
  passwords: CandidatePassword[];
  categories: CandidateCategory[];
  failures: RecordFailure[];
  /** Safe, non-sensitive notes about things that were adjusted. */
  warnings: string[];
  /** Password records found in the file, valid or not. */
  detected: number;
}

/** `ok: false` is a file-level error: nothing in the file can be trusted. */
export type ParseResult =
  | { ok: true; data: ParsedImport }
  | { ok: false; error: string };

// ---------------------------------------------------------------------------
// Duplicate analysis
// ---------------------------------------------------------------------------

/** What the importer needs to know about a password already on the device. */
export interface ExistingPassword {
  id: number;
  domain: string;
  username: string;
  password: string;
  updatedAt: number | null;
}

/**
 * Passwords with the same domain and username but different passwords.
 * The user decides what to do with them.
 */
export interface ConflictGroup {
  key: string;
  /** Display values (from the first incoming record). */
  domain: string;
  username: string;
  existingIds: number[];
  /** Indexes into `ParsedImport.passwords`. */
  incomingIndexes: number[];
}

export interface ImportPlan {
  /** Indexes into `ParsedImport.passwords` that can be imported without asking. */
  fresh: number[];
  conflicts: ConflictGroup[];
  /** Records skipped because domain, username AND password already exist. */
  duplicates: number;
}

export interface ImportDecisions {
  /** Conflicting incoming records (indexes) the user wants to import. */
  importIndexes: ReadonlySet<number>;
  /** Existing passwords (ids) the user wants removed after importing. */
  removeExistingIds: ReadonlySet<number>;
}

/** Everything the review step needs. Holds plaintext passwords: keep it in memory only. */
export interface ImportSession {
  format: TransferFormat;
  parsed: ParsedImport;
  plan: ImportPlan;
  existing: ExistingPassword[];
}

// ---------------------------------------------------------------------------
// Outcomes
// ---------------------------------------------------------------------------

export interface FailureSummaryLine {
  message: string;
  count: number;
}

/**
 * Result of an import. Messages never contain imported values, only
 * counts and fixed text, so they are safe to display and log.
 */
export type ImportOutcome =
  /** The user closed the file picker. Not an error. */
  | { status: "cancelled" }
  /** The file was readable but held no password records. */
  | { status: "empty" }
  | {
      status: "completed";
      /** Password records found in the file. */
      detected: number;
      /** Passwords written to the database. */
      imported: number;
      /** Records intentionally not imported (duplicates, or skipped by the user). */
      skipped: number;
      /** Records rejected as invalid, or that could not be saved. */
      failed: number;
      categoriesCreated: number;
      /** Existing passwords removed at the user's request. */
      removed: number;
      failureSummary: FailureSummaryLine[];
      warnings: string[];
    }
  /** File-level failure: nothing was imported. */
  | { status: "failed"; message: string };

export type PrepareOutcome =
  | Extract<ImportOutcome, { status: "cancelled" | "empty" | "failed" }>
  | { status: "ready"; session: ImportSession };

export type ExportOutcome =
  /** There are no passwords to export. */
  | { status: "empty" }
  /** The user closed the Save As dialog. Not an error. */
  | { status: "cancelled" }
  /** The system reported the file as written. */
  | { status: "saved"; count: number; fileName: string }
  | { status: "failed"; message: string };
