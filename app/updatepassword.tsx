import FormTextField from "@/components/FormTextField";
import ScreenHeading from "@/components/ScreenHeading";
import { passwords } from "@/db/schema";
import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router, useLocalSearchParams } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useEffect, useState } from "react";
import { Button, ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function UpdatePassword() {
  const { id } = useLocalSearchParams();

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
          padding: 20,
          gap: 16,
        }}
      >
        <ScreenHeading title="Update Password" />

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
        />

        <FormTextField
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
        />

        <FormTextField
          label="Notes"
          value={notes}
          onChangeText={setNotes}
          editable={isEditing}
          multiline
        />

        {!isEditing ? (
          <Button title="Edit Password" onPress={() => setIsEditing(true)} />
        ) : (
          <>
            <Button title="Update Password" onPress={updatePassword} />

            <Button
              title="Cancel"
              onPress={() => {
                setIsEditing(false);
                loadAndRefreshPassword();
              }}
            />
          </>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}
