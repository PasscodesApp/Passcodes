import Text from "@/components/Text";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import { unlockWithBiometricsApp } from "@/libs/biometric";
import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6";
import { useFocusEffect } from "expo-router";
import { useCallback, useRef, useState } from "react";
import { Alert, ScrollView } from "react-native";
import { Button, Card, useTheme } from "react-native-paper";

import FormatSelector from "../components/FormatSelector";
import { saveTextFile } from "../files";
import { exportPasswords } from "../services/exportService";
import type { ExportOutcome, TransferFormat } from "../types";

export default function ExportScreenContent() {
  const theme = useTheme();
  const passwordRepository = usePasswordRepository();
  const categoryRepository = useCategoryRepository();

  const [format, setFormat] = useState<TransferFormat>("google-csv");
  const [busy, setBusy] = useState(false);
  const [outcome, setOutcome] = useState<ExportOutcome | null>(null);

  // undefined = loading, null = the count could not be read.
  const [count, setCount] = useState<number | null | undefined>(undefined);

  const runningRef = useRef(false);

  // Re-read from the repository whenever the screen is shown, so the number
  // is never a stale copy.
  const refreshCount = useCallback(() => {
    passwordRepository
      .count()
      .then(setCount)
      .catch(() => setCount(null));
  }, [passwordRepository]);

  useFocusEffect(refreshCount);

  async function handleExport() {
    if (runningRef.current) {
      return;
    }

    runningRef.current = true;
    setBusy(true);
    setOutcome(null);

    try {
      const unlocked = await unlockWithBiometricsApp();

      if (!unlocked) {
        Alert.alert(
          "Authentication Failed",
          "Exporting passwords is protected by App Lock.",
        );
        return;
      }

      setOutcome(
        await exportPasswords(
          format,
          { passwords: passwordRepository, categories: categoryRepository },
          saveTextFile,
        ),
      );
    } finally {
      runningRef.current = false;
      setBusy(false);
      refreshCount();
    }
  }

  const countLabel =
    count === undefined
      ? "Counting passwords…"
      : count === null
        ? "The number of passwords couldn't be read."
        : count === 0
          ? "You have no passwords to export yet."
          : `${count} ${count === 1 ? "password" : "passwords"} will be exported.`;

  return (
    <ScrollView contentContainerStyle={{ padding: 16, gap: 16 }}>
      <Text
        variant="bodyMedium"
        style={{ color: theme.colors.onSurfaceVariant }}
      >
        Choose a format. All of your stored passwords are exported
        {format === "passcodes-json" ? ", together with your categories" : ""}.
      </Text>

      <FormatSelector value={format} onChange={setFormat} disabled={busy} />

      <Card>
        <Card.Content style={{ gap: 8 }}>
          <Text variant="titleMedium" accessibilityLiveRegion="polite">
            {countLabel}
          </Text>
          <Text
            variant="bodySmall"
            style={{ color: theme.colors.onSurfaceVariant }}
          >
            The exported file is not encrypted. Anyone who gets the file can
            read your passwords, so keep it somewhere safe.
          </Text>
        </Card.Content>
      </Card>

      <Button
        mode="contained"
        icon={({ size, color }) => (
          <FontAwesome6
            name="file-export"
            iconStyle="solid"
            size={size}
            color={color}
          />
        )}
        onPress={handleExport}
        loading={busy}
        disabled={busy || count === 0 || count === undefined}
        accessibilityLabel="Export passwords"
      >
        {busy ? "Exporting…" : "Export"}
      </Button>

      {outcome ? <ExportStatus outcome={outcome} /> : null}
    </ScrollView>
  );
}

function ExportStatus({ outcome }: { outcome: ExportOutcome }) {
  const theme = useTheme();

  return (
    <Card>
      <Card.Content style={{ gap: 8 }}>
        {outcome.status === "failed" ? (
          <>
            <Text variant="titleMedium" style={{ color: theme.colors.error }}>
              Export failed
            </Text>
            <Text accessibilityLiveRegion="polite">{outcome.message}</Text>
          </>
        ) : outcome.status === "empty" ? (
          <>
            <Text variant="titleMedium">Nothing to export</Text>
            <Text accessibilityLiveRegion="polite">
              You have no passwords to export yet.
            </Text>
          </>
        ) : outcome.status === "cancelled" ? (
          <>
            <Text variant="titleMedium">Export cancelled</Text>
            <Text accessibilityLiveRegion="polite">Nothing was saved.</Text>
          </>
        ) : (
          <>
            <Text variant="titleMedium">Export saved</Text>
            <Text accessibilityLiveRegion="polite">
              {`${outcome.count} ${outcome.count === 1 ? "password was" : "passwords were"} saved to the location you chose.`}
            </Text>
          </>
        )}
      </Card.Content>
    </Card>
  );
}
