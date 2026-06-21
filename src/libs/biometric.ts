// src/features/auth/biometric.ts

import * as LocalAuthentication from "expo-local-authentication";
import AsyncStorage from "expo-sqlite/kv-store";

const IS_BIOMETRICS_ENABLED = "feat_auth_isBiometicsEnabled";
enum BIOMETRICS_AUTH_KV {
  ENABLED = "true",
  DISABLED = "false",
}

export function isBiometricsAuthEnabled() {
  let result = AsyncStorage.getItemSync(IS_BIOMETRICS_ENABLED);
  return result === BIOMETRICS_AUTH_KV.DISABLED ? false : true;
}

export function toggleBiometricsFeature() {
  let isEnabled = isBiometricsAuthEnabled();

  AsyncStorage.setItemAsync(
    IS_BIOMETRICS_ENABLED,
    isEnabled ? BIOMETRICS_AUTH_KV.DISABLED : BIOMETRICS_AUTH_KV.ENABLED,
  );

  return !isEnabled;
}

export async function isBiometricAvailable() {
  const hasHardware = await LocalAuthentication.hasHardwareAsync();
  const isEnrolled = await LocalAuthentication.isEnrolledAsync();

  return hasHardware && isEnrolled;
}

export async function authenticateBiometric() {
  const result = await LocalAuthentication.authenticateAsync({
    promptMessage: "Unlock Passcodes",
    cancelLabel: "Cancel",
    fallbackLabel: "Use device password",
    disableDeviceFallback: false,
  });

  return result.success;
}
