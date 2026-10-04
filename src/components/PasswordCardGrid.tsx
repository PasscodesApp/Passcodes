import PasswordListItem from "@/components/PasswordListItem";
import type { Password } from "@/libs/category_groups";
import { memo } from "react";
import { View } from "react-native";

// Module level, so the (memoized) cards always get the same style object.
const FILL = { flex: 1 } as const;

/**
 * Passwords laid out as a list (1 column) or grid (2 columns) inside a
 * section. A wrapping row (not a nested list), so it can live inside the
 * collapsible area of a section.
 */
function PasswordCardGrid({
  passwords,
  numColumns,
}: {
  passwords: Password[];
  numColumns: number;
}) {
  const width = numColumns === 2 ? "50%" : "100%";

  return (
    <View style={{ flexDirection: "row", flexWrap: "wrap" }}>
      {passwords.map((password) => (
        <View key={password.id} style={{ width }}>
          <PasswordListItem item={password} style={FILL} />
        </View>
      ))}
    </View>
  );
}

export default memo(PasswordCardGrid);
