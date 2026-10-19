import { ImportReviewScreenContent } from "@/features/import-export";
import { SafeAreaView } from "react-native-safe-area-context";

export default function ImportReviewScreen() {
  return (
    <SafeAreaView style={{ flex: 1 }} edges={["left", "right", "bottom"]}>
      <ImportReviewScreenContent />
    </SafeAreaView>
  );
}
