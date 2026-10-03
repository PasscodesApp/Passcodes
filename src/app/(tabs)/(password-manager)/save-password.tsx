import CategoryField from "@/components/CategoryField";
import FormTextField from "@/components/FormTextField";
import SecureTextField from "@/components/SecureTextField";
import { usePasswordRepository } from "@/contexts/RepositoryContext";
import { useToast } from "@/contexts/ToastContext";
import { getScreenShotSecureScreen } from "@/libs/screenshot_prevention";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { router, useLocalSearchParams } from "expo-router";
import { useState } from "react";
import { ScrollView, View } from "react-native";
import { Button } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SavePasswordScreen() {
  let [domain, setDomain] = useState("");
  let [username, setUsername] = useState("");
  let [password, setPassword] = useState("");
  let [url, setUrl] = useState("");
  let [notes, setNotes] = useState("");

  // Preselect the category the user was viewing when they pressed "+".
  // (missing / invalid param = Uncategorized)
  const { categoryId: categoryIdParam } = useLocalSearchParams<{
    categoryId?: string;
  }>();
  let [categoryId, setCategoryId] = useState<number | null>(() => {
    const parsed = Number(categoryIdParam);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  });

  const passwordRepository = usePasswordRepository();

  async function handleSavePassword() {
    if (!domain || !username || !password) {
      showToast("Domain, Username and Password are required.", "error");
      return;
    }

    try {
      await passwordRepository.create({
        domain,
        username,
        password,
        notes,
        url,
        categoryId,
      });

      showToast("Password saved successfully");
      router.back();
    } catch (err) {
      console.error(err);
      showToast("Failed to save, please try again!!", "error");
    }
  }

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

        <CategoryField value={categoryId} onChange={setCategoryId} />

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
            onPress={handleSavePassword}
          >
            Save Password
          </Button>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
