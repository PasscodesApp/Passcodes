import { Stack } from "expo-router";

export default function SettingsLayout() {
  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="settings"></Stack.Screen>

      <Stack.Screen
        name="import-passwords"
        options={{ title: "Import passwords", headerShown: true }}
      />

      <Stack.Screen
        name="import-review"
        options={{ title: "Review duplicates", headerShown: true }}
      />

      <Stack.Screen
        name="export-passwords"
        options={{ title: "Export passwords", headerShown: true }}
      />
    </Stack>
  );
}
