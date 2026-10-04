import { useSyncExternalStore } from "react";

/**
 * In-memory collapsed/expanded state of the home sections.
 *
 * It lives OUTSIDE React state on purpose: toggling one section re-renders
 * only that section, not the whole screen (and not the FAB).
 * Reset on app restart (all sections start expanded).
 */
const collapsed = new Set<string>();
const listeners = new Set<() => void>();

function subscribe(listener: () => void) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

export function toggleCollapsed(key: string) {
  if (collapsed.has(key)) {
    collapsed.delete(key);
  } else {
    collapsed.add(key);
  }

  listeners.forEach((listener) => listener());
}

/** Re-renders only when the collapsed state of THIS key changes. */
export function useIsCollapsed(key: string): boolean {
  return useSyncExternalStore(subscribe, () => collapsed.has(key));
}
