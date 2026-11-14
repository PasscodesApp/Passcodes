import EmptyState from "@/components/EmptyState";
import Text from "@/components/Text";
import { usePasswordRepository } from "@/contexts/RepositoryContext";
import { useToast } from "@/contexts/ToastContext";
import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6/static";
import { FlashList } from "@shopify/flash-list";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { useCallback } from "react";
import { Alert, View } from "react-native";
import { Button, Card, Divider, useTheme } from "react-native-paper";

import { describeDeletedAt } from "./describeDeletedAt";

function passwordsLabel(count: number) {
  return `${count} ${count === 1 ? "password" : "passwords"}`;
}

/**
 * "Recently deleted": every soft-deleted password, with Restore and
 * "Delete forever" for one password or for all of them. All buttons are
 * plain text buttons.
 *
 * Nothing here is permanent unless the user confirms "Delete forever".
 */
export default function RecentlyDeletedScreenContent() {
  const theme = useTheme();
  const passwordRepository = usePasswordRepository();
  const { showToast } = useToast();

  const { data: deleted = [], error } = useLiveQuery(
    passwordRepository.observeDeleted(),
    [],
  );

  const restore = useCallback(
    async (ids: number[]) => {
      try {
        await passwordRepository.restoreMany(ids);
        showToast(`Restored ${passwordsLabel(ids.length)}`);
      } catch (err) {
        console.error("Failed to restore passwords:", err);
        showToast("Failed to restore; please try again", "error");
      }
    },
    [passwordRepository, showToast],
  );

  const confirmDeleteForever = useCallback(
    (ids: number[], title: string) => {
      Alert.alert(
        title,
        "This permanently removes it from Passcodes. This can't be undone.",
        [
          { text: "Cancel", style: "cancel" },
          {
            text: "Delete forever",
            style: "destructive",
            onPress: async () => {
              try {
                await passwordRepository.permanentlyDeleteMany(ids);
                showToast(`Permanently deleted ${passwordsLabel(ids.length)}`);
              } catch (err) {
                console.error("Failed to delete passwords:", err);
                showToast("Failed to delete; please try again", "error");
              }
            },
          },
        ],
      );
    },
    [passwordRepository, showToast],
  );

  const allIds = deleted.map((password) => password.id);

  if (error) {
    return (
      <EmptyState
        icon="triangle-exclamation"
        title="Couldn't load this list"
        message="Please go back and open Recently deleted again."
      />
    );
  }

  return (
    <FlashList
      data={deleted}
      keyExtractor={(item) => String(item.id)}
      contentContainerStyle={{ padding: 16, paddingBottom: 40 }}
      ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
      ListEmptyComponent={
        <EmptyState
          icon="trash-can"
          title="Nothing deleted"
          message="Passwords you delete stay here so you can bring them back. You can remove them for good at any time."
        />
      }
      ListHeaderComponent={
        deleted.length === 0 ? null : (
          <Card style={{ marginBottom: 16 }}>
            <Card.Content style={{ gap: 4 }}>
              <View
                style={{ flexDirection: "row", alignItems: "center", gap: 10 }}
              >
                <FontAwesome6
                  name="circle-info"
                  iconStyle="solid"
                  size={16}
                  color={theme.colors.primary}
                />
                <Text variant="titleSmall">
                  {passwordsLabel(deleted.length)} in Recently deleted
                </Text>
              </View>

              <Text
                variant="bodySmall"
                style={{ color: theme.colors.onSurfaceVariant }}
              >
                They stay here until you delete them forever. Restore one to
                bring it back.
              </Text>

              <View
                style={{
                  flexDirection: "row",
                  justifyContent: "flex-end",
                  marginTop: 4,
                }}
              >
                <Button
                  mode="text"
                  textColor={theme.colors.error}
                  onPress={() =>
                    confirmDeleteForever(
                      allIds,
                      `Delete ${passwordsLabel(allIds.length)} forever?`,
                    )
                  }
                >
                  Delete all
                </Button>
                <Button mode="text" onPress={() => restore(allIds)}>
                  Restore all
                </Button>
              </View>
            </Card.Content>
          </Card>
        )
      }
      renderItem={({ item }) => {
        const title = item.domain || "Untitled";

        return (
          <Card>
            <Card.Content style={{ gap: 12 }}>
              <View
                style={{ flexDirection: "row", alignItems: "center", gap: 14 }}
              >
                <View
                  style={{
                    width: 44,
                    height: 44,
                    borderRadius: 22,
                    alignItems: "center",
                    justifyContent: "center",
                    backgroundColor: theme.colors.secondaryContainer,
                  }}
                >
                  <Text
                    variant="titleMedium"
                    style={{ color: theme.colors.onSecondaryContainer }}
                  >
                    {title.charAt(0).toUpperCase()}
                  </Text>
                </View>

                <View style={{ flex: 1 }}>
                  <Text variant="titleMedium" numberOfLines={1}>
                    {title}
                  </Text>

                  {item.username ? (
                    <Text
                      variant="bodyMedium"
                      numberOfLines={1}
                      style={{ color: theme.colors.onSurfaceVariant }}
                    >
                      {item.username}
                    </Text>
                  ) : null}

                  <Text
                    variant="labelSmall"
                    style={{ color: theme.colors.onSurfaceVariant }}
                  >
                    {describeDeletedAt(item.deletedAt)}
                  </Text>
                </View>
              </View>

              <Divider />

              <View style={{ flexDirection: "row", justifyContent: "flex-end" }}>
                <Button
                  mode="text"
                  textColor={theme.colors.error}
                  onPress={() =>
                    confirmDeleteForever([item.id], `Delete "${title}" forever?`)
                  }
                >
                  Delete forever
                </Button>
                <Button mode="text" onPress={() => restore([item.id])}>
                  Restore
                </Button>
              </View>
            </Card.Content>
          </Card>
        );
      }}
    />
  );
}
