// src/features/auth/biometric.ts

import * as LocalAuthentication from "expo-local-authentication";
import AsyncStorage from "expo-sqlite/kv-store";

const IS_BIOMETRICS_ENABLED = "feat_auth_isBiometicsEnabled";
enum BIOMETRICS_AUTH_KV {
  ENABLED = "true",
  DISABLED = "false",
}

// ------ PUBLIC APIS ------

export async function unlockWithBiometricsApp() {
  const isAuthEnrolled = canAuthenticate();

  if (!isAuthEnrolled) {
    return true;
  }

  let success = await authenticate();
  return success;
}

export function isBiometricsAuthEnabled() {
  let result = AsyncStorage.getItemSync(IS_BIOMETRICS_ENABLED);

  // this line make sure, this feature is turn on by default.
  return result === BIOMETRICS_AUTH_KV.DISABLED ? false : true;
}

export function toggleBiometricsFeature() {
  let isEnabled = isBiometricsAuthEnabled();

  // TODO: making such that is biometric are not enrolled and hardware is avaliable user are taking to settings app.

  AsyncStorage.setItemAsync(
    IS_BIOMETRICS_ENABLED,
    isEnabled ? BIOMETRICS_AUTH_KV.DISABLED : BIOMETRICS_AUTH_KV.ENABLED,
  );

  return !isEnabled;
}

// ------ PRIVATE APIS ------

async function canAuthenticate() {
  const level = await LocalAuthentication.getEnrolledLevelAsync();

  return level !== LocalAuthentication.SecurityLevel.NONE;
}

async function authenticate() {
  const result = await LocalAuthentication.authenticateAsync({
    promptMessage: "Unlock Passcodes",
    cancelLabel: "Cancel",
    fallbackLabel: "Use device password",
    disableDeviceFallback: false,
  });

  return result.success;
}
