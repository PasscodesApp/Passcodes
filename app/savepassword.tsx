import FormTextField from "@/components/FormTextField";
import { passwords } from "@/db/schema";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import { Alert, Button, ScrollView, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SavePassword() {
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
        <Text style={{ fontSize: 32, fontWeight: "bold", textAlign: "center" }}>
          Save Password
        </Text>

        <FormTextField
          label="Domain"
          value={domain}
          onChangeText={setDomain}
          placeholder="Enter your domain..."
          placeholderTextColor={"#000"}
        />

        <FormTextField
          label="Username"
          value={username}
          onChangeText={setUsername}
          placeholder="Enter your username..."
          placeholderTextColor={"#000"}
        />

        <FormTextField
          label="Password"
          value={password}
          onChangeText={setPassword}
          placeholder="Enter your password..."
          placeholderTextColor={"#000"}
        />

        <FormTextField
          label="URL"
          value={url}
          onChangeText={setUrl}
          placeholder="Enter your url..."
          placeholderTextColor={"#000"}
        />

        <FormTextField
          label="Notes"
          value={notes}
          onChangeText={setNotes}
          placeholder="Enter your notes..."
          placeholderTextColor={"#000"}
          multiline
        />

        <Button
          title="Save Password"
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
