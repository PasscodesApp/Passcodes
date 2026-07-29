import FormTextField from "@/components/FormTextField";
import SecureTextField from "@/components/SecureTextField";
import { passwords } from "@/db/schema";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router, useLocalSearchParams } from "expo-router";
import { usePreventScreenCapture } from "expo-screen-capture";
import { useSQLiteContext } from "expo-sqlite";
import { useEffect, useState } from "react";
import { ScrollView, View } from "react-native";
import { Button } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function PasswordDetailsScreen() {
  const { id } = useLocalSearchParams();
  usePreventScreenCapture();

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const [domain, setDomain] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [url, setUrl] = useState("");
  const [notes, setNotes] = useState("");
  const [isEditing, setIsEditing] = useState(false);

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
    }
  }

  async function updatePassword() {
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

    router.back();
  }

  useEffect(() => {
    loadAndRefreshPassword();
  }, []);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView
        contentContainerStyle={{
          margin: 20,
          gap: 24,
        }}
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

        <View
          style={{
            flex: 1,
            flexDirection: "row-reverse",
            gap: 4,
          }}
        >
          {!isEditing ? (
            <Button
              mode="contained-tonal"
              icon={({ size, color }) => (
                <FontAwesome6
                  name="pencil"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={() => setIsEditing(true)}
            >
              Edit
            </Button>
          ) : (
            <>
              <Button
                mode="contained-tonal"
                icon={({ size, color }) => (
                  <FontAwesome6
                    name="store"
                    size={size}
                    color={color}
                    iconStyle="solid"
                  />
                )}
                onPress={() => updatePassword()}
              >
                Save
              </Button>

              <Button
                icon={({ size, color }) => (
                  <FontAwesome6
                    name="xmark"
                    size={size}
                    color={color}
                    iconStyle="solid"
                  />
                )}
                onPress={() => {
                  setIsEditing(false);
                  loadAndRefreshPassword();
                }}
              >
                Cancel
              </Button>
            </>
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
