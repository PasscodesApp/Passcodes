import { passwords } from "@/db/schema";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import { Button, Text, TextInput, View } from "react-native";

export default function SavePassword() {
  let [domain, setDomain] = useState("");
  let [username, setUsername] = useState("");
  let [password, setPassword] = useState("");
  let [url, setUrl] = useState("");
  let [notes, setNotes] = useState("");

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Text>Save Password</Text>

      <TextInput
        style={{ borderWidth: 2 }}
        value={domain}
        onChangeText={setDomain}
        placeholder="Enter your domain..."
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={username}
        onChangeText={setUsername}
        placeholder="Enter your username..."
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={password}
        onChangeText={setPassword}
        placeholder="Enter your passsword..."
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={url}
        onChangeText={setUrl}
        placeholder="Enter your url..."
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={notes}
        onChangeText={setNotes}
        placeholder="Enter your notes..."
      />

      <Button
        title="Save Password"
        onPress={() => {
          drizzleDb
            .insert(passwords)
            .values({
              domain,
              username,
              password,
              notes,
              url,
            })
            .then(() => router.back());
        }}
      />
    </View>
  );
}
