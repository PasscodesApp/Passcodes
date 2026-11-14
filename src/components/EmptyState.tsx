import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6/static";
import type { ComponentProps } from "react";
import { View } from "react-native";
import { Button, Text, useTheme } from "react-native-paper";

type IconName = ComponentProps<typeof FontAwesome6>["name"];

type EmptyStateProps = {
  icon: IconName;
  title: string;
  message: string;
  /** Optional single action, shown as a text button. */
  actionLabel?: string;
  onAction?: () => void;
};

/**
 * Friendly "nothing here yet" placeholder shared by every list screen:
 * a soft icon badge, a short title, one helpful sentence and, optionally,
 * the one action that fixes the emptiness.
 */
export default function EmptyState({
  icon,
  title,
  message,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  const theme = useTheme();

  return (
    <View
      accessibilityRole="summary"
      style={{
        alignItems: "center",
        paddingHorizontal: 32,
        paddingTop: 72,
        gap: 12,
      }}
    >
      <View
        style={{
          width: 104,
          height: 104,
          borderRadius: 52,
          alignItems: "center",
          justifyContent: "center",
          backgroundColor: theme.colors.secondaryContainer,
          marginBottom: 8,
        }}
      >
        <FontAwesome6
          name={icon}
          iconStyle="solid"
          size={40}
          color={theme.colors.onSecondaryContainer}
        />
      </View>

      <Text variant="titleLarge" style={{ textAlign: "center" }}>
        {title}
      </Text>

      <Text
        variant="bodyMedium"
        style={{
          textAlign: "center",
          maxWidth: 300,
          color: theme.colors.onSurfaceVariant,
        }}
      >
        {message}
      </Text>

      {actionLabel && onAction ? (
        <Button mode="text" onPress={onAction} style={{ marginTop: 4 }}>
          {actionLabel}
        </Button>
      ) : null}
    </View>
  );
}
