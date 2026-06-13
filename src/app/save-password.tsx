import FormTextField from "@/components/FormTextField";
import ScreenHeading from "@/components/ScreenHeading";
import { passwords } from "@/db/schema";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import { Alert, Button, ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SavePasswordScreen() {
  let [domain, setDomain] = useState("");
  let [username, setUsername] = useState("");
  let [password, setPassword] = useState("");
  let [url, setUrl] = useState("");
  let [notes, setNotes] = useState("");

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView
        contentContainerStyle={{
          padding: 20,
          gap: 16,
        }}
      >
        <ScreenHeading title="New Password" />

        <FormTextField
          label="Domain"
          value={domain}
          isRequired={true}
          onChangeText={setDomain}
          placeholder="google, instagram, whatsapp...."
          placeholderTextColor={"#9e9e9e"}
        />

        <FormTextField
          label="Username"
          value={username}
          isRequired={true}
          onChangeText={setUsername}
          placeholder="alan24_st, olivia_12, ava2026@gmail.com..."
          placeholderTextColor={"#9e9e9e"}
        />

        <FormTextField
          label="Password"
          value={password}
          isRequired={true}
          onChangeText={setPassword}
          placeholder="************"
          placeholderTextColor={"#9e9e9e"}
        />

        <FormTextField
          label="URL"
          value={url}
          onChangeText={setUrl}
          placeholder="https://..."
          placeholderTextColor={"#9e9e9e"}
        />

        <FormTextField
          label="Notes"
          value={notes}
          onChangeText={setNotes}
          placeholder="your nonsense..."
          placeholderTextColor={"#9e9e9e"}
          multiline
        />

        <Button
          title="Save"
          onPress={() => {
            if (!domain || !username || !password) {
              Alert.alert(
                "Missing Fields",
                "Domain, Username and Password are required.",
              );
              return;
            }

            drizzleDb
              .insert(passwords)
              .values({
                domain,
                username,
                password,
                notes,
                url,
              })
              .then(() => router.back())
              .catch((err) => {
                console.error(err);
                Alert.alert("Error", "Failed to save password.");
              });
          }}
        />
      </ScrollView>
    </SafeAreaView>
  );
}
