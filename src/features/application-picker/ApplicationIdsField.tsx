import FormTextField from "@/components/FormTextField";
import Text from "@/components/Text";
import {
  formatApplicationIds,
  parseApplicationIds,
} from "@/libs/application_ids";
import { useState } from "react";
import { View } from "react-native";
import { Button, useTheme } from "react-native-paper";

import ApplicationPickerModal from "./ApplicationPickerModal";
import { isInstalledApplicationsAvailable } from "./installedApps";
import { useApplicationLabels } from "./useApplicationLabels";

type Props = {
  value: string;
  onChangeText: (text: string) => void;
  editable?: boolean;
};

/**
 * Application IDs input. On Android it also offers "Choose from installed
 * apps" and shows the names of the apps behind the typed IDs.
 */
export default function ApplicationIdsField({
  value,
  onChangeText,
  editable = true,
}: Props) {
  const theme = useTheme();
  const [pickerOpen, setPickerOpen] = useState(false);
  const labels = useApplicationLabels(value);
  const names = parseApplicationIds(value)
    .ids.map((id) => labels[id])
    .filter(Boolean);

  return (
    <View style={{ gap: 4 }}>
      <FormTextField
        label="Application IDs"
        value={value}
        onChangeText={onChangeText}
        editable={editable}
        placeholder="com.example.app, com.example.beta"
        placeholderTextColor={"#9e9e9e"}
        autoCapitalize="none"
        autoCorrect={false}
        multiline
      />

      {names.length > 0 ? (
        <Text
          variant="bodySmall"
          style={{ color: theme.colors.onSurfaceVariant, paddingHorizontal: 4 }}
        >
          {names.join(", ")}
        </Text>
      ) : null}

      {isInstalledApplicationsAvailable && editable ? (
        <View style={{ flexDirection: "row" }}>
          <Button mode="text" compact onPress={() => setPickerOpen(true)}>
            Choose from installed apps
          </Button>
        </View>
      ) : null}

      {pickerOpen ? (
        <ApplicationPickerModal
          currentText={value}
          onConfirm={(ids) => onChangeText(formatApplicationIds(ids.join(",")))}
          onClose={() => setPickerOpen(false)}
        />
      ) : null}
    </View>
  );
}
