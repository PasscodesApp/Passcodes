import { drizzle } from "drizzle-orm/expo-sqlite";
import { useMigrations } from "drizzle-orm/expo-sqlite/migrator";
import { File } from "expo-file-system";
import { Stack } from "expo-router";
import { SQLiteProvider, openDatabaseSync } from "expo-sqlite";
import { Suspense } from "react";
import { ActivityIndicator, Text, View } from "react-native";
import migrations from "../db/drizzle/migrations";

async function migrateOldAndroidData() {
  console.log("----- Migrating Android Old Data To Expo -----");

  const packageName = "com.jeeldobariya.passcodes.dev"; // Replace with your Android application ID
  const dbName = "master.db"; // Replace with your Room database name

  // 1. Construct the exact URI
  const dbDirUri = `file:///data/data/${packageName}/databases/`;
  console.log(dbDirUri);

  // 2. Check if the file exists before performing operations
  const roomDbFile = new File(dbDirUri, dbName);
  if (!roomDbFile.exists) {
    console.warn("room database file not found at:", roomDbFile);
    // return; // TODO: just for debugging. must throw a error instead.
    // throw new Error("Database not found at: " + dbUri);
  }

  console.log("room database file found at:", roomDbFile.uri);
}

const DATABASE_NAME = "test2.db";

export default function RootLayout() {
  const expoDb = openDatabaseSync(DATABASE_NAME);
  migrateOldAndroidData();

  const db = drizzle(expoDb);

  const { success, error } = useMigrations(db, migrations);

  if (error) {
    return (
      <View>
        <Text>Migration error: {error.message}</Text>
      </View>
    );
  }

  if (!success) {
    return (
      <View>
        <Text>Migration is in progress...</Text>
      </View>
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
