import AsyncStorage from "expo-sqlite/kv-store";
import { useSyncExternalStore } from "react";

import { DEFAULT_FAVICON_PROVIDER, type FaviconProvider } from "./favicon";

const KEY = "feat_favicon_provider";

/** Older versions had an on/off switch only; "false" there means "off". */
const LEGACY_KEY = "feat_website_icons_isEnabled";

const VALID: readonly FaviconProvider[] = [
  "google",
  "duckduckgo",
  "faviconim",
  "off",
];

let current: FaviconProvider | undefined;
const listeners = new Set<() => void>();

export function getFaviconProvider(): FaviconProvider {
  if (current === undefined) {
    const stored = AsyncStorage.getItemSync(KEY);

    if (stored && (VALID as readonly string[]).includes(stored)) {
      current = stored as FaviconProvider;
    } else if (AsyncStorage.getItemSync(LEGACY_KEY) === "false") {
      current = "off";
    } else {
      current = DEFAULT_FAVICON_PROVIDER;
    }
  }

  return current;
}

export function setFaviconProvider(provider: FaviconProvider) {
  current = provider;
  AsyncStorage.setItemAsync(KEY, provider);
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

/** Reactive: components re-render when the provider changes. */
export function useFaviconProvider(): FaviconProvider {
  return useSyncExternalStore(subscribe, getFaviconProvider);
}
