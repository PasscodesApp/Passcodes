import * as ScreenCapture from "expo-screen-capture";
import { isScreenshotPreventionEnabled } from "./store";

export async function preventScreenShots(key?: string) {
  isScreenshotPreventionEnabled() &&
    (await ScreenCapture.preventScreenCaptureAsync(key));
}

export async function allowScreenShots(key?: string) {
  isScreenshotPreventionEnabled() &&
    (await ScreenCapture.allowScreenCaptureAsync(key));
}
