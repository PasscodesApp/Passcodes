import Text from "@/components/Text";
import { usePasswordRepository } from "@/contexts/RepositoryContext";
import { useToast } from "@/contexts/ToastContext";
import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6/static";
import { FlashList } from "@shopify/flash-list";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { useCallback } from "react";
import { Alert, View } from "react-native";
import { Button, Card, useTheme } from "react-native-paper";

import { describeDeletedAt } from "./describeDeletedAt";

function passwordsLabel(count: number) {
  return `${count} ${count === 1 ? "password" : "passwords"}`;
}

/**
 * "Recently deleted": every soft-deleted password, with Restore and
 * "Delete forever" for one password or for all of them.
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
      <View style={{ flex: 1, justifyContent: "center", padding: 24 }}>
        <Text style={{ textAlign: "center", color: theme.colors.error }}>
          Recently deleted passwords couldn&apos;t be loaded. Please go back and
          try again.
        </Text>
      </View>
    );
  }

  return (
    <View style={{ flex: 1 }}>
      <FlashList
        data={deleted}
        keyExtractor={(item) => String(item.id)}
        contentContainerStyle={{ padding: 12, paddingBottom: 32 }}
        ListHeaderComponent={
          <View style={{ gap: 12, marginBottom: 12 }}>
            <Text style={{ color: theme.colors.onSurfaceVariant }}>
              Deleted passwords stay here until you delete them forever. Restore
              a password to bring it back.
            </Text>

            {deleted.length > 0 ? (
              <View style={{ flexDirection: "row", gap: 12 }}>
                <Button
                  mode="contained-tonal"
                  style={{ flex: 1 }}
                  onPress={() => restore(allIds)}
                >
                  Restore all
                </Button>
                <Button
                  mode="outlined"
                  style={{ flex: 1 }}
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
              </View>
            ) : null}
          </View>
        }
        ListEmptyComponent={
          <View style={{ alignItems: "center", padding: 32, gap: 12 }}>
            <FontAwesome6
              name="trash-can"
              iconStyle="solid"
              size={40}
              color={theme.colors.onSurfaceVariant}
            />
            <Text style={{ fontSize: 18, fontWeight: "600" }}>
              Nothing here
            </Text>
            <Text
              style={{
                textAlign: "center",
                color: theme.colors.onSurfaceVariant,
              }}
            >
              Passwords you delete will show up here.
            </Text>
          </View>
        }
        ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
        renderItem={({ item }) => (
          <Card>
            <Card.Content style={{ gap: 4 }}>
              <Text style={{ fontSize: 17, fontWeight: "600" }} numberOfLines={1}>
                {item.domain || "(no name)"}
              </Text>

              {item.username ? (
                <Text
                  numberOfLines={1}
                  style={{ color: theme.colors.onSurfaceVariant }}
                >
                  {item.username}
                </Text>
              ) : null}

              <Text
                variant="labelMedium"
                style={{ color: theme.colors.onSurfaceVariant }}
              >
                {item.deletedAt ? describeDeletedAt(item.deletedAt) : "Deleted"}
              </Text>
            </Card.Content>

            <Card.Actions>
              <Button
                textColor={theme.colors.error}
                onPress={() =>
                  confirmDeleteForever(
                    [item.id],
                    `Delete "${item.domain || "this password"}" forever?`,
                  )
                }
              >
                Delete forever
              </Button>
              <Button mode="contained-tonal" onPress={() => restore([item.id])}>
                Restore
              </Button>
            </Card.Actions>
          </Card>
        )}
      />
    </View>
  );
}
