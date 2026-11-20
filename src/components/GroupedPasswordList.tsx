import CategorySection from "@/components/CategorySection";
import type { CategoryRef } from "@/libs/category_actions";
import type { CategoryGroup } from "@/libs/category_groups";
import type { ReactNode } from "react";
import { memo } from "react";
import { RefreshControl, ScrollView } from "react-native";

type GroupedPasswordListProps = {
  groups: CategoryGroup[];
  numColumns: number;
  forceExpanded: boolean;
  refreshing: boolean;
  onRefresh: () => void;
  /** Shown instead of the list when there is nothing to show. */
  empty: ReactNode;
  onOpenActions: (category: CategoryRef) => void;
};

/**
 * Grouped layout: collapsible category sections.
 *
 * A plain ScrollView on purpose: sections change height when they expand /
 * collapse, which a recycling list (FlashList) handles badly. Collapsed
 * sections unmount their cards, and each card is memoized, so this stays
 * cheap for a normal password vault.
 */
function GroupedPasswordList({
  groups,
  numColumns,
  forceExpanded,
  refreshing,
  onRefresh,
  empty,
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
      {groups.length === 0
        ? empty
        : groups.map((group) => (
            <CategorySection
              key={group.key}
              group={group}
              numColumns={numColumns}
              forceExpanded={forceExpanded}
              onOpenActions={onOpenActions}
            />
          ))}
    </ScrollView>
  );
}

export default memo(GroupedPasswordList);
