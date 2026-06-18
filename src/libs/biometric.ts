// src/features/auth/biometric.ts

import * as LocalAuthentication from "expo-local-authentication";

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
