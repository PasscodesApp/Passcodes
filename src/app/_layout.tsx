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
import { useEffect, useState } from "react";
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
  const [authenticated, setAuthenticated] = useState(
    isBiometricsAuthEnabled() ? false : true,
  );

  useEffect(() => {
    StatusBar.setStyle("auto");
    StatusBar.setHidden(false);

    NavigationBar.setStyle("auto");
    NavigationBar.setHidden(false);
  }, []);

  useEffect(() => {
    const subscription = AppState.addEventListener("change", (nextAppState) => {
      if (nextAppState !== "active") {
        setAuthenticated(false);
      }
    });

    return () => {
      subscription.remove();
    };
  }, []);

  if (!authenticated) {
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
                setAuthenticated(true);
                return;
              }

              const success = await authenticateBiometric();
              setAuthenticated(success);
            }

            unlock();
          }}
        ></Button>
      </SafeAreaView>
    );
  }

  return <Stack screenOptions={{ headerShown: false }} />;
}
