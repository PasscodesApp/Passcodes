import { Stack } from "expo-router";

export default function PasswordManagerLayout() {
  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="index" options={{ title: "Password Manager" }} />

      <Stack.Screen
        name="password-details"
        options={{ title: "Password Details" }}
      />

      <Stack.Screen
        name="save-password"
        options={{
          title: "New Password",
          presentation: "formSheet",
          sheetAllowedDetents: [0.7, 0.95],
          sheetGrabberVisible: true,
          headerShown: false,
          sheetCornerRadius: 20,
        }}
      />
    </Stack>
  );
}
