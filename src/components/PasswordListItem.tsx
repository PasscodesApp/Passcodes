import PasswordItemCard from "@/components/PasswordItemCard";
import { usePasswordRepository } from "@/contexts/RepositoryContext";
import { useToast } from "@/contexts/ToastContext";
import type { passwords } from "@/db/schema";
import { router } from "expo-router";
import { Alert, Pressable, type StyleProp, type ViewStyle } from "react-native";

type PasswordRow = typeof passwords.$inferSelect;

/**
 * One tappable password card: tap = open details, long press = delete.
 * Shared by the grouped home list and the single-category screen.
 */
export default function PasswordListItem({
  item,
  style,
}: {
  item: PasswordRow;
  style?: StyleProp<ViewStyle>;
}) {
  const passwordRepository = usePasswordRepository();
  const { showToast } = useToast();

  function confirmDelete() {
    Alert.alert("Delete?", `${item.domain} : ${item.username}`, [
      {
        text: "Cancel",
        style: "cancel",
      },
      {
        text: "Delete",
        onPress: async () => {
          try {
            await passwordRepository.delete(item.id);
          } catch (error) {
            console.error("Failed to delete password:", error);
            showToast("Failed to delete; please try again", "error");
          }
        },
        style: "destructive",
      },
    ]);
  }

  return (
    <Pressable
      style={[{ margin: 2 }, style]}
      onPress={() =>
        router.push({
          pathname: "/password-details",
          params: { id: item.id },
        })
      }
      onLongPress={confirmDelete}
    >
      <PasswordItemCard
        {...item}
        style={{
          height: "100%",
        }}
      />
    </Pressable>
  );
}
