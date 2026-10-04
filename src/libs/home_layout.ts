import AsyncStorage from "expo-sqlite/kv-store";
import { useSyncExternalStore } from "react";

/**
 * How passwords are organised on the home screen.
 *
 * - "grouped": collapsible sections, one per category
 * - "folders": a grid of folders, tap one to open its passwords
 *
 * Both layouts show the same flat categories, only the presentation differs.
 */
export type HomeLayout = "grouped" | "folders";

const HOME_LAYOUT_KEY = "feat_home_layout";

let current: HomeLayout | undefined;
const listeners = new Set<() => void>();

export function getHomeLayout(): HomeLayout {
  if (current === undefined) {
    current =
      AsyncStorage.getItemSync(HOME_LAYOUT_KEY) === "folders"
        ? "folders"
        : "grouped"; // default
  }

  return current;
}

export function setHomeLayout(layout: HomeLayout) {
  current = layout;
  AsyncStorage.setItemAsync(HOME_LAYOUT_KEY, layout);
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

/** Reactive: components re-render when the setting changes. */
export function useHomeLayout(): HomeLayout {
  return useSyncExternalStore(subscribe, getHomeLayout);
}
