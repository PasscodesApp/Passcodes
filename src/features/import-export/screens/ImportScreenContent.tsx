import Text from "@/components/Text";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6";
import { router } from "expo-router";
import { useEffect, useRef, useState } from "react";
import { ScrollView } from "react-native";
import { Button, useTheme } from "react-native-paper";

import FormatSelector from "../components/FormatSelector";
import ImportSummary from "../components/ImportSummary";
import { pickImportText } from "../files";
import {
  setImportOutcome,
  setImportSession,
  useImportOutcome,
} from "../importSession";
import { commitImport, prepareImport } from "../services/importService";
import type { TransferFormat } from "../types";
import { defaultDecisions } from "../utils/duplicates";

export default function ImportScreenContent() {
  const theme = useTheme();
  const passwordRepository = usePasswordRepository();
  const categoryRepository = useCategoryRepository();

  const [format, setFormat] = useState<TransferFormat>("google-csv");
  const [busy, setBusy] = useState(false);
  const outcome = useImportOutcome();

  // A ref, not state: blocks a second tap before React re-renders.
  const runningRef = useRef(false);

  // Start clean, and don't show an old result the next time the screen opens.
  useEffect(() => {
    setImportOutcome(null);
    return () => setImportOutcome(null);
  }, []);

  async function handleImport() {
    if (runningRef.current) {
      return;
    }

    runningRef.current = true;
    setBusy(true);
    setImportOutcome(null);

    const repositories = {
      passwords: passwordRepository,
      categories: categoryRepository,
    };

    try {
      const prepared = await prepareImport(
        format,
        repositories,
        pickImportText,
      );

      if (prepared.status !== "ready") {
        setImportOutcome(prepared);
        return;
      }

      const { session } = prepared;

      if (session.plan.conflicts.length > 0) {
        // The user decides; the review screen reports the final result.
        setImportSession(session);
        router.push("/import-review");
        return;
      }

      setImportOutcome(
        await commitImport(
          session,
          defaultDecisions(session.plan),
          repositories,
        ),
      );
    } finally {
      runningRef.current = false;
      setBusy(false);
    }
  }

  return (
    <ScrollView contentContainerStyle={{ padding: 16, gap: 16 }}>
      <Text
        variant="bodyMedium"
        style={{ color: theme.colors.onSurfaceVariant }}
      >
        Choose a format, then select the file to import. Imported passwords are
        added to the ones you already have.
      </Text>

      <Text
        variant="bodySmall"
        style={{ color: theme.colors.onSurfaceVariant }}
      >
        Passwords that already exist with the same domain, username and password
        are skipped. If the same domain and username has a different password,
        you can review those before anything is imported.
      </Text>

      <FormatSelector value={format} onChange={setFormat} disabled={busy} />

      <Button
        mode="contained"
        icon={({ size, color }) => (
          <FontAwesome6
            name="file-import"
            iconStyle="solid"
            size={size}
            color={color}
          />
        )}
        onPress={handleImport}
        loading={busy}
        disabled={busy}
        accessibilityLabel="Select a file to import"
      >
        {busy ? "Working…" : "Select file"}
      </Button>

      {outcome && outcome.status !== "cancelled" ? (
        <ImportSummary outcome={outcome} />
      ) : null}
    </ScrollView>
  );
}
