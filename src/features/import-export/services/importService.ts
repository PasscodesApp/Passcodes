/**
 * Import workflow, in two steps so the user can review duplicates:
 *
 *   prepareImport()  pick -> read -> parse -> compare with the database
 *   commitImport()   write the chosen records, categories first
 *
 * No React Native / Expo imports: file access and repositories are passed in,
 * so the workflow can be tested in plain Node. Messages never contain values
 * from the file.
 */

import type { CategoryRepository } from "@/repositories/CategoryRepository";
import type {
  ImportPasswordRow,
  PasswordRepository,
} from "@/repositories/PasswordRepository";

import { parseGoogleCsv } from "../formats/googleCsv";
import { parsePasscodesJson } from "../formats/passcodesJson";
import {
  FAILURE_REASON_TEXT,
  type CandidateCategory,
  type FailureReason,
  type FailureSummaryLine,
  type ImportDecisions,
  type ImportOutcome,
  type ImportSession,
  type ParseResult,
  type PrepareOutcome,
  type TransferFormat,
} from "../types";
import {
  analyzeImport,
  removableExistingIds,
  selectedIndexes,
  skippedByUser,
} from "../utils/duplicates";

export interface ImportRepositories {
  passwords: Pick<PasswordRepository, "getAll" | "importMany" | "deleteMany">;
  categories: Pick<CategoryRepository, "getAll" | "create">;
}

/** Resolves to the file text, or `null` if the user cancelled the picker. */
export type PickText = (format: TransferFormat) => Promise<string | null>;

function parseFile(format: TransferFormat, text: string): ParseResult {
  return format === "google-csv"
    ? parseGoogleCsv(text)
    : parsePasscodesJson(text);
}

function describeNothingValid(failed: number): string {
  return `None of the ${failed} record(s) in the file were valid, so nothing was imported.`;
}

/** Never throws. Does not change the database. */
export async function prepareImport(
  format: TransferFormat,
  repositories: Pick<ImportRepositories, "passwords">,
  pickText: PickText,
): Promise<PrepareOutcome> {
  let text: string | null;

  try {
    text = await pickText(format);
  } catch {
    return {
      status: "failed",
      message: "The selected file couldn't be read. Nothing was imported.",
    };
  }

  if (text === null) {
    return { status: "cancelled" };
  }

  const parsed = parseFile(format, text);

  if (!parsed.ok) {
    return { status: "failed", message: parsed.error };
  }

  const { data } = parsed;

  if (data.detected === 0) {
    return { status: "empty" };
  }

  if (data.passwords.length === 0) {
    return { status: "failed", message: describeNothingValid(data.detected) };
  }

  let existing: ImportSession["existing"];

  try {
    const rows = await repositories.passwords.getAll();

    existing = rows.map((row) => ({
      id: row.id,
      domain: row.domain,
      username: row.username,
      password: row.password,
      updatedAt: row.updatedAt,
    }));
  } catch {
    return {
      status: "failed",
      message: "Your saved passwords couldn't be read. Nothing was imported.",
    };
  }

  return {
    status: "ready",
    session: {
      format,
      parsed: data,
      plan: analyzeImport(data.passwords, existing),
      existing,
    },
  };
}

function normalizeCategoryName(name: string): string {
  return name.trim().replace(/\s+/g, " ").toLowerCase();
}

/**
 * Maps the file's category `_id`s to database ids, reusing categories that
 * already exist (same name, case-insensitive) and creating the rest.
 */
