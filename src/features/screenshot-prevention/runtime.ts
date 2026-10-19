import * as ScreenCapture from "expo-screen-capture";

export async function preventScreenShots(key?: string) {
  await ScreenCapture.preventScreenCaptureAsync(key);
}

export async function allowScreenShots(key?: string) {
  await ScreenCapture.allowScreenCaptureAsync(key);
}
