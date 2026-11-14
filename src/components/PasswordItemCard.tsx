import Favicon from "@/components/Favicon";
import Text from "@/components/Text";
import { getFaviconHost } from "@/libs/favicon";
import { StyleSheet, View, ViewStyle } from "react-native";
import { useTheme } from "react-native-paper";

type Props = {
  domain: string;
  username: string;
  /** Saved web address; the website icon is taken from it. */
  url?: string | null;
  /** Grid cell: just icon + site name, to save space and attention. */
  compact?: boolean;
  style?: ViewStyle;
};

const LIST_ICON_SIZE = 44;
const GRID_ICON_SIZE = 28;

export default function PasswordItemCard({
  domain,
  username,
  url,
  compact = false,
  ...props
}: Props) {
  const theme = useTheme();

  // `null` = nothing to look an icon up for (no usable web address).
  const host = getFaviconHost(url);

  if (compact) {
    return (
      <View
        style={[
          styles.card,
          { backgroundColor: theme.colors.surface, padding: 12 },
          styles.gridRow,
          props.style,
        ]}
      >
        {host ? (
          <Favicon key={host} host={host} size={GRID_ICON_SIZE} />
        ) : null}

        <Text variant="titleSmall" numberOfLines={1} style={styles.flex}>
          {domain}
        </Text>
      </View>
    );
  }

  return (
    <View
      style={[
        styles.card,
        { backgroundColor: theme.colors.surface, padding: 12 },
        styles.listRow,
        props.style,
      ]}
    >
      {/* Fixed slot: rows without an icon stay aligned with the others. */}
      <View style={styles.iconSlot}>
        {host ? (
          <Favicon key={host} host={host} size={LIST_ICON_SIZE} />
        ) : null}
      </View>

      <View style={styles.listText}>
        <Text variant="titleMedium" numberOfLines={1}>
          {domain}
        </Text>

        <Text
          variant="bodySmall"
          numberOfLines={1}
          style={{ color: theme.colors.onSurfaceVariant }}
        >
          {username}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
  },
  flex: {
    flex: 1,
  },
  gridRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    minHeight: 56,
  },
  listRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
  },
  iconSlot: {
    width: LIST_ICON_SIZE,
    height: LIST_ICON_SIZE,
  },
  listText: {
    flex: 1,
    gap: 2,
  },
});