async function resolveCategories(
  candidates: readonly CandidateCategory[],
  repository: ImportRepositories["categories"],
): Promise<{
  idBySource: Map<number, number>;
  created: number;
  failed: number;
}> {
  const idBySource = new Map<number, number>();

  if (candidates.length === 0) {
    return { idBySource, created: 0, failed: 0 };
  }

  const idByName = new Map<string, number>();

  try {
    for (const category of await repository.getAll()) {
      idByName.set(normalizeCategoryName(category.name), category.id);
    }
  } catch {
    return { idBySource, created: 0, failed: candidates.length };
  }

  let created = 0;
  let failed = 0;

  for (const candidate of candidates) {
    const key = normalizeCategoryName(candidate.name);
    const knownId = idByName.get(key);

    if (knownId !== undefined) {
      idBySource.set(candidate.sourceId, knownId);
      continue;
    }

    try {
      const category = await repository.create(candidate.name);
      idByName.set(key, category.id);
      idBySource.set(candidate.sourceId, category.id);
      created += 1;
    } catch {
      // Invalid or reserved name: its passwords are imported as Unsorted.
      failed += 1;
    }
  }

  return { idBySource, created, failed };
}

function summarizeFailures(
  reasons: readonly FailureReason[],
): FailureSummaryLine[] {
  const counts = new Map<FailureReason, number>();

  for (const reason of reasons) {
    counts.set(reason, (counts.get(reason) ?? 0) + 1);
  }

  return [...counts].map(([reason, count]) => ({
    message: FAILURE_REASON_TEXT[reason],
    count,
  }));
}

/** Never throws. Writes the chosen records and reports what was committed. */
export async function commitImport(
  session: ImportSession,
  decisions: ImportDecisions,
  repositories: ImportRepositories,
): Promise<ImportOutcome> {
  const { parsed, plan } = session;
  const warnings = [...parsed.warnings];

  const chosen = selectedIndexes(plan, decisions);
  const categories = await resolveCategories(
    parsed.categories,
    repositories.categories,
  );

  if (categories.failed > 0) {
    warnings.push(
      `${categories.failed} categor${categories.failed === 1 ? "y" : "ies"} couldn't be created; their passwords were imported as Unsorted.`,
    );
  }

  const candidates = chosen.flatMap((index) => {
    const candidate = parsed.passwords[index];
    return candidate ? [{ index, candidate }] : [];
  });

  const rows: ImportPasswordRow[] = candidates.map(({ candidate }) => ({
    domain: candidate.domain,
    username: candidate.username,
    password: candidate.password,
    notes: candidate.notes,
    url: candidate.url,
    categoryId:
      candidate.categorySourceId === null
        ? null
        : (categories.idBySource.get(candidate.categorySourceId) ?? null),
    createdAt: candidate.createdAt,
    updatedAt: candidate.updatedAt,
  }));

  let insertedRowIndexes: number[] = [];
  let failedRowIndexes: number[] = [];

  if (rows.length > 0) {
    try {
      const result = await repositories.passwords.importMany(rows);
      insertedRowIndexes = result.insertedIndexes;
      failedRowIndexes = result.failedIndexes;
    } catch {
      failedRowIndexes = rows.map((_, index) => index);
    }
  }

  if (rows.length > 0 && insertedRowIndexes.length === 0) {
    return {
      status: "failed",
      message: "The passwords couldn't be saved. Nothing was imported.",
    };
  }

  const insertedCandidateIndexes = new Set(
    insertedRowIndexes.flatMap((rowIndex) => {
      const entry = candidates[rowIndex];
      return entry ? [entry.index] : [];
    }),
  );

  let removed = 0;
  const idsToRemove = removableExistingIds(
    plan,
    decisions,
    insertedCandidateIndexes,
  );

  if (idsToRemove.length > 0) {
    try {
      await repositories.passwords.deleteMany(idsToRemove);
      removed = idsToRemove.length;
    } catch {
      warnings.push("Some existing passwords couldn't be removed.");
    }
  }

  const passwordFailures = parsed.failures.filter(
    (failure) => failure.table === "passwords",
  );

  const failureReasons: FailureReason[] = [
    ...passwordFailures.map((failure) => failure.reason),
    ...failedRowIndexes.map((): FailureReason => "save-failed"),
  ];

  return {
    status: "completed",
    detected: parsed.detected,
    imported: insertedRowIndexes.length,
    skipped: plan.duplicates + skippedByUser(plan, decisions),
    failed: failureReasons.length,
    categoriesCreated: categories.created,
    removed,
    failureSummary: summarizeFailures(failureReasons),
    warnings,
  };
}
