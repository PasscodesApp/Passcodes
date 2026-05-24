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
        gap: 5,
      }}
    >
      <Text style={{ fontSize: 36 }}>Save Password</Text>

      <TextInput
        style={{ borderWidth: 2 }}
        value={domain}
        onChangeText={setDomain}
        placeholder="Enter your domain..."
        placeholderTextColor={"#000"}
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={username}
        onChangeText={setUsername}
        placeholder="Enter your username..."
        placeholderTextColor={"#000"}
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={password}
        onChangeText={setPassword}
        placeholder="Enter your passsword..."
        placeholderTextColor={"#000"}
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={url}
        onChangeText={setUrl}
        placeholder="Enter your url..."
        placeholderTextColor={"#000"}
      />
      <TextInput
        style={{ borderWidth: 2 }}
        value={notes}
        onChangeText={setNotes}
        placeholder="Enter your notes..."
        placeholderTextColor={"#000"}
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
