import DatabaseProvider from "@/db/provider";
import { Stack } from "expo-router";

export default function RootLayout() {
  return (
    <DatabaseProvider>
      <AppContent />
    </DatabaseProvider>
  );
}

function AppContent() {
  return <Stack screenOptions={{ headerShown: false }} />;
}
