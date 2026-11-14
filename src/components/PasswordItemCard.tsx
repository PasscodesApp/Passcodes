import Favicon from "@/components/Favicon";
import Text from "@/components/Text";
import { getFaviconHost } from "@/libs/favicon";
import { useWebsiteIconsEnabled } from "@/libs/favicon_setting";
import { formatTimestamp } from "@passcodes/passalgo";
import { StyleSheet, View, ViewStyle } from "react-native";
import { useTheme } from "react-native-paper";

type Props = {
  domain: string;
  username: string;
  updatedAt: number | null;
  /** Saved web address; the website icon is taken from it. */
  url?: string | null;
  /** Grid cell (smaller icon, labelled lines) instead of a full-width row. */
  compact?: boolean;
  style?: ViewStyle;
};

const LIST_ICON_SIZE = 40;
const GRID_ICON_SIZE = 20;

export default function PasswordItemCard({
  domain,
  username,
  updatedAt,
  url,
  compact = false,
  ...props
}: Props) {
  const theme = useTheme();
  const iconsEnabled = useWebsiteIconsEnabled();

  // `null` = no icon: icons are off, or the entry has no usable web address.
  const host = iconsEnabled ? getFaviconHost(url) : null;

  const containerStyle = [
    {
      backgroundColor: theme.colors.surface,
      borderRadius: 16,
      padding: 16,
    },
    props.style,
  ];

  const updatedText = updatedAt ? formatTimestamp(updatedAt) : "just now";

  if (compact) {
    return (
      <View style={containerStyle}>
        <View style={styles.gridHeader}>
          {host ? (
            <Favicon key={host} host={host} size={GRID_ICON_SIZE} />
          ) : null}

          <Text
            numberOfLines={1}
            style={[styles.gridDomain, { color: theme.colors.primary }]}
          >
            {domain}
          </Text>
        </View>

        <Text style={styles.label} numberOfLines={1}>
          Username:{" "}
          <Text style={[styles.value, { color: theme.colors.primary }]}>
            {username}
          </Text>
        </Text>

        <Text style={styles.label}>
          Password:{" "}
          <Text style={[styles.value, { color: theme.colors.tertiary }]}>
            **********
          </Text>
        </Text>

        <Text style={[styles.label, { color: theme.colors.onSurface }]}>
          Updated At:{" "}
          <Text style={[styles.value, { color: theme.colors.tertiary }]}>
            {updatedText}
          </Text>
        </Text>
      </View>
    );
  }

  return (
    <View style={[containerStyle, styles.row]}>
      {/* Fixed slot: rows without an icon stay aligned with the others. */}
      <View style={styles.iconSlot}>
        {host ? (
          <Favicon key={host} host={host} size={LIST_ICON_SIZE} />
        ) : null}
      </View>

      <View style={styles.rowText}>
        <Text variant="titleMedium" numberOfLines={1}>
          {domain}
        </Text>

        <Text
          numberOfLines={1}
          style={{ color: theme.colors.onSurfaceVariant }}
        >
          {username}
        </Text>

        <Text
          variant="labelSmall"
          style={{ color: theme.colors.onSurfaceVariant }}
        >
          Updated {updatedText}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  label: {
    fontWeight: "600",
    marginBottom: 6,
  },

  value: {
    fontWeight: "400",
  },

  gridHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    marginBottom: 8,
  },

  gridDomain: {
    flex: 1,
    fontWeight: "700",
  },

  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
  },

  iconSlot: {
    width: LIST_ICON_SIZE,
    height: LIST_ICON_SIZE,
  },

  rowText: {
    flex: 1,
    gap: 2,
  },
});
