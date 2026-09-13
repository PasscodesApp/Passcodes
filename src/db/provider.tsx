import Text from "@/components/Text";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { useMigrations } from "drizzle-orm/expo-sqlite/migrator";
import { openDatabaseSync } from "expo-sqlite";
import {
  createContext,
  Suspense,
  useContext,
  type PropsWithChildren,
} from "react";
import { View } from "react-native";
import { ActivityIndicator } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

import migrations from "./drizzle/migrations";
import * as schema from "./schema";

export type DrizzleDatabase = ReturnType<typeof drizzle>;

const DrizzleDatabaseContext = createContext<DrizzleDatabase | undefined>(
  undefined,
);

interface DrizzleDatabaseProviderProps extends PropsWithChildren {
  /**
   * Name of the SQLite database file.
   */
  databaseName: string;
}

/**
 * Provides a single shared Drizzle database instance to the
 * entire component tree.
 *
 * The provider owns the complete database lifecycle:
 *
 * 1. Opens the Expo SQLite database.
 * 2. Creates the Drizzle database instance.
 * 3. Runs Drizzle migrations.
 * 4. Waits until migrations have completed successfully.
 * 5. Provides the ready Drizzle database through React context.
 *
 * Children are never rendered before the database is ready.
 * Migration failures are displayed using the provider's fatal
 * database error UI.
 *
 * The database instance is shared by all descendants. Consumers
 * should access it using `useDrizzleDatabase()` instead of opening
 * their own SQLite connection.
 *
 * @example
 * ```tsx
 * <DrizzleDatabaseProvider
 *   databaseName="master.db"
 * >
 *   <App />
 * </DrizzleDatabaseProvider>
 * ```
 */
export default function DrizzleDatabaseProvider({
  children,
  databaseName,
}: DrizzleDatabaseProviderProps) {
  const expoDb = openDatabaseSync(databaseName, {
    enableChangeListener: true,
  });

  const db = drizzle(expoDb, { schema });

  const { success, error } = useMigrations(db, migrations);

  if (error) {
    return <DrizzleDatabaseError error={error} />;
  }

  if (!success) {
    return <DrizzleDatabaseLoading />;
  }

  return (
    <DrizzleDatabaseContext.Provider value={db}>
      <Suspense fallback={<DrizzleDatabaseLoading />}>{children}</Suspense>
    </DrizzleDatabaseContext.Provider>
  );
}

/**
 * Returns the shared Drizzle database.
 *
 * this hook is only intended to be use inside `DrizzleDatabaseProvider`.
 */
export function useDrizzleDatabase(): DrizzleDatabase {
  const db = useContext(DrizzleDatabaseContext);

  if (db === undefined) {
    throw new Error(
      "useDrizzleDatabase must be used inside DrizzleDatabaseProvider.",
    );
  }

  return db;
}

function DrizzleDatabaseLoading() {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: "#020221",
      }}
    >
      <ActivityIndicator size="large" style={{ margin: 20 }} />

      <Text style={{ color: "lightblue", fontSize: 30 }}>Please Wait.....</Text>
      <Text style={{ color: "lightblue" }}>Opening Local Database...</Text>
    </SafeAreaView>
  );
}

function DrizzleDatabaseError({ error }: { error: unknown }) {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: "#020221",
        paddingHorizontal: 24,
      }}
    >
      <View
        style={{
          width: "100%",
          maxWidth: 420,
          padding: 24,
          borderRadius: 24,
          alignItems: "center",
          gap: 12,
        }}
      >
        <View
          style={{
            width: 56,
            height: 56,
            borderRadius: 28,
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: "#351B25",
            marginBottom: 4,
          }}
        >
          <Text
            style={{
              color: "#EF9F9F",
              fontSize: 26,
              fontWeight: "800",
            }}
          >
            !
          </Text>
        </View>

        <Text
          style={{
            color: "#EF9F9F",
            fontSize: 18,
            fontWeight: "800",
            textAlign: "center",
          }}
        >
          Database initialization failed
        </Text>

        <Text
          style={{
            color: "#D9BFC3",
            fontSize: 14,
            lineHeight: 21,
            textAlign: "center",
          }}
        >
          Passcodes couldn't prepare its local database. Please close and reopen
          the app.
        </Text>
      </View>
    </SafeAreaView>
  );
}
