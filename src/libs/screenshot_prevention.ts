import AsyncStorage from "expo-sqlite/kv-store";

import * as ScreenCapture from "expo-screen-capture";
const IS_SCREENSHOT_PREVENTION_ENABLED = "feat_screenshot_prevention_isEnabled";
enum SCREENSHOT_PREVENTION_KV {
  ENABLED = "true",
  DISABLED = "false",
}

export function getScreenShotSecureScreen() {
  if (isScreenshotPreventionEnabled()) {
    ScreenCapture.preventScreenCaptureAsync();
    return true;
  } else {
    ScreenCapture.allowScreenCaptureAsync();
    return false;
  }
}

export function toggleScreenshotPreventionFeature() {
  let isEnabled = isScreenshotPreventionEnabled();

  AsyncStorage.setItemAsync(
    IS_SCREENSHOT_PREVENTION_ENABLED,
    isEnabled
      ? SCREENSHOT_PREVENTION_KV.DISABLED
      : SCREENSHOT_PREVENTION_KV.ENABLED,
  );

  return !isEnabled;
}

export function isScreenshotPreventionEnabled() {
  let result = AsyncStorage.getItemSync(IS_SCREENSHOT_PREVENTION_ENABLED);

  // this line make sure, this feature is turn on by default.
  return result === SCREENSHOT_PREVENTION_KV.DISABLED ? false : true;
}
