import Text from "@/components/Text";
import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6";
import { useState } from "react";
import { View } from "react-native";
import {
  Card,
  Divider,
  IconButton,
  Switch,
  useTheme,
} from "react-native-paper";

import type {
  CandidatePassword,
  ConflictGroup,
  ExistingPassword,
} from "../types";

const MASK = "••••••••";

interface VersionRowProps {
  title: string;
  password: string;
  revealed: boolean;
  detail: string;
  switchLabel: string;
  value: boolean;
  onValueChange: () => void;
  disabled: boolean;
  destructive?: boolean;
}

function VersionRow({
  title,
  password,
  revealed,
  detail,
  switchLabel,
  value,
  onValueChange,
  disabled,
  destructive = false,
}: VersionRowProps) {
  const theme = useTheme();

  return (
    <View style={{ flexDirection: "row", alignItems: "center", gap: 12 }}>
      <View style={{ flex: 1, gap: 2 }}>
        <Text variant="labelMedium" style={{ color: theme.colors.primary }}>
          {title}
        </Text>
        <Text numberOfLines={revealed ? 3 : 1}>
          {revealed ? password : MASK}
        </Text>
        <Text
          variant="bodySmall"
          style={{ color: theme.colors.onSurfaceVariant }}
        >
          {detail}
        </Text>
      </View>

      <View style={{ alignItems: "center", minWidth: 64 }}>
        <Switch
          value={value}
          onValueChange={onValueChange}
          disabled={disabled}
          color={destructive ? theme.colors.error : theme.colors.primary}
          accessibilityLabel={`${switchLabel}: ${title}`}
        />
        <Text
          variant="labelSmall"
          style={{ color: theme.colors.onSurfaceVariant }}
        >
          {switchLabel}
        </Text>
      </View>
    </View>
  );
}

interface ConflictGroupCardProps {
  group: ConflictGroup;
  /** All incoming passwords; `group.incomingIndexes` point into it. */
  passwords: readonly CandidatePassword[];
  existingById: ReadonlyMap<number, ExistingPassword>;
  importIndexes: ReadonlySet<number>;
  removeIds: ReadonlySet<number>;
  onToggleImport: (index: number) => void;
  onToggleRemove: (id: number) => void;
  disabled: boolean;
}

/**
 * One account (same domain and username) that exists in more than one
 * version. Passwords stay masked until the user reveals them.
 */
export default function ConflictGroupCard({
  group,
  passwords,
  existingById,
  importIndexes,
  removeIds,
  onToggleImport,
  onToggleRemove,
  disabled,
}: ConflictGroupCardProps) {
  const theme = useTheme();
  const [revealed, setRevealed] = useState(false);

  const existingRows = group.existingIds.flatMap((id) => {
    const row = existingById.get(id);
    return row ? [row] : [];
  });

  return (
    <Card>
      <Card.Content style={{ gap: 12 }}>
        <View style={{ flexDirection: "row", alignItems: "center" }}>
          <View style={{ flex: 1 }}>
            <Text variant="titleMedium" numberOfLines={1}>
              {group.domain}
            </Text>
            <Text
              variant="bodySmall"
              numberOfLines={1}
              style={{ color: theme.colors.onSurfaceVariant }}
            >
              {group.username}
            </Text>
          </View>

          <IconButton
            icon={({ size, color }) => (
              <FontAwesome6
                name={revealed ? "eye-slash" : "eye"}
                iconStyle="solid"
                size={size}
                color={color}
              />
            )}
            onPress={() => setRevealed((value) => !value)}
            accessibilityLabel={revealed ? "Hide passwords" : "Show passwords"}
          />
        </View>

        {existingRows.map((row) => (
          <View key={`existing-${row.id}`} style={{ gap: 12 }}>
            <Divider />
            <VersionRow
              title="On this device"
              password={row.password}
              revealed={revealed}
              detail={`Updated ${row.updatedAt ?? "unknown"}`}
              switchLabel="Remove"
              value={removeIds.has(row.id)}
              onValueChange={() => onToggleRemove(row.id)}
              disabled={disabled}
              destructive
            />
          </View>
        ))}

        {group.incomingIndexes.map((index) => {
          const candidate = passwords[index];

          if (!candidate) {
            return null;
          }

          return (
            <View key={`incoming-${index}`} style={{ gap: 12 }}>
              <Divider />
              <VersionRow
                title="In the file"
                password={candidate.password}
                revealed={revealed}
                detail={
                  candidate.updatedAt
                    ? `Updated ${candidate.updatedAt}`
                    : "No date in the file"
                }
                switchLabel="Import"
                value={importIndexes.has(index)}
                onValueChange={() => onToggleImport(index)}
                disabled={disabled}
              />
            </View>
          );
        })}
      </Card.Content>
    </Card>
  );
}
