import { RecentlyDeletedScreenContent } from "@/features/recently-deleted";
import { SafeAreaView } from "react-native-safe-area-context";

export default function RecentlyDeletedScreen() {
  return (
    <SafeAreaView style={{ flex: 1 }} edges={["left", "right", "bottom"]}>
      <RecentlyDeletedScreenContent />
    </SafeAreaView>
  );
}
