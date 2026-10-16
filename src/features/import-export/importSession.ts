import { useSyncExternalStore } from "react";

import type { ImportOutcome, ImportSession } from "./types";

/**
 * In-memory hand-off between the Import screen and the Review screen
 * (same pattern as `libs/selection_store.ts`).
 *
 * The session contains plaintext passwords, so it is deliberately NOT put in
 * route params or React state, and it is cleared as soon as the review is
 * finished or left.
 */
let session: ImportSession | null = null;
let outcome: ImportOutcome | null = null;
const listeners = new Set<() => void>();

function emit() {
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

export function setImportSession(next: ImportSession) {
  session = next;
  emit();
}

export function clearImportSession() {
  if (session !== null) {
    session = null;
    emit();
  }
}

export function setImportOutcome(next: ImportOutcome | null) {
  outcome = next;
  emit();
}

export function useImportSession(): ImportSession | null {
  return useSyncExternalStore(subscribe, () => session);
}

export function useImportOutcome(): ImportOutcome | null {
  return useSyncExternalStore(subscribe, () => outcome);
}
