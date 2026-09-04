import Text from "@/components/Text";
import { formatDate } from "@passcodes/passalgo";
import { StyleSheet, View, ViewStyle } from "react-native";
import { useTheme } from "react-native-paper";

type Props = {
  domain: string;
  username: string;
  updatedAt: string | null;
  style?: ViewStyle;
};

export default function PasswordItemCard({
  domain,
  username,
  updatedAt,
  ...props
}: Props) {
  const theme = useTheme();

  return (
    <View
      style={[
        {
          backgroundColor: theme.colors.surface,
          borderRadius: 16,
          padding: 16,
        },
        props.style,
      ]}
    >
      <Text style={styles.label}>
        Domain:{" "}
        <Text style={[styles.value, { color: theme.colors.primary }]}>
          {domain}
        </Text>
      </Text>

      <Text style={styles.label}>
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
          {updatedAt ? formatDate(updatedAt) : "just now"}
        </Text>
      </Text>
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
});
