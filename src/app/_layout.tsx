import ScreenHeading from "@/components/ScreenHeading";
import DatabaseProvider from "@/db/provider";
import {
  authenticateBiometric,
  isBiometricAvailable,
  isBiometricsAuthEnabled,
} from "@/libs/biometric";
import { AppState, Button } from "react-native";

import { Stack } from "expo-router";
import { useEffect, useState } from "react";
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
        <ScreenHeading title="App Locked!!" />
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
