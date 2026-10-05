import { useEffect, useSyncExternalStore } from "react";
import { BackHandler } from "react-native";

/**
 * Multi-select (like a gallery app): long press a password to start,
 * tap to add/remove, then move or delete the selection.
 *
 * Lives outside React state so selecting a card re-renders only that card
 * (plus the selection bar), never the whole list.
 */
let selected = new Set<number>();
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

export function toggleSelected(id: number) {
  const next = new Set(selected);

  if (next.has(id)) {
    next.delete(id);
  } else {
    next.add(id);
  }

  selected = next;
  emit();
}

export function selectMany(ids: number[]) {
  selected = new Set(ids);
  emit();
}

export function clearSelection() {
  if (selected.size === 0) {
    return;
  }

  selected = new Set();
  emit();
}

export function getSelectedIds(): number[] {
  return Array.from(selected);
}

export function isSelecting(): boolean {
  return selected.size > 0;
}

export type SelectionState = "off" | "unselected" | "selected";

/** Per-card state, a card only re-renders when ITS state changes. */
export function useSelectionState(id: number): SelectionState {
  return useSyncExternalStore(subscribe, () =>
    selected.size === 0 ? "off" : selected.has(id) ? "selected" : "unselected",
  );
}

/** Flips only when selection mode starts / ends. */
export function useIsSelecting(): boolean {
  return useSyncExternalStore(subscribe, () => selected.size > 0);
}

export function useSelectionCount(): number {
  return useSyncExternalStore(subscribe, () => selected.size);
}

/** Android back button leaves selection mode first. */
export function useSelectionBackHandler() {
  const active = useIsSelecting();

  useEffect(() => {
    if (!active) {
      return;
    }

    const subscription = BackHandler.addEventListener(
      "hardwareBackPress",
      () => {
        clearSelection();
        return true;
      },
    );

    return () => subscription.remove();
  }, [active]);
}
