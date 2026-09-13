import { createContext, PropsWithChildren, useContext, useMemo } from "react";

import DrizzleDatabaseProvider, { useDrizzleDatabase } from "@/db/provider";
import { PasswordRepository } from "@/repositories/PasswordRepository";

type Repositories = {
  password: PasswordRepository;
};

const RepositoryContext = createContext<Repositories | undefined>(undefined);

function RepositoryProviderContent({
  children,
}: {
  children: React.ReactNode;
}) {
  const db = useDrizzleDatabase();

  const repositories = useMemo(
    () => ({
      password: new PasswordRepository(db),
    }),
    [db],
  );

  return (
    <RepositoryContext.Provider value={repositories}>
      {children}
    </RepositoryContext.Provider>
  );
}

export function RepositoryProvider({
  dbName,
  children,
}: PropsWithChildren<{ dbName: string }>) {
  return (
    <DrizzleDatabaseProvider databaseName={dbName}>
      <RepositoryProviderContent>{children}</RepositoryProviderContent>
    </DrizzleDatabaseProvider>
  );
}

export function usePasswordRepository(): PasswordRepository {
  const repositories = useContext(RepositoryContext);

  if (!repositories) {
    throw new Error(
      "usePasswordRepository must be used inside RepositoryProvider",
    );
  }

  return repositories.password;
}
