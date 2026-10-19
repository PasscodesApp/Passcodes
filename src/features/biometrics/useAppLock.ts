import { useEffect, useSyncExternalStore } from "react";
import { AppState } from "react-native";

import { describeAuthFailure } from "./authentication";
import type { AppStateName } from "./lockController";
import { appLock } from "./runtime";

/**
 * App Lock for the root layout: tells whether the app is locked, keeps it in
 * sync with the app going to the background, and starts the unlock prompt.
 */
export function useAppLock() {
  const state = useSyncExternalStore(appLock.subscribe, appLock.getState);

  useEffect(() => {
    const subscription = AppState.addEventListener("change", (next) =>
      appLock.handleAppStateChange(next as AppStateName),
    );

    // Locked at launch: ask right away. Overlapping calls share one prompt.
    if (appLock.getState().locked) {
      void appLock.unlock();
    }

    return () => subscription.remove();
  }, []);

  return {
    locked: state.locked,
    authenticating: state.authenticating,
    /** Why the last unlock attempt failed, or `null` (nothing to show). */
    message: describeAuthFailure(state.lastOutcome),
    unlock: () => {
      void appLock.unlock();
    },
  };
}
