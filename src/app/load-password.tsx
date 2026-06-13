import ScreenHeading from "@/components/ScreenHeading";
import { passwords } from "@/db/schema";
import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useEffect, useState } from "react";
import { Button, FlatList, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function LoadPasswordScreen() {
  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const [passwordList, setPasswordList] = useState<any[]>([]);

  function deletePasswordById(id: number) {
    drizzleDb
      .delete(passwords)
      .where(eq(passwords.id, id))
      .then(() => {
        setPasswordList((prev) => prev.filter((item) => item.id !== id));
      });
  }

  useEffect(() => {
    drizzleDb
      .select()
      .from(passwords)
      .then((result) => setPasswordList(result));
  }, []);

  return (
    <SafeAreaView
      style={{
        flex: 1,
        backgroundColor: "#f5f5f5",
        padding: 20,
        gap: 12,
      }}
    >
      <ScreenHeading title="Password Manager" />

      <FlatList
        data={passwordList}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={{
          paddingBottom: 20,
        }}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={<Text style={styles.emptyText}>No Data!!</Text>}
        renderItem={({ item }) => (
          <View
            style={{
              backgroundColor: "#fff",
              borderRadius: 16,
              padding: 16,
              marginBottom: 16,
            }}
          >
            <Text style={styles.label}>
              Domain: <Text style={styles.value}>{item.domain}</Text>
            </Text>

            <Text style={styles.label}>
              Username: <Text style={styles.value}>{item.username}</Text>
            </Text>

            <Text style={styles.label}>
              Password: <Text style={styles.value}>{item.password}</Text>
            </Text>

            <Text style={styles.label}>
              Updated At: <Text style={styles.value}>{item.updatedAt}</Text>
            </Text>

            <View style={{ gap: 10, marginTop: 10 }}>
              <Button
                title="View"
                onPress={() =>
                  router.push({
                    pathname: "/password-details",
                    params: { id: item.id },
                  })
                }
              />
              <Button
                title="Delete"
                color="#e63946"
                onPress={() => deletePasswordById(item.id)}
              />
            </View>
          </View>
        )}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  label: {
    fontSize: 15,
    fontWeight: "600",
    marginBottom: 6,
    color: "#333",
  },

  value: {
    fontWeight: "400",
    color: "#555",
  },

  emptyText: {
    textAlign: "center",
    marginTop: 40,
    fontSize: 16,
    color: "gray",
  },
});
