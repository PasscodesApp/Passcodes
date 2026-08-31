import FormTextField from "@/components/FormTextField";
import SecureTextField from "@/components/SecureTextField";
import { useToast } from "@/contexts/ToastContext";

import { passwords } from "@/db/schema";
import { getScreenShotSecureScreen } from "@/libs/screenshot_prevention";
import formatDate from "@/utils/formating";

import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { Href, router, useLocalSearchParams } from "expo-router";
import { usePreventRemove } from "expo-router/react-navigation";
import { useSQLiteContext } from "expo-sqlite";

import { useEffect, useState } from "react";
import { Keyboard, ScrollView, TextInput, View } from "react-native";

import { Button, FAB } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function PasswordDetailsScreen() {
  const { id } = useLocalSearchParams();

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const [domain, setDomain] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [url, setUrl] = useState("");
  const [notes, setNotes] = useState("");
  const [lastupdateAt, setLastupdateAt] = useState("");

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

      setDomain(data.domain || "");
      setUsername(data.username || "");
      setPassword(data.password || "");
      setUrl(data.url || "");
      setNotes(data.notes || "");
      setLastupdateAt(data.updatedAt ? formatDate(data.updatedAt) : "just now");
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

    await drizzleDb
      .update(passwords)
      .set({
        domain,
        username,
        password,
        url,
        notes,
        updatedAt: new Date().toISOString(),
      })
      .where(eq(passwords.id, Number(id)));

    setIsEditing(false);

    await loadAndRefreshPassword();

    showToast("Password saved successfully");
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
        <FormTextField
          label="Domain"
          value={domain}
          onChangeText={setDomain}
          editable={isEditing}
        />

        <FormTextField
          label="Username"
          value={username}
          onChangeText={setUsername}
          editable={isEditing}
          autoCapitalize="none"
          autoCorrect={false}
        />

        <SecureTextField
          label="Password"
          value={password}
          onChangeText={setPassword}
          editable={isEditing}
        />

        <FormTextField
          label="URL"
          value={url}
          onChangeText={setUrl}
          editable={isEditing}
          autoCapitalize="none"
          autoCorrect={false}
        />

        <FormTextField
          label="Notes"
          value={notes}
          onChangeText={setNotes}
          editable={isEditing}
          multiline
          numberOfLines={3}
        />

        <FormTextField
          label="UpdatedAt"
          value={lastupdateAt}
          editable={false}
        />

        <View
          style={{
            flex: 1,
            flexDirection: "row-reverse",
            gap: 4,
          }}
        >
          {!isEditing && (
            <Button
              icon={({ size, color }) => (
                <FontAwesome6
                  name="link"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={() => router.navigate(url as Href)}
            >
              Open
            </Button>
          )}

          {isEditing && (
            <Button
              icon={({ size, color }) => (
                <FontAwesome6
                  name="xmark"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={handleCancel}
            >
              Cancel
            </Button>
          )}
        </View>
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
