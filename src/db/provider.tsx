import Text from "@/components/Text";
import { DatabaseRecoveryScreen } from "@/features/database-recovery";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { useMigrations } from "drizzle-orm/expo-sqlite/migrator";
import { openDatabaseSync, type SQLiteDatabase } from "expo-sqlite";
import {
  createContext,
  Suspense,
  useCallback,
  useContext,
  useState,
  type PropsWithChildren,
} from "react";
import { ActivityIndicator } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

import migrations from "./drizzle/migrations";
import * as schema from "./schema";

export type DrizzleDatabase = ReturnType<typeof drizzle>;

const DrizzleDatabaseContext = createContext<DrizzleDatabase | undefined>(
  undefined,
);

/**
 * Connections that are already open, by file name.
 *
 * A connection is reused when the user taps "Try again" and is never closed
 * here: closing the last connection to a WAL database makes SQLite merge and
 * delete the -wal file, which would modify the user's database files while
 * they are trying to recover them.
 */
const openConnections = new Map<string, SQLiteDatabase>();

type OpenResult =
  | { ok: true; db: DrizzleDatabase }
  | { ok: false; error: unknown };

function openDrizzleDatabase(databaseName: string): OpenResult {
  try {
    let expoDb = openConnections.get(databaseName);

    if (!expoDb) {
      expoDb = openDatabaseSync(databaseName, { enableChangeListener: true });
      openConnections.set(databaseName, expoDb);
    }

    return { ok: true, db: drizzle(expoDb, { schema }) };
  } catch (error) {
    return { ok: false, error };
  }
}

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
 * Children are never rendered before the database is ready. If opening the
 * database or running a migration fails, the database recovery screen is
 * shown instead. It never changes the existing database, and "Try again"
 * repeats the whole start-up.
 *
 * Consumers should access the database with `useDrizzleDatabase()` instead
 * of opening their own SQLite connection.
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
  const [attempt, setAttempt] = useState(0);
  const retry = useCallback(() => setAttempt((current) => current + 1), []);

  // `key` restarts the whole start-up (open + migrate) on every retry.
  return (
    <DatabaseStartup key={attempt} databaseName={databaseName} onRetry={retry}>
      {children}
    </DatabaseStartup>
  );
}

function DatabaseStartup({
  children,
  databaseName,
  onRetry,
}: DrizzleDatabaseProviderProps & { onRetry: () => void }) {
  // Opened once per start-up attempt (lazy initial state), not on every render.
  const [opened] = useState(() => openDrizzleDatabase(databaseName));

  if (!opened.ok) {
    return (
      <DatabaseRecoveryScreen
        databaseName={databaseName}
        error={opened.error}
        onRetry={onRetry}
      />
    );
  }

  return (
    <DatabaseMigrator
      db={opened.db}
      databaseName={databaseName}
      onRetry={onRetry}
    >
      {children}
    </DatabaseMigrator>
  );
}

function DatabaseMigrator({
  children,
  db,
  databaseName,
  onRetry,
}: PropsWithChildren<{
  db: DrizzleDatabase;
  databaseName: string;
  onRetry: () => void;
}>) {
  const { success, error } = useMigrations(db, migrations);

  if (error) {
    return (
      <DatabaseRecoveryScreen
        databaseName={databaseName}
        error={error}
        onRetry={onRetry}
      />
    );
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
