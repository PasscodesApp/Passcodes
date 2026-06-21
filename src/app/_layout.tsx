import ScreenHeading from "@/components/ScreenHeading";
import DatabaseProvider from "@/db/provider";
import {
  authenticateBiometric,
  isBiometricAvailable,
  isBiometricsAuthEnabled,
} from "@/libs/biometric";
import { NavigationBar } from "expo-navigation-bar";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useEffect, useRef, useState } from "react";
import { AppState, Button } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function RootLayout() {
  return (
    <DatabaseProvider>
      <AppContent />
    </DatabaseProvider>
  );
}

function AppContent() {
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
        const elapsedSeconds =
          Date.now() - (backgroundTimestamp.current ?? Date.now()) / 1000;

        if (elapsedSeconds > 30) {
          setIsAuthenticated(false);
        }
      }

      appState.current = nextAppState;
    });

    return () => subscription.remove();
  }, []);

  useEffect(() => {
    StatusBar.setStyle("auto");
    StatusBar.setHidden(false);

    NavigationBar.setStyle("auto");
    NavigationBar.setHidden(false);
  }, []);

  if (!isAuthenticated) {
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
        <Button
          title="Unlock"
          onPress={() => {
            async function unlock() {
              const available = await isBiometricAvailable();

              if (!available) {
                // TODO: must ask for other method to authenicate.
                setIsAuthenticated(true);
                return;
              }

              const success = await authenticateBiometric();
              setIsAuthenticated(success);
            }

            unlock();
          }}
        ></Button>
      </SafeAreaView>
    );
  }

  return <Stack screenOptions={{ headerShown: false }} />;
}
