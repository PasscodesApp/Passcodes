import Text from "@/components/Text";
import type { FolderData } from "@/libs/category_groups";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { memo } from "react";
import { Pressable } from "react-native";
import { useTheme } from "react-native-paper";

type FolderCardProps = {
  folder: FolderData;
  onOpen: (categoryId: number | null) => void;
  onOpenActions: (category: { id: number; name: string }) => void;
};

/** One folder tile: tap = open, long press = rename / delete. */
function FolderCard({ folder, onOpen, onOpenActions }: FolderCardProps) {
  const theme = useTheme();
  const { categoryId, name, count } = folder;

  return (
    <Pressable
      onPress={() => onOpen(categoryId)}
      onLongPress={
        categoryId === null
          ? undefined // Uncategorized can't be renamed / deleted
          : () => onOpenActions({ id: categoryId, name })
      }
      accessibilityRole="button"
      accessibilityLabel={`${name}, ${count} ${count === 1 ? "password" : "passwords"}`}
      accessibilityHint={
        categoryId === null ? undefined : "Long press for rename and delete"
      }
      style={{
        flex: 1,
        margin: 2,
        gap: 8,
        backgroundColor: theme.colors.surface,
        borderRadius: 16,
        padding: 16,
      }}
    >
      <FontAwesome6
        name="folder"
        size={28}
        color={theme.colors.primary}
        iconStyle="solid"
      />

      <Text variant="titleMedium" numberOfLines={1}>
        {name}
      </Text>

      <Text style={{ fontSize: 12, color: theme.colors.onSurfaceVariant }}>
        {count} {count === 1 ? "password" : "passwords"}
      </Text>
    </Pressable>
  );
}

export default memo(FolderCard);
