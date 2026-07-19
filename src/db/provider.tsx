import Text from "@/components/Text";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { useMigrations } from "drizzle-orm/expo-sqlite/migrator";
import { openDatabaseSync, SQLiteProvider } from "expo-sqlite";
import { Suspense, type PropsWithChildren } from "react";
import { View } from "react-native";
import { ActivityIndicator } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";
import migrations from "./drizzle/migrations";

export const DATABASE_NAME = "master.db";

export default function DatabaseProvider({ children }: PropsWithChildren) {
  const expoDb = openDatabaseSync(DATABASE_NAME);

  const db = drizzle(expoDb);

  const { success, error } = useMigrations(db, migrations);

  if (error) {
    return (
      <SafeAreaView
        style={{ flex: 1, alignItems: "center", justifyContent: "center" }}
      >
        <View
          style={{
            backgroundColor: "#020221",
            padding: 20,
            borderRadius: 20,
            gap: 12,
            alignItems: "center",
          }}
        >
          <Text style={{ color: "#EF9F9F", fontSize: 16, fontWeight: 800 }}>
            Something went wrong!!
          </Text>
          <Text style={{ color: "#f56f6f" }}>
            Please close & reopen the app.
          </Text>
        </View>
      </SafeAreaView>
    );
  }

  if (!success) {
    return (
      <SafeAreaView
        style={{ flex: 1, alignItems: "center", justifyContent: "center" }}
      >
        <ActivityIndicator size="large" />
      </SafeAreaView>
    );
  }

  return (
    <Suspense
      fallback={
        <SafeAreaView
          style={{ flex: 1, alignItems: "center", justifyContent: "center" }}
        >
          <ActivityIndicator size="large" />
        </SafeAreaView>
      }
    >
      <SQLiteProvider
        databaseName={DATABASE_NAME}
        options={{
          enableChangeListener: true,
        }}
        useSuspense
      >
        {children}
      </SQLiteProvider>
    </Suspense>
  );
}
