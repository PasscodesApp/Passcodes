import { drizzle } from "drizzle-orm/expo-sqlite";
import { useMigrations } from "drizzle-orm/expo-sqlite/migrator";
import { Stack } from "expo-router";
import { SQLiteProvider, openDatabaseSync } from "expo-sqlite";
import { Suspense } from "react";
import { ActivityIndicator, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import migrations from "../db/drizzle/migrations";

const DATABASE_NAME = "test2.db";

export default function RootLayout() {
  const expoDb = openDatabaseSync(DATABASE_NAME);

  const db = drizzle(expoDb);

  const { success, error } = useMigrations(db, migrations);

  if (error) {
    return (
      <SafeAreaView>
        <Text style={{ fontSize: 32 }}>Migration error: {error.message}</Text>
      </SafeAreaView>
    );
  }

  if (!success) {
    return (
      <SafeAreaView>
        <ActivityIndicator size="large" />
      </SafeAreaView>
    );
  }

  return (
    <Suspense fallback={<ActivityIndicator size="large" />}>
      <SQLiteProvider databaseName={DATABASE_NAME} useSuspense>
        <Stack screenOptions={{ headerShown: false }} />
      </SQLiteProvider>
    </Suspense>
  );
}
