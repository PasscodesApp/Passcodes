import * as ScreenCapture from "expo-screen-capture";
import { useEffect } from "react";

export function useSecretScreen(key: string) {
  useEffect(() => {
    ScreenCapture.preventScreenCaptureAsync(key);

    return () => {
      ScreenCapture.allowScreenCaptureAsync(key);
    };
  }, []);
}
