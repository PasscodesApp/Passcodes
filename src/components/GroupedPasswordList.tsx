import CategorySection from "@/components/CategorySection";
import Text from "@/components/Text";
import type { CategoryRef } from "@/libs/category_actions";
import type { CategoryGroup } from "@/libs/category_groups";
import { memo } from "react";
import { RefreshControl, ScrollView } from "react-native";
import { LayoutAnimationConfig } from "react-native-reanimated";

type GroupedPasswordListProps = {
  groups: CategoryGroup[];
  numColumns: number;
  forceExpanded: boolean;
  refreshing: boolean;
  onRefresh: () => void;
  emptyText: string;
  onOpenActions: (category: CategoryRef) => void;
};

/**
 * Grouped layout: collapsible category sections.
 *
 * A plain ScrollView on purpose: accordion animations (height changes of
 * rows) are reliable here, while a recycling list (FlashList) fights with
 * them. Collapsed sections unmount their cards, and each card is memoized,
 * so this stays cheap for a normal password vault.
 */
function GroupedPasswordList({
  groups,
  numColumns,
  forceExpanded,
  refreshing,
  onRefresh,
  emptyText,
  onOpenActions,
}: GroupedPasswordListProps) {
  return (
    <ScrollView
      contentContainerStyle={{
        padding: 20,
        paddingBottom: 110, // room for the FAB / selection bar
      }}
      showsVerticalScrollIndicator={false}
      keyboardShouldPersistTaps="handled"
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      {/* skipEntering: don't animate the very first render of the list. */}
      <LayoutAnimationConfig skipEntering>
        {groups.length === 0 ? (
          <Text
            style={{
              textAlign: "center",
              marginTop: 40,
              fontSize: 12,
              color: "gray",
            }}
          >
            {emptyText}
          </Text>
        ) : (
          groups.map((group) => (
            <CategorySection
              key={group.key}
              group={group}
              numColumns={numColumns}
              forceExpanded={forceExpanded}
              onOpenActions={onOpenActions}
            />
          ))
        )}
      </LayoutAnimationConfig>
    </ScrollView>
  );
}

export default memo(GroupedPasswordList);
