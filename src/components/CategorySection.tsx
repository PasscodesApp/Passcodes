import Collapsible from "@/components/Collapsible";
import PasswordCardGrid from "@/components/PasswordCardGrid";
import Text from "@/components/Text";
import { toggleCollapsed, useIsCollapsed } from "@/libs/collapse_store";
import type { CategoryRef } from "@/libs/category_actions";
import type { CategoryGroup } from "@/libs/category_groups";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { memo, useEffect } from "react";
import { Pressable, StyleSheet, View } from "react-native";
import { IconButton, useTheme } from "react-native-paper";
import Animated, {
  LinearTransition,
  useAnimatedStyle,
  useSharedValue,
  withTiming,
} from "react-native-reanimated";

type CategorySectionProps = {
  group: CategoryGroup;
  numColumns: number;
  /** While searching, matches are always shown. */
  forceExpanded: boolean;
  onOpenActions: (category: CategoryRef) => void;
};

/**
 * One collapsible category in the grouped layout: header (chevron, name,
 * count, actions) + animated body.
 *
 * Collapsed state comes from the collapse store, so toggling re-renders only
 * this section.
 */
function CategorySection({
  group,
  numColumns,
  forceExpanded,
  onOpenActions,
}: CategorySectionProps) {
  const theme = useTheme();
  const collapsed = useIsCollapsed(group.key);
  const expanded = forceExpanded || !collapsed;

  const { categoryId, name, count } = group;

  return (
    <Animated.View
      layout={LinearTransition.duration(220)}
      style={{ marginBottom: 16 }}
    >
      <View
        style={{
          flexDirection: "row",
          alignItems: "center",
          marginBottom: 8,
          borderBottomWidth: StyleSheet.hairlineWidth,
          borderBottomColor: theme.colors.outlineVariant,
        }}
      >
        <Pressable
          disabled={forceExpanded}
          onPress={() => toggleCollapsed(group.key)}
          accessibilityRole="button"
          accessibilityState={{ expanded }}
          accessibilityLabel={`${name}, ${count} ${count === 1 ? "password" : "passwords"}`}
          style={{
            flex: 1,
            flexDirection: "row",
            alignItems: "center",
            gap: 12,
            paddingVertical: 12,
            paddingHorizontal: 4,
          }}
        >
          <AnimatedChevron expanded={expanded} color={theme.colors.onSurface} />

          <Text variant="titleMedium" style={{ flex: 1 }} numberOfLines={1}>
            {name}
          </Text>

          <Text style={{ color: theme.colors.onSurfaceVariant }}>{count}</Text>
        </Pressable>

        {categoryId !== null && (
          <IconButton
            size={18}
            accessibilityLabel={`Actions for ${name}`}
            icon={() => (
              <FontAwesome6
                name="ellipsis-vertical"
                size={16}
                color={theme.colors.onSurfaceVariant}
                iconStyle="solid"
              />
            )}
            onPress={() => onOpenActions({ id: categoryId, name })}
          />
        )}
      </View>

      <Collapsible expanded={expanded}>
        {group.passwords.length > 0 ? (
          <PasswordCardGrid
            passwords={group.passwords}
            numColumns={numColumns}
          />
        ) : (
          <Text
            style={{
              textAlign: "center",
              fontSize: 12,
              color: "gray",
              marginVertical: 12,
            }}
          >
            No passwords in this category.
          </Text>
        )}
      </Collapsible>
    </Animated.View>
  );
}

function AnimatedChevron({
  expanded,
  color,
}: {
  expanded: boolean;
  color: string;
}) {
  const rotation = useSharedValue(expanded ? 1 : 0);

  useEffect(() => {
    rotation.value = withTiming(expanded ? 1 : 0, { duration: 220 });
  }, [expanded, rotation]);

  const style = useAnimatedStyle(() => ({
    transform: [{ rotate: `${rotation.value * 90}deg` }],
  }));

  return (
    <Animated.View style={[{ width: 16, alignItems: "center" }, style]}>
      <FontAwesome6
        name="chevron-right"
        size={14}
        color={color}
        iconStyle="solid"
      />
    </Animated.View>
  );
}

export default memo(CategorySection);
