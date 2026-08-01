import Text from "@/components/Text";
import { passwords } from "@/db/schema";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { FlashList } from "@shopify/flash-list";
import { eq } from "drizzle-orm";
import { drizzle, useLiveQuery } from "drizzle-orm/expo-sqlite";
import { router } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import {
  Alert,
  Pressable,
  StyleSheet,
  useWindowDimensions,
  View,
} from "react-native";
import { FAB, IconButton, useTheme } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function LoadPasswordScreen() {
  const { width } = useWindowDimensions();

  const isLandscape = width > 700;
  const [forceGrid, setForceGrid] = useState(false);
  const numColumns = forceGrid || isLandscape ? 2 : 1;

  let theme = useTheme();
  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const [refreshKey, setRefreshKey] = useState(0);
  const { data: passwordList = [] } = useLiveQuery(
    drizzleDb.select().from(passwords),
    [refreshKey],
  );
  const [refreshing, setRefreshing] = useState(false);

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
    <SafeAreaView style={{ flex: 1, paddingVertical: 12 }}>
      <View
        style={{
          flexDirection: "row",
          justifyContent: "space-between",
          alignItems: "center",
          paddingHorizontal: 16,
          paddingVertical: 8,
        }}
      >
        <Text variant="headlineSmall">Passwords</Text>

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
        data={passwordList}
        key={numColumns}
        numColumns={numColumns}
        keyExtractor={(item) => item.id.toString()}
        refreshing={refreshing}
        onRefresh={onRefresh}
        contentContainerStyle={{
          paddingInline: 20,
        }}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={<Text style={styles.emptyText}>No Data!!</Text>}
        renderItem={({ item }) => (
          <Pressable
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
                marginBottom: 16,
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
                  {item.updatedAt?.slice(0, 10)}
                </Text>
              </Text>
            </View>
          </Pressable>
        )}
      />
      <FAB
        style={{ position: "absolute", bottom: 35, right: 35 }}
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
      ></FAB>
    </SafeAreaView>
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
