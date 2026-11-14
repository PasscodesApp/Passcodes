import AsyncStorage from "expo-sqlite/kv-store";
import { useSyncExternalStore } from "react";

const KEY = "feat_website_icons_isEnabled";

let current: boolean | undefined;
const listeners = new Set<() => void>();

/** On by default; only an explicit "false" turns it off. */
export function areWebsiteIconsEnabled(): boolean {
  if (current === undefined) {
    current = AsyncStorage.getItemSync(KEY) !== "false";
  }

  return current;
}

export function setWebsiteIconsEnabled(enabled: boolean) {
  current = enabled;
  AsyncStorage.setItemAsync(KEY, enabled ? "true" : "false");
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

/** Reactive: components re-render when the setting changes. */
export function useWebsiteIconsEnabled(): boolean {
  return useSyncExternalStore(subscribe, areWebsiteIconsEnabled);
}
