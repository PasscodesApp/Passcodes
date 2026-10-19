import * as ScreenCapture from "expo-screen-capture";
import { useEffect } from "react";

export function useSecretScreenController(key: string) {
  useEffect(() => {
    ScreenCapture.preventScreenCaptureAsync(key);

    return () => {
      ScreenCapture.allowScreenCaptureAsync(key);
    };
  }, []);
}
