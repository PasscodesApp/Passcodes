import FormTextField from "@/components/FormTextField";
import Text from "@/components/Text";
import {
  CATEGORY_NAME_MAX_LENGTH,
  CategoryValidationError,
} from "@/repositories/CategoryRepository";
import { useState } from "react";
import {
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  View,
} from "react-native";
import { Button, HelperText, useTheme } from "react-native-paper";

type CategoryNameDialogProps = {
  title: string;
  confirmLabel: string;
  initialName?: string;
  /**
   * Called with the typed name. Throw to keep the dialog open, a
   * `CategoryValidationError` message is shown under the field.
   * The PARENT closes the dialog after a successful submit.
   */
  onSubmit: (name: string) => Promise<void>;
  onCancel: () => void;
};

/**
 * Name dialog shared by "Create Category" (password form + manage screen)
 * and "Rename Category".
 *
 * Uses React Native's `Modal` (not Paper's Portal based Dialog) so it also
 * works on top of the native formSheet of the Add Password screen.
 */
export default function CategoryNameDialog({
  title,
  confirmLabel,
  initialName = "",
  onSubmit,
  onCancel,
}: CategoryNameDialogProps) {
  const theme = useTheme();

  const [name, setName] = useState(initialName);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit() {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setError("");

    try {
      await onSubmit(name);
    } catch (err) {
      if (err instanceof CategoryValidationError) {
        setError(err.message);
      } else {
        console.error("Category dialog failed:", err);
        setError("Something went wrong, please try again!!");
      }

      setIsSubmitting(false);
    }
  }

  return (
    <Modal
      visible
      transparent
      animationType="fade"
      statusBarTranslucent
      onRequestClose={onCancel}
    >
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <Pressable
          style={{
            flex: 1,
            backgroundColor: "rgba(0, 0, 0, 0.5)",
            justifyContent: "center",
            padding: 24,
          }}
          onPress={onCancel}
        >
          {/* Inner Pressable swallows touches so tapping the dialog doesn't close it. */}
          <Pressable
            onPress={() => {}}
            style={{
              backgroundColor: theme.colors.surface,
              borderRadius: 20,
              padding: 20,
              gap: 16,
            }}
          >
            <Text variant="titleLarge">{title}</Text>

            <View>
              <FormTextField
                label="Name"
                value={name}
                onChangeText={(text) => {
                  setName(text);
                  setError("");
                }}
                placeholder="Work, School, Games..."
                placeholderTextColor={"#9e9e9e"}
                maxLength={CATEGORY_NAME_MAX_LENGTH}
                autoFocus
                error={!!error}
                onSubmitEditing={handleSubmit}
              />

              <HelperText type="error" visible={!!error}>
                {error}
              </HelperText>
            </View>

            <View style={{ flexDirection: "row-reverse", gap: 4 }}>
              <Button
                mode="contained"
                onPress={handleSubmit}
                loading={isSubmitting}
                disabled={isSubmitting}
              >
                {confirmLabel}
              </Button>

              <Button onPress={onCancel} disabled={isSubmitting}>
                Cancel
              </Button>
            </View>
          </Pressable>
        </Pressable>
      </KeyboardAvoidingView>
    </Modal>
  );
}
