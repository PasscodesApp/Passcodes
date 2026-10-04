import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { router } from "expo-router";
import { memo, useCallback } from "react";
import { FAB } from "react-native-paper";
import { useIsSelecting } from "@/libs/selection_store";

// Module level => stable identity, the FAB never re-renders because of it.
const PlusIcon = ({ size, color }: { size: number; color: string }) => (
  <FontAwesome6 name="plus" size={size} color={color} iconStyle="solid" />
);

/**
 * "Add password" FAB. `categoryId` (a number, or null for Uncategorized)
 * pre-selects that category and hides the Category field on the Add screen.
 * Hidden while multi-selecting.
 */
function AddPasswordFab({ categoryId }: { categoryId: number | null }) {
  const isSelectingNow = useIsSelecting();

  const handlePress = useCallback(() => {
    router.push({
      pathname: "/save-password",
      params:
        categoryId === null
          ? { lockCategory: "1" }
          : { categoryId, lockCategory: "1" },
    });
  }, [categoryId]);

  if (isSelectingNow) {
    return null;
  }

  return (
    <FAB
      style={{
        position: "absolute",
        bottom: 35,
        right: 35,
      }}
      accessibilityLabel="Add password"
      icon={PlusIcon}
      onPress={handlePress}
    />
  );
}

export default memo(AddPasswordFab);
