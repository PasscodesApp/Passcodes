import FolderCard from "@/components/FolderCard";
import Text from "@/components/Text";
import type { CategoryRef } from "@/libs/category_actions";
import type { FolderData } from "@/libs/category_groups";
import { FlashList } from "@shopify/flash-list";
import { memo, useCallback } from "react";

type FolderListProps = {
  folders: FolderData[];
  hasCategories: boolean;
  refreshing: boolean;
  onRefresh: () => void;
  onOpenFolder: (categoryId: number | null) => void;
  onOpenActions: (category: CategoryRef) => void;
};

/** Folders layout: a 2-column grid of folders. */
function FolderList({
  folders,
  hasCategories,
  refreshing,
  onRefresh,
  onOpenFolder,
  onOpenActions,
}: FolderListProps) {
  const renderItem = useCallback(
    ({ item }: { item: FolderData }) => (
      <FolderCard
        folder={item}
        onOpen={onOpenFolder}
        onOpenActions={onOpenActions}
      />
    ),
    [onOpenFolder, onOpenActions],
  );

  return (
    <FlashList
      data={folders}
      numColumns={2}
      keyExtractor={(item) => item.key}
      renderItem={renderItem}
      refreshing={refreshing}
      onRefresh={onRefresh}
      contentContainerStyle={{
        padding: 20,
        paddingBottom: 110, // room for the FAB
      }}
      showsVerticalScrollIndicator={false}
      ListHeaderComponent={
        hasCategories ? null : (
          <Text
            style={{
              textAlign: "center",
              marginBottom: 16,
              fontSize: 12,
              color: "gray",
            }}
          >
            No folders yet. Tap + to create your first folder.
          </Text>
        )
      }
    />
  );
}

export default memo(FolderList);
