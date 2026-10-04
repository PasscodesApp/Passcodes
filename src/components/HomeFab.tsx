import SpeedDialFab, { type SpeedDialAction } from "@/components/SpeedDialFab";
import type { CategoryNoun } from "@/libs/category_actions";
import type { HomeLayout } from "@/libs/home_layout";
import { useIsSelecting } from "@/libs/selection_store";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { router } from "expo-router";
import { memo, useMemo } from "react";
import { FAB } from "react-native-paper";

// Module level => stable identities (no FAB re-render / animation replay).
const renderPlus = (size: number, color: string) => (
  <FontAwesome6 name="plus" size={size} color={color} iconStyle="solid" />
);

const renderFolderPlus = (size: number, color: string) => (
  <FontAwesome6 name="folder-plus" size={size} color={color} iconStyle="solid" />
);

const FolderPlusIcon = ({ size, color }: { size: number; color: string }) =>
  renderFolderPlus(size, color);

function goToAddPassword() {
  router.push("/save-password");
}

type HomeFabProps = {
  layout: HomeLayout;
  noun: CategoryNoun;
  onCreateCategory: () => void;
};

/**
 * Home screen FAB.
 *
 * - grouped layout: speed dial (New category / Add password)
 * - folders layout: single "new folder" button (passwords are added from
 *   inside a folder)
 *
 * Memoized with stable props, so collapsing sections, searching or
 * selecting never re-renders it. Hidden while multi-selecting.
 */
function HomeFab({ layout, noun, onCreateCategory }: HomeFabProps) {
  const isSelectingNow = useIsSelecting();

  const actions = useMemo<SpeedDialAction[]>(
    () => [
      {
        key: "category",
        label: `New ${noun}`,
        renderIcon: renderFolderPlus,
        onPress: onCreateCategory,
      },
      {
        key: "password",
        label: "Add password",
        renderIcon: renderPlus,
        onPress: goToAddPassword,
      },
    ],
    [noun, onCreateCategory],
  );

  if (isSelectingNow) {
    return null;
  }

  if (layout === "folders") {
    return (
      <FAB
        style={{
          position: "absolute",
          bottom: 35,
          right: 35,
        }}
        accessibilityLabel={`New ${noun}`}
        icon={FolderPlusIcon}
        onPress={onCreateCategory}
      />
    );
  }

  return <SpeedDialFab actions={actions} renderIcon={renderPlus} />;
}

export default memo(HomeFab);
