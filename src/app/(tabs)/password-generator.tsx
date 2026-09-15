import PasswordGeneratorScreenContent from "@/features/password-generator/ScreenContent";
import { SafeAreaView } from "react-native-safe-area-context";

export default function PasswordGeneratorScreen() {
  return (
    <SafeAreaView style={{ flex: 1, paddingVertical: 12 }}>
      <PasswordGeneratorScreenContent />
    </SafeAreaView>
  );
}
