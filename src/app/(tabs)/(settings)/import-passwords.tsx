import { ImportScreenContent } from "@/features/import-export";
import { SafeAreaView } from "react-native-safe-area-context";

export default function ImportPasswordsScreen() {
  return (
    <SafeAreaView style={{ flex: 1 }} edges={["left", "right", "bottom"]}>
      <ImportScreenContent />
    </SafeAreaView>
  );
}
