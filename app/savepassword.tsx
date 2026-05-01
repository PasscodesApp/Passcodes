import { router } from "expo-router";
import { useState } from "react";
import { Button, Text, TextInput, View } from "react-native";

function savePasswordToDB(
  domain: string,
  username: string,
  password: string,
  url: string,
  notes: string,
) {
  console.log("Save the infomation:", [domain, username, password, url, notes]);
}

export default function SavePassword() {
  let [domain, setDomain] = useState("");
  let [username, setUsername] = useState("");
  let [password, setPassword] = useState("");
  let [url, setUrl] = useState("");
  let [notes, setNotes] = useState("");

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
          savePasswordToDB(domain, username, password, url, notes);
          router.back();
        }}
      />
    </View>
  );
}
