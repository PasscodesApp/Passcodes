import ScreenHeading from "@/components/ScreenHeading";
import DatabaseProvider from "@/db/provider";
import {
  isBiometricsAuthEnabled,
  unlockWithBiometricsApp,
} from "@/libs/biometric";
import { DarkTheme, DefaultTheme, Stack, ThemeProvider } from "expo-router";
import { useEffect, useRef, useState } from "react";
import { AppState, Button, useColorScheme } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function RootLayout() {
  return (
    <DatabaseProvider>
      <AppContent />
    </DatabaseProvider>
  );
}

function AppContent() {
  let systemScheme = useColorScheme();
  let isDarkScheme = systemScheme === "dark";
  const appState = useRef(AppState.currentState);
  const backgroundTimestamp = useRef<number | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(
    isBiometricsAuthEnabled() ? false : true,
  );

  useEffect(() => {
    const subscription = AppState.addEventListener("change", (nextAppState) => {
      if (
        appState.current === "active" &&
        nextAppState.match(/inactive|background/)
      ) {
        backgroundTimestamp.current = Date.now();
      }

      if (
        appState.current.match(/inactive|background/) &&
        nextAppState === "active"
      ) {
        const elapsedMilliseconds =
          Date.now() - (backgroundTimestamp.current ?? Date.now());

        if (elapsedMilliseconds / 1000 > 120) {
          setIsAuthenticated(false);
          unlock();
        }
      }

      appState.current = nextAppState;
    });

    return () => subscription.remove();
  }, []);

  async function unlock() {
    let result = await unlockWithBiometricsApp();
    setIsAuthenticated(result);
  }

  if (!isAuthenticated) {
    unlock();

    return (
      <SafeAreaView
        style={{
          flex: 1,
          alignItems: "center",
          justifyContent: "center",
          gap: 10,
        }}
      >
        <ScreenHeading title="App Locked!!" style={{ marginBlock: 12 }} />
        <Button title="Unlock" onPress={unlock}></Button>
      </SafeAreaView>
    );
  }

  return (
    <ThemeProvider value={isDarkScheme ? DarkTheme : DefaultTheme}>
      <Stack screenOptions={{ headerShown: true }} />
    </ThemeProvider>
  );
}
