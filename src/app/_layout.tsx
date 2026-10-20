import Config from "@/config";
import { RepositoryProvider } from "@/contexts/RepositoryContext";
import { ToastProvider } from "@/contexts/ToastContext";
import { LockScreen, useAppLock } from "@/features/biometrics";
import { NavigationBar } from "expo-navigation-bar";
import { DarkTheme, DefaultTheme, Slot, ThemeProvider } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useEffect } from "react";
import { useColorScheme } from "react-native";
import { PaperProvider } from "react-native-paper";

export default function RootLayout() {
  let systemScheme = useColorScheme();

  useEffect(() => {
    StatusBar.setStyle("auto");
    StatusBar.setHidden(false);

    NavigationBar.setStyle("auto");
    NavigationBar.setHidden(false);
  }, [systemScheme]);

  return (
    <RepositoryProvider dbName={Config.DATABASE_NAME}>
      <AppContent />
    </RepositoryProvider>
  );
}

function AppContent() {
  let systemScheme = useColorScheme();
  let isDarkScheme = systemScheme === "dark";

  // All App Lock behaviour (when to lock, re-locking after the app was in
  // the background, the unlock prompt) lives in the biometrics feature.
  const { locked, authenticating, message, unlock } = useAppLock();

  if (locked) {
    return (
      <LockScreen onUnlock={unlock} busy={authenticating} message={message} />
    );
  }

  return (
    <ThemeProvider value={isDarkScheme ? DarkTheme : DefaultTheme}>
      <PaperProvider>
        <ToastProvider>
          <Slot />
        </ToastProvider>
      </PaperProvider>
    </ThemeProvider>
  );
}
