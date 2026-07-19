import Text from "@/components/Text";
import { passwords } from "@/db/schema";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { FlashList } from "@shopify/flash-list";
import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router, Stack } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useEffect, useState } from "react";
import { Alert, Pressable, StyleSheet, View } from "react-native";
import { FAB, useTheme } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function LoadPasswordScreen() {
  let theme = useTheme();
  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const [passwordList, setPasswordList] = useState<any[]>([]);

  function deletePassword(password: any) {
    Alert.alert("Delete?", `${password.domain} : ${password.username}`, [
      {
        text: "Cancel",
        style: "cancel",
      },
      {
        text: "Delete",
        onPress: () => {
          drizzleDb
            .delete(passwords)
            .where(eq(passwords.id, password.id))
            .then(() => {
              setPasswordList((prev) =>
                prev.filter((item) => item.id !== password.id),
              );
            });
        },
        style: "destructive",
      },
    ]);
  }

  useEffect(() => {
    drizzleDb
      .select()
      .from(passwords)
      .then((result) => setPasswordList(result));
  }, []);

  return (
    <>
      <Stack.Title>Password Manager</Stack.Title>
      <SafeAreaView style={{ flex: 1 }}>
        <FlashList
          data={passwordList}
          keyExtractor={(item) => item.id.toString()}
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
                  <Text
                    style={[styles.value, { color: theme.colors.tertiary }]}
                  >
                    **********
                  </Text>
                </Text>

                <Text style={[styles.label, { color: theme.colors.onSurface }]}>
                  Updated At:{" "}
                  <Text
                    style={[styles.value, { color: theme.colors.tertiary }]}
                  >
                    {item.updatedAt.split("T")[0]}
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
