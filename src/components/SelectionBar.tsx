import CategoryPickerSheet from "@/components/CategoryPickerSheet";
import Text from "@/components/Text";
import { usePasswordRepository } from "@/contexts/RepositoryContext";
import { useToast } from "@/contexts/ToastContext";
import {
  clearSelection,
  getSelectedIds,
  selectMany,
  useSelectionCount,
} from "@/libs/selection_store";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { memo, useState } from "react";
import { Alert } from "react-native";
import { IconButton, Surface, useTheme } from "react-native-paper";
import Animated, { FadeInDown, FadeOutDown } from "react-native-reanimated";

function passwordsLabel(count: number) {
  return `${count} ${count === 1 ? "password" : "passwords"}`;
}

/**
 * Bottom bar shown while multi-selecting: close, count, select all, move,
 * delete. Subscribes to the selection store itself, so the screen does not
 * re-render when the selection changes.
 *
 * `allIds` = the passwords currently visible (what "select all" selects).
 */
function SelectionBar({ allIds }: { allIds: number[] }) {
  const theme = useTheme();
  const passwordRepository = usePasswordRepository();
  const { showToast } = useToast();

  const count = useSelectionCount();
  const [isMoving, setIsMoving] = useState(false);

  async function handleMove(categoryId: number | null, name: string) {
    setIsMoving(false);

    const ids = getSelectedIds();

    try {
      await passwordRepository.moveToCategory(ids, categoryId);
      clearSelection();
      showToast(`Moved ${passwordsLabel(ids.length)} to ${name}`);
    } catch (err) {
      console.error("Failed to move passwords:", err);
      showToast("Failed to move; please try again", "error");
    }
  }

  function handleDelete() {
    const ids = getSelectedIds();

    Alert.alert(
      `Delete ${passwordsLabel(ids.length)}?`,
      "This can't be undone.",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: async () => {
            try {
              await passwordRepository.deleteMany(ids);
              clearSelection();
              showToast(`Deleted ${passwordsLabel(ids.length)}`);
            } catch (err) {
              console.error("Failed to delete passwords:", err);
              showToast("Failed to delete; please try again", "error");
            }
          },
        },
      ],
    );
  }

  if (count === 0) {
    return null;
  }

  return (
    <>
      <Animated.View
        entering={FadeInDown.duration(180)}
        exiting={FadeOutDown.duration(150)}
        style={{ position: "absolute", left: 0, right: 0, bottom: 0 }}
      >
        <Surface
          elevation={4}
          style={{
            flexDirection: "row",
            alignItems: "center",
            paddingHorizontal: 8,
            paddingVertical: 4,
            borderTopLeftRadius: 20,
            borderTopRightRadius: 20,
          }}
        >
          <IconButton
            accessibilityLabel="Cancel selection"
            icon={() => (
              <FontAwesome6
                name="xmark"
                size={18}
                color={theme.colors.onSurface}
                iconStyle="solid"
              />
            )}
            onPress={clearSelection}
          />

          <Text variant="titleMedium" style={{ flex: 1 }}>
            {count} selected
          </Text>

          <IconButton
            accessibilityLabel="Select all"
            icon={() => (
              <FontAwesome6
                name="check-double"
                size={18}
                color={theme.colors.onSurface}
                iconStyle="solid"
              />
            )}
            onPress={() => selectMany(allIds)}
          />

          <IconButton
            accessibilityLabel="Move selected"
            icon={() => (
              <FontAwesome6
                name="folder-open"
                size={18}
                color={theme.colors.onSurface}
                iconStyle="solid"
              />
            )}
            onPress={() => setIsMoving(true)}
          />

          <IconButton
            accessibilityLabel="Delete selected"
            icon={() => (
              <FontAwesome6
                name="trash"
                size={18}
                color={theme.colors.error}
                iconStyle="solid"
              />
            )}
            onPress={handleDelete}
          />
        </Surface>
      </Animated.View>

      {isMoving && (
        <CategoryPickerSheet
          title="Move to"
          onSelect={handleMove}
          onClose={() => setIsMoving(false)}
        />
      )}
    </>
  );
}

export default memo(SelectionBar);
