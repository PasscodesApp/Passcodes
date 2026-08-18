import Text from "@/components/Text";
import { passwords } from "@/db/schema";
import formatDate from "@/utils/formating";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { FlashList } from "@shopify/flash-list";
import { eq } from "drizzle-orm";
import { drizzle, useLiveQuery } from "drizzle-orm/expo-sqlite";
import { router, Stack } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useMemo, useState } from "react";
import { Alert, Pressable, StyleSheet, View } from "react-native";
import { FAB, IconButton, useTheme } from "react-native-paper";

export default function LoadPasswordScreen() {
  const [forceGrid, setForceGrid] = useState(false);
  const numColumns = forceGrid ? 2 : 1;

  const [searchQuery, setSearchQuery] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [refreshing, setRefreshing] = useState(false);

  const theme = useTheme();

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const { data: passwordList = [] } = useLiveQuery(
    drizzleDb.select().from(passwords),
    [refreshKey],
  );

  /**
   * Filter passwords whenever searchQuery or passwordList changes.
   *
   * useMemo isn't required, but it avoids doing the filtering
   * on every render when neither value has changed.
   */
  const filteredPasswords = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();

    // No search text -> show everything
    if (!query) {
      return passwordList;
    }

    return passwordList.filter((password) => {
      const domain = password.domain?.toLowerCase() ?? "";
      const username = password.username?.toLowerCase() ?? "";
      const url = password.url?.toLowerCase() ?? "";
      const notes = password.notes?.toLowerCase() ?? "";

      return (
        domain.includes(query) ||
        username.includes(query) ||
        url.includes(query) ||
        notes.includes(query)
      );
    });
  }, [passwordList, searchQuery]);

  async function onRefresh() {
    setRefreshing(true);
    setRefreshKey((k) => k + 1);
    setRefreshing(false);
  }

  function deletePassword(password: any) {
    Alert.alert("Delete?", `${password.domain} : ${password.username}`, [
      {
        text: "Cancel",
        style: "cancel",
      },
      {
        text: "Delete",
        onPress: async () => {
          await drizzleDb
            .delete(passwords)
            .where(eq(passwords.id, password.id));
        },
        style: "destructive",
      },
    ]);
  }

  return (
    <>
      <Stack.Screen>
        <Stack.Screen.Title>Password Manager</Stack.Screen.Title>

        <Stack.SearchBar
          placeholder="Search passwords..."
          onChangeText={(event) => {
            setSearchQuery(event.nativeEvent.text);
          }}
          onSearchButtonPress={(event) => {
            setSearchQuery(event.nativeEvent.text);
          }}
        />
      </Stack.Screen>

      <View
        style={{
          flexDirection: "row",
          justifyContent: "space-between",
          alignItems: "center",
          paddingHorizontal: 16,
          paddingVertical: 8,
        }}
      >
        <Text variant="headlineSmall">
          {searchQuery.trim()
            ? `Passwords (${filteredPasswords.length})`
            : "Passwords"}
        </Text>

        <IconButton
          style={{ backgroundColor: "#ccc" }}
          icon={() => {
            return numColumns === 1 ? (
              <FontAwesome6 name="grip" iconStyle="solid" size={20} />
            ) : (
              <FontAwesome6 name="list" iconStyle="solid" size={20} />
            );
          }}
          onPress={() => {
            setForceGrid((g) => !g);
          }}
        />
      </View>

      <FlashList
        data={filteredPasswords}
        key={numColumns}
        numColumns={numColumns}
        keyExtractor={(item) => item.id.toString()}
        refreshing={refreshing}
        onRefresh={onRefresh}
        contentContainerStyle={{
          paddingInline: 20,
        }}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <Text style={styles.emptyText}>
            {searchQuery.trim() ? "No passwords found!!" : "No data!!"}
          </Text>
        }
        renderItem={({ item }) => (
          <Pressable
            style={{ margin: 2 }}
            onPress={() =>
              router.push({
                pathname: "/password-details",
                params: { id: item.id },
              })
            }
            onLongPress={() => deletePassword(item)}
          >
            <View
              style={{
                backgroundColor: theme.colors.surface,
                borderRadius: 16,
                padding: 16,
                height: "100%",
              }}
            >
              <Text style={styles.label}>
                Domain:{" "}
                <Text style={[styles.value, { color: theme.colors.primary }]}>
                  {item.domain}
                </Text>
              </Text>

              <Text style={styles.label}>
                Username:{" "}
                <Text style={[styles.value, { color: theme.colors.primary }]}>
                  {item.username}
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
                  {item.updatedAt == null
                    ? "just now"
                    : formatDate(item.updatedAt)}
                </Text>
              </Text>
            </View>
          </Pressable>
        )}
      />

      <FAB
        style={{
          position: "absolute",
          bottom: 35,
          right: 35,
        }}
        icon={({ size, color }) => (
          <FontAwesome6
            name="plus"
            size={size}
            color={color}
            iconStyle="solid"
          />
        )}
        onPress={() => {
          router.push("/save-password");
        }}
      />
    </>
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

  emptyText: {
    textAlign: "center",
    marginTop: 40,
    fontSize: 12,
    color: "gray",
  },
});
