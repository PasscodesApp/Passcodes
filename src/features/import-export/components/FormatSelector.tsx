import Text from "@/components/Text";
import { View } from "react-native";
import { SegmentedButtons, useTheme } from "react-native-paper";

import { TRANSFER_FORMAT_OPTIONS, type TransferFormat } from "../types";

interface FormatSelectorProps {
  value: TransferFormat;
  onChange: (format: TransferFormat) => void;
  disabled?: boolean;
}

export default function FormatSelector({
  value,
  onChange,
  disabled = false,
}: FormatSelectorProps) {
  const theme = useTheme();
  const selected = TRANSFER_FORMAT_OPTIONS.find(
    (option) => option.value === value,
  );

  return (
    <View style={{ gap: 8 }}>
      <SegmentedButtons
        value={value}
        onValueChange={(next) => onChange(next as TransferFormat)}
        buttons={TRANSFER_FORMAT_OPTIONS.map((option) => ({
          value: option.value,
          label: option.label,
          disabled,
          accessibilityLabel: option.label,
        }))}
      />

      {selected ? (
        <Text
          variant="bodySmall"
          style={{ color: theme.colors.onSurfaceVariant }}
        >
          {selected.description}
        </Text>
      ) : null}
    </View>
  );
}
