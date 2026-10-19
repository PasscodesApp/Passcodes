import Text from "@/components/Text";
import { View } from "react-native";
import { Card, useTheme } from "react-native-paper";

import type { ImportOutcome } from "../types";

interface ImportSummaryProps {
  /** A cancelled picker shows nothing, so it is not accepted here. */
  outcome: Exclude<ImportOutcome, { status: "cancelled" }>;
}

function Row({ label, value }: { label: string; value: number }) {
  return (
    <View style={{ flexDirection: "row", justifyContent: "space-between" }}>
      <Text>{label}</Text>
      <Text style={{ fontWeight: "600" }}>{value}</Text>
    </View>
  );
}

export default function ImportSummary({ outcome }: ImportSummaryProps) {
  const theme = useTheme();

  if (outcome.status === "failed") {
    return (
      <Card>
        <Card.Content style={{ gap: 8 }}>
          <Text variant="titleMedium" style={{ color: theme.colors.error }}>
            Import failed
          </Text>
          <Text accessibilityLiveRegion="polite">{outcome.message}</Text>
        </Card.Content>
      </Card>
    );
  }

  if (outcome.status === "empty") {
    return (
      <Card>
        <Card.Content style={{ gap: 8 }}>
          <Text variant="titleMedium">Nothing to import</Text>
          <Text accessibilityLiveRegion="polite">
            No passwords were found in that file.
          </Text>
        </Card.Content>
      </Card>
    );
  }

  const muted = { color: theme.colors.onSurfaceVariant };

  return (
    <Card>
      <Card.Content style={{ gap: 12 }}>
        <Text variant="titleMedium">Import finished</Text>

        <View accessible accessibilityLiveRegion="polite" style={{ gap: 4 }}>
          <Row label="Found in file" value={outcome.detected} />
          <Row label="Imported" value={outcome.imported} />
          <Row label="Skipped" value={outcome.skipped} />
          <Row label="Failed" value={outcome.failed} />
          {outcome.categoriesCreated > 0 ? (
            <Row label="Categories created" value={outcome.categoriesCreated} />
          ) : null}
          {outcome.removed > 0 ? (
            <Row label="Existing passwords removed" value={outcome.removed} />
          ) : null}
        </View>

        {outcome.skipped > 0 ? (
          <Text variant="bodySmall" style={muted}>
            Skipped passwords were already on this device or were left out
            during the review.
          </Text>
        ) : null}

        {outcome.failureSummary.length > 0 ? (
          <View style={{ gap: 2 }}>
            <Text variant="labelLarge">Why some records failed</Text>
            {outcome.failureSummary.map((line) => (
              <Text key={line.message} variant="bodySmall" style={muted}>
                {`${line.message}: ${line.count}`}
              </Text>
            ))}
          </View>
        ) : null}

        {outcome.warnings.length > 0 ? (
          <View style={{ gap: 2 }}>
            <Text variant="labelLarge">Notes</Text>
            {outcome.warnings.map((warning) => (
              <Text key={warning} variant="bodySmall" style={muted}>
                {warning}
              </Text>
            ))}
          </View>
        ) : null}
      </Card.Content>
    </Card>
  );
}
