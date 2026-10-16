/**
 * Duplicate analysis for imports. Pure functions, no I/O.
 *
 * Policy (nothing is ever overwritten silently):
 * - domain + username + password all equal to an existing password, or to an
 *   earlier record of the same file -> exact duplicate, skipped.
 * - domain + username equal but the password differs -> conflict. The user
 *   reviews every conflict; by default both versions are kept.
 * - anything else -> imported without asking.
 *
 * Domains compare after trimming and lowercasing (hostnames are not case
 * sensitive). Usernames and passwords compare exactly: discarding a record is
 * the destructive direction, so when in doubt the record is kept.
 */

import type {
  CandidatePassword,
  ConflictGroup,
  ExistingPassword,
  ImportDecisions,
  ImportPlan,
} from "../types";

const SEPARATOR = "\u0000";

export function normalizeDomain(domain: string): string {
  return domain.trim().toLowerCase();
}

function identityKey(domain: string, username: string): string {
  return `${normalizeDomain(domain)}${SEPARATOR}${username}`;
}

function exactKey(domain: string, username: string, password: string): string {
  return `${identityKey(domain, username)}${SEPARATOR}${password}`;
}

export function analyzeImport(
  candidates: readonly CandidatePassword[],
  existing: readonly ExistingPassword[],
): ImportPlan {
  const exactSeen = new Set<string>();
  const existingByIdentity = new Map<string, number[]>();

  for (const row of existing) {
    exactSeen.add(exactKey(row.domain, row.username, row.password));

    const key = identityKey(row.domain, row.username);
    const ids = existingByIdentity.get(key);

    if (ids) {
      ids.push(row.id);
    } else {
      existingByIdentity.set(key, [row.id]);
    }
  }

  let duplicates = 0;
  const incomingByIdentity = new Map<string, number[]>();

  candidates.forEach((candidate, index) => {
    const exact = exactKey(
      candidate.domain,
      candidate.username,
      candidate.password,
    );

    if (exactSeen.has(exact)) {
      duplicates += 1;
      return;
    }

    exactSeen.add(exact);

    const key = identityKey(candidate.domain, candidate.username);
    const indexes = incomingByIdentity.get(key);

    if (indexes) {
      indexes.push(index);
    } else {
      incomingByIdentity.set(key, [index]);
    }
  });

  const fresh: number[] = [];
  const conflicts: ConflictGroup[] = [];

  for (const [key, incomingIndexes] of incomingByIdentity) {
    const existingIds = existingByIdentity.get(key) ?? [];
    const firstIndex = incomingIndexes[0];

    if (existingIds.length === 0 && incomingIndexes.length === 1) {
      if (firstIndex !== undefined) {
        fresh.push(firstIndex);
      }
      continue;
    }

    const first = firstIndex === undefined ? undefined : candidates[firstIndex];

    conflicts.push({
      key,
      domain: first?.domain ?? "",
      username: first?.username ?? "",
      existingIds,
      incomingIndexes,
    });
  }

  fresh.sort((a, b) => a - b);
  conflicts.sort(
    (a, b) => (a.incomingIndexes[0] ?? 0) - (b.incomingIndexes[0] ?? 0),
  );

  return { fresh, conflicts, duplicates };
}

/** Default review state: keep both versions of every conflict, remove nothing. */
export function defaultDecisions(plan: ImportPlan): ImportDecisions {
  return {
    importIndexes: new Set(plan.conflicts.flatMap((c) => c.incomingIndexes)),
    removeExistingIds: new Set<number>(),
  };
}

/** Indexes of the incoming records to write, in file order. */
export function selectedIndexes(
  plan: ImportPlan,
  decisions: ImportDecisions,
): number[] {
  const chosen = plan.conflicts
    .flatMap((group) => group.incomingIndexes)
    .filter((index) => decisions.importIndexes.has(index));

  return [...plan.fresh, ...chosen].sort((a, b) => a - b);
}

/** Conflicting incoming records the user chose not to import. */
export function skippedByUser(
  plan: ImportPlan,
  decisions: ImportDecisions,
): number {
  return plan.conflicts
    .flatMap((group) => group.incomingIndexes)
    .filter((index) => !decisions.importIndexes.has(index)).length;
}

/**
 * Existing passwords to remove. Only applied to a group when at least one of
 * its incoming records was really written, so a failed import can never
 * cost the user the password it was meant to replace.
 */
export function removableExistingIds(
  plan: ImportPlan,
  decisions: ImportDecisions,
  insertedIndexes: ReadonlySet<number>,
): number[] {
  const ids: number[] = [];

  for (const group of plan.conflicts) {
    if (!group.incomingIndexes.some((index) => insertedIndexes.has(index))) {
      continue;
    }

    for (const id of group.existingIds) {
      if (decisions.removeExistingIds.has(id)) {
        ids.push(id);
      }
    }
  }

  return ids;
}
