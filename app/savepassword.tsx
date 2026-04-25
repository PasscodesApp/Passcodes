import { router } from "expo-router";
import { useState } from "react";
import { Button, Text, TextInput, View } from "react-native";

export default function SavePassword() {
  let [domain, setDomain] = useState("Domain:");
  let [username, setUsername] = useState("Username:");
  let [password, setPassword] = useState("Password:");
  let [url, setUrl] = useState("URL:");
  let [notes, setNotes] = useState("Notes:");

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Text>Save Password</Text>

      <TextInput value={domain} onChangeText={setDomain} />
      <TextInput value={username} onChangeText={setUsername} />
      <TextInput value={password} onChangeText={setPassword} />
      <TextInput value={url} onChangeText={setUrl} />
      <TextInput value={notes} onChangeText={setNotes} />

      <Button title="Save Password" onPress={() => router.back()} />
    </View>
  );
}
