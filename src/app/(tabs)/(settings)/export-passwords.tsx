import { ExportScreenContent } from "@/features/import-export";
import { SafeAreaView } from "react-native-safe-area-context";

export default function ExportPasswordsScreen() {
  return (
    <SafeAreaView style={{ flex: 1 }} edges={["left", "right", "bottom"]}>
      <ExportScreenContent />
    </SafeAreaView>
  );
}
