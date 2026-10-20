import Text from "@/components/Text";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import {
  confirmIdentity,
  describeAuthFailure,
  isAuthenticated,
} from "@/features/biometrics";
import { router, useNavigation } from "expo-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { Alert, FlatList, View } from "react-native";
import { Button, Card, useTheme } from "react-native-paper";

import ConflictGroupCard from "../components/ConflictGroupCard";
import {
  clearImportSession,
  setImportOutcome,
  useImportSession,
} from "../importSession";
import { commitImport } from "../services/importService";
import { defaultDecisions } from "../utils/duplicates";

function toggled<T>(set: ReadonlySet<T>, value: T): Set<T> {
  const next = new Set(set);

  if (next.has(value)) {
    next.delete(value);
  } else {
    next.add(value);
  }

  return next;
}

/**
 * Lets the user decide what to do with accounts that exist in more than one
 * version. Default: keep both versions of everything, remove nothing.
 */
export default function ImportReviewScreenContent() {
  const theme = useTheme();
  const navigation = useNavigation();
  const passwordRepository = usePasswordRepository();
  const categoryRepository = useCategoryRepository();
  const session = useImportSession();

  const [importIndexes, setImportIndexes] = useState<ReadonlySet<number>>(() =>
    session ? defaultDecisions(session.plan).importIndexes : new Set(),
  );
  const [removeIds, setRemoveIds] = useState<ReadonlySet<number>>(new Set());
  const [busy, setBusy] = useState(false);
  const runningRef = useRef(false);

  // The session holds plaintext passwords: drop it whenever this screen is
  // left (back button, gesture, or after finishing).
  useEffect(() => {
    return navigation.addListener("beforeRemove", () => {
      clearImportSession();
    });
  }, [navigation]);

  const existingById = useMemo(
    () => new Map((session?.existing ?? []).map((row) => [row.id, row])),
    [session],
  );

  if (!session) {
    return (
      <View style={{ flex: 1, padding: 16, gap: 16, justifyContent: "center" }}>
        <Text style={{ textAlign: "center" }}>
          There is nothing to review. Start an import first.
        </Text>
        <Button mode="outlined" onPress={() => router.back()}>
          Back
        </Button>
      </View>
    );
  }

  const { plan, parsed } = session;
  const allConflictIndexes = plan.conflicts.flatMap((g) => g.incomingIndexes);
  const importCount = plan.fresh.length + importIndexes.size;

  async function handleConfirm() {
    if (!session || runningRef.current) {
      return;
    }

    runningRef.current = true;
    setBusy(true);

    try {
      if (removeIds.size > 0) {
        const identity = await confirmIdentity(
          "Authenticate to remove existing passwords",
        );

        if (!isAuthenticated(identity)) {
          // `null` = the user simply cancelled: nothing to tell them.
          const reason = describeAuthFailure(identity);

          if (reason) {
            Alert.alert("Authentication Failed", reason);
          }

          return;
        }
      }

      const outcome = await commitImport(
        session,
        { importIndexes, removeExistingIds: removeIds },
        { passwords: passwordRepository, categories: categoryRepository },
      );

      setImportOutcome(outcome);
      router.back();
    } finally {
      runningRef.current = false;
      setBusy(false);
    }
  }

  function handleCancel() {
    setImportOutcome({ status: "cancelled" });
    router.back();
  }

  const muted = { color: theme.colors.onSurfaceVariant };

  return (
    <View style={{ flex: 1 }}>
      <FlatList
        data={plan.conflicts}
        keyExtractor={(group) => group.key}
        extraData={[importIndexes, removeIds, busy]}
        contentContainerStyle={{ padding: 16, gap: 12 }}
        ListHeaderComponent={
          <View style={{ gap: 12, marginBottom: 4 }}>
            <Card>
              <Card.Content style={{ gap: 8 }}>
                <Text variant="titleMedium">
                  {`${plan.conflicts.length} ${plan.conflicts.length === 1 ? "account has" : "accounts have"} more than one version`}
                </Text>
                <Text variant="bodySmall" style={muted}>
                  These have the same domain and username as a password you
                  already have (or appear twice in the file) but with a
                  different password. By default both versions are kept so you
                  can clean up later. Switch &ldquo;Import&rdquo; off to leave
                  a version out, or &ldquo;Remove&rdquo; on to delete the
                  version on this device once the imported one is saved.
                </Text>
                <Text variant="bodySmall" style={muted}>
                  {`${plan.fresh.length} other ${plan.fresh.length === 1 ? "password is" : "passwords are"} new and will be imported. ${plan.duplicates} exact ${plan.duplicates === 1 ? "duplicate is" : "duplicates are"} skipped. ${parsed.failures.filter((f) => f.table === "passwords").length} invalid ${parsed.failures.filter((f) => f.table === "passwords").length === 1 ? "record" : "records"} will not be imported.`}
                </Text>
              </Card.Content>
            </Card>

            <View style={{ flexDirection: "row", gap: 8 }}>
              <Button
                mode="outlined"
                style={{ flex: 1 }}
                disabled={busy}
                onPress={() => setImportIndexes(new Set(allConflictIndexes))}
              >
                Keep all
              </Button>
              <Button
                mode="outlined"
                style={{ flex: 1 }}
                disabled={busy}
                onPress={() => {
                  setImportIndexes(new Set());
                  setRemoveIds(new Set());
                }}
              >
                Skip all
              </Button>
            </View>
          </View>
        }
        renderItem={({ item }) => (
          <ConflictGroupCard
            group={item}
            passwords={parsed.passwords}
            existingById={existingById}
            importIndexes={importIndexes}
            removeIds={removeIds}
            onToggleImport={(index) =>
              setImportIndexes((current) => toggled(current, index))
            }
            onToggleRemove={(id) =>
              setRemoveIds((current) => toggled(current, id))
            }
            disabled={busy}
          />
        )}
      />

      <View
        style={{
          padding: 16,
          gap: 8,
          backgroundColor: theme.colors.surface,
        }}
      >
        <Button
          mode="contained"
          onPress={handleConfirm}
          loading={busy}
          disabled={busy}
          accessibilityLabel="Import the selected passwords"
        >
          {busy
            ? "Importing…"
            : `Import ${importCount} ${importCount === 1 ? "password" : "passwords"}`}
        </Button>
        <Button mode="text" onPress={handleCancel} disabled={busy}>
          Cancel import
        </Button>
      </View>
    </View>
  );
}
