import PasswordItemCard from "@/components/PasswordItemCard";
import type { Password } from "@/libs/category_groups";
import {
  isSelecting,
  toggleSelected,
  useSelectionState,
} from "@/libs/selection_store";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { router } from "expo-router";
import { memo, useCallback } from "react";
import { Pressable, StyleSheet, View, type StyleProp, type ViewStyle } from "react-native";
import { useTheme } from "react-native-paper";

/**
 * One password card.
 *
 * - tap: open details (or toggle selection while selecting)
 * - long press: start selecting (gallery style), delete / move are in the
 *   selection bar
 *
 * Memoized and subscribed to the selection store per id, so selecting one
 * card re-renders only that card.
 */
function PasswordListItem({
  item,
  style,
}: {
  item: Password;
  style?: StyleProp<ViewStyle>;
}) {
  const theme = useTheme();
  const selection = useSelectionState(item.id);
  const isSelected = selection === "selected";

  const handlePress = useCallback(() => {
    if (isSelecting()) {
      toggleSelected(item.id);
      return;
    }

    router.push({
      pathname: "/password-details",
      params: { id: item.id },
    });
  }, [item.id]);

  const handleLongPress = useCallback(() => {
    toggleSelected(item.id);
  }, [item.id]);

  return (
    <Pressable
      style={[{ margin: 2 }, style]}
      onPress={handlePress}
      onLongPress={handleLongPress}
      accessibilityRole="button"
      accessibilityHint="Long press to select"
      accessibilityState={{ selected: isSelected }}
    >
      <PasswordItemCard
        domain={item.domain}
        username={item.username}
        updatedAt={item.updatedAt}
        style={{
          height: "100%",
        }}
      />

      {selection !== "off" && (
        <>
          <View
            pointerEvents="none"
            style={[
              StyleSheet.absoluteFill,
              {
                borderRadius: 16,
                borderWidth: isSelected ? 2 : 0,
                borderColor: theme.colors.primary,
              },
            ]}
          />

          <View
            pointerEvents="none"
            style={{
              position: "absolute",
              top: 10,
              right: 10,
              width: 22,
              height: 22,
              borderRadius: 11,
              borderWidth: 2,
              borderColor: isSelected
                ? theme.colors.primary
                : theme.colors.outline,
              backgroundColor: isSelected ? theme.colors.primary : "transparent",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            {isSelected && (
              <FontAwesome6
                name="check"
                size={11}
                color={theme.colors.onPrimary}
                iconStyle="solid"
              />
            )}
          </View>
        </>
      )}
    </Pressable>
  );
}

export default memo(PasswordListItem);
