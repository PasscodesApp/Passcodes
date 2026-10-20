/**
 * Wires the pure biometrics logic to the real platform: `expo-local-authentication`
 * for the prompt, `expo-sqlite/kv-store` for the setting, `AppState` for the
 * foreground/background events. This is the only file in the feature that
 * imports native modules.
 */

import * as LocalAuthentication from "expo-local-authentication";
import Storage from "expo-sqlite/kv-store";
import { AppState } from "react-native";

import {
  createAuthenticator,
  type AuthBackend,
  type AuthOutcome,
} from "./authentication";
import { createFlag } from "./flag";
import {
  AUTO_LOCK_TIMEOUT_MS,
  createAppLock,
  type AppStateName,
} from "./lockController";

/**
 * Same key (typo included) the app has always used, so users keep their
 * existing choice. Do not rename it.
 */
const APP_LOCK_SETTING_KEY = "feat_auth_isBiometicsEnabled";

const expoBackend: AuthBackend = {
  async hasDeviceSecurity() {
    const level = await LocalAuthentication.getEnrolledLevelAsync();
    return level !== LocalAuthentication.SecurityLevel.NONE;
  },

  async prompt(message) {
    const result = await LocalAuthentication.authenticateAsync({
      promptMessage: message,
      cancelLabel: "Cancel",
      fallbackLabel: "Use device password",
      disableDeviceFallback: false,
    });

    return result.success
      ? { success: true }
      : { success: false, error: result.error };
  },
};

const authenticator = createAuthenticator(expoBackend);

/** On by default. */
const appLockSetting = createFlag(Storage, APP_LOCK_SETTING_KEY, true);

export const appLock = createAppLock({
  authenticator,
  isEnabled: appLockSetting.get,
  now: Date.now,
  timeoutMs: AUTO_LOCK_TIMEOUT_MS,
  unlockReason: "Unlock Passcodes",
  initialAppState: AppState.currentState as AppStateName,
});

// ------ PUBLIC APIS ------

export function isAppLockEnabled(): boolean {
  return appLockSetting.get();
}

/** Flips the setting and returns the value that is really stored now. */
export function toggleAppLock(): boolean {
  return appLockSetting.toggle();
}

/**
 * Asks the user to prove who they are before a sensitive action (exporting,
 * removing passwords). It asks even when the App Lock switch is off: that
 * switch is about locking the app, not about protecting these actions.
 */
export function confirmIdentity(reason: string): Promise<AuthOutcome> {
  return authenticator.authenticate(reason);
}
