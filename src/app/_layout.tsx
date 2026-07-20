import ScreenHeading from "@/components/ScreenHeading";
import DatabaseProvider from "@/db/provider";
import {
  isBiometricsAuthEnabled,
  unlockWithBiometricsApp,
} from "@/libs/biometric";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { NavigationBar } from "expo-navigation-bar";
import { DarkTheme, DefaultTheme, Tabs, ThemeProvider } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useEffect, useRef, useState } from "react";
import { AppState, Button, useColorScheme } from "react-native";
import { PaperProvider, useTheme } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function RootLayout() {
  let systemScheme = useColorScheme();

  useEffect(() => {
    StatusBar.setStyle("auto");
    StatusBar.setHidden(false);

    NavigationBar.setStyle("auto");
    NavigationBar.setHidden(false);
  }, [systemScheme]);

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
      <PaperProvider>
        <Tabs
          screenOptions={{
            headerShown: true,
            tabBarActiveTintColor: useTheme().colors.tertiary,
            headerTitleAlign: "center",
          }}
        >
          <Tabs.Screen
            name="(password-manager)"
            options={{
              title: "Passcodes",
              tabBarIcon: ({ size, color }) => (
                <FontAwesome6
                  name="key"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              ),
            }}
          />
          <Tabs.Screen
            name="(settings)"
            options={{
              title: "Settings",
              tabBarIcon: ({ size, color }) => (
                <FontAwesome6
                  name="gear"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              ),
            }}
          />
        </Tabs>
      </PaperProvider>
    </ThemeProvider>
  );
}
