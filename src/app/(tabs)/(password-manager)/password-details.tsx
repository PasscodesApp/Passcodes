import PasswordDetailCardActions from "@/components/PasswordDetailCardActions";
import PasswordFormCard, {
  usePasswordForm,
} from "@/components/PasswordFormCard";
import { useToast } from "@/contexts/ToastContext";

import { passwords } from "@/db/schema";
import { getScreenShotSecureScreen } from "@/libs/screenshot_prevention";
import { formatDate } from "@passcodes/passalgo";

import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { Href, router, useLocalSearchParams } from "expo-router";
import { usePreventRemove } from "expo-router/react-navigation";
import { useSQLiteContext } from "expo-sqlite";

import { useEffect, useState } from "react";
import { Keyboard, ScrollView, TextInput } from "react-native";

import { FAB } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function PasswordDetailsScreen() {
  const { id } = useLocalSearchParams();

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const { state, setState, updateField } = usePasswordForm();
  const [isEditing, setIsEditing] = useState(false);

  const { showToast } = useToast();

  getScreenShotSecureScreen();

  /**
   * Remove focus from whichever TextInput is currently focused
   * and dismiss the keyboard.
   *
   * This is important because Keyboard.dismiss() alone can leave
   * the native TextInput focused, which can leave the focus outline
   * visible after leaving edit mode.
   */
  function unfocusAllFields() {
    TextInput.State.currentlyFocusedInput()?.blur();
    Keyboard.dismiss();
  }

  async function loadAndRefreshPassword() {
    const result = await drizzleDb
      .select()
      .from(passwords)
      .where(eq(passwords.id, Number(id)));

    if (result.length > 0) {
      const data = result[0];

      setState({
        domain: data.domain || "",
        username: data.username || "",
        password: data.password || "",
        url: data.url || "",
        notes: data.notes || "",
        updatedAt: data.updatedAt ? formatDate(data.updatedAt) : "just now",
      });
    }
  }

  async function updatePassword() {
    /*
     * Blur BEFORE changing isEditing.
     *
     * This prevents the TextInput from remaining focused when
     * it becomes read-only.
     */
    unfocusAllFields();

    try {
      await drizzleDb
        .update(passwords)
        .set({ ...state, updatedAt: new Date().toISOString() })
        .where(eq(passwords.id, Number(id)));

      setIsEditing(false);

      await loadAndRefreshPassword();

      showToast("Password updated successfully");
    } catch (error) {
      showToast("Failed to update; please try again", "error");
      console.error("Failed to update password:", error);
    }
  }

  function handleCancel() {
    /*
     * Remove focus before switching the inputs to read-only.
     */
    unfocusAllFields();

    setIsEditing(false);

    /*
     * Restore the values from the database.
     */
    loadAndRefreshPassword();

    showToast("Changes discarded");
  }

  function handleFabPress() {
    if (isEditing) {
      updatePassword();
    } else {
      setIsEditing(true);
    }
  }

  usePreventRemove(isEditing, () => handleCancel());

  useEffect(() => {
    loadAndRefreshPassword();
  }, []);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView
        contentContainerStyle={{
          margin: 20,
          gap: 24,
          paddingBottom: 100,
        }}
        keyboardShouldPersistTaps="handled"
      >
        <PasswordFormCard
          value={state}
          editable={isEditing}
          onChange={updateField}
        />

        <PasswordDetailCardActions
          isEditing={isEditing}
          onOpen={() => router.navigate(state.url as Href)}
          onCancel={handleCancel}
        />
      </ScrollView>

      <FAB
        style={{
          position: "absolute",
          bottom: 35,
          right: 35,
        }}
        icon={({ size, color }) => (
          <FontAwesome6
            name={isEditing ? "check" : "pencil"}
            size={size}
            color={color}
            iconStyle="solid"
          />
        )}
        onPress={handleFabPress}
      />
    </SafeAreaView>
  );
}
