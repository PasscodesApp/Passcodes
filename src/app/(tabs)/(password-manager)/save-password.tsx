import FormTextField from "@/components/FormTextField";
import SecureTextField from "@/components/SecureTextField";
import { useToast } from "@/contexts/ToastContext";
import { passwords } from "@/db/schema";
import { getScreenShotSecureScreen } from "@/libs/screenshot_prevention";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import { Alert, ScrollView, View } from "react-native";
import { Button } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SavePasswordScreen() {
  let [domain, setDomain] = useState("");
  let [username, setUsername] = useState("");
  let [password, setPassword] = useState("");
  let [url, setUrl] = useState("");
  let [notes, setNotes] = useState("");

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const { showToast } = useToast();

  getScreenShotSecureScreen();

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView
        contentContainerStyle={{
          padding: 20,
          gap: 16,
        }}
      >
        <FormTextField
          label="Domain"
          value={domain}
          onChangeText={setDomain}
          placeholder="google, instagram, whatsapp...."
          placeholderTextColor={"#9e9e9e"}
        />

        <FormTextField
          label="Username"
          value={username}
          onChangeText={setUsername}
          placeholder="alan24_st, olivia_12, ava2026@gmail.com..."
          placeholderTextColor={"#9e9e9e"}
          autoCapitalize="none"
          autoCorrect={false}
        />

        <SecureTextField
          label="Password"
          value={password}
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
          autoCapitalize="none"
          autoCorrect={false}
        />

        <FormTextField
          label="Notes"
          value={notes}
          onChangeText={setNotes}
          placeholder="your nonsense..."
          placeholderTextColor={"#9e9e9e"}
          multiline
        />

        <View
          style={{
            margin: 20,
            alignItems: "center",
          }}
        >
          <Button
            mode="contained"
            icon={({ size, color }) => (
              <FontAwesome6
                name="store"
                size={size}
                color={color}
                iconStyle="solid"
              />
            )}
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
                .then(() => {
                  showToast("Password saved successfully");
                  router.back();
                })
                .catch((err) => {
                  console.error(err);
                  showToast("Failed to save, please try again!!", "error");
                  Alert.alert("Error", "Failed to save password.");
                });
            }}
          >
            Save Password
          </Button>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
