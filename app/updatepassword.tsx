import { passwords } from "@/db/schema";
import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { router, useLocalSearchParams } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useEffect, useState } from "react";
import {
  Button,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function UpdatePassword() {
  const { id } = useLocalSearchParams();

  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  const [domain, setDomain] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [url, setUrl] = useState("");
  const [notes, setNotes] = useState("");

  useEffect(() => {
    async function loadPassword() {
      const result = await drizzleDb
        .select()
        .from(passwords)
        .where(eq(passwords.id, Number(id)));

      if (result.length > 0) {
        const data = result[0];

        setDomain(data.domain || "");
        setUsername(data.username || "");
        setPassword(data.password || "");
        setUrl(data.url || "");
        setNotes(data.notes || "");
      }
    }

    loadPassword();
  }, []);

  async function updatePassword() {
    await drizzleDb
      .update(passwords)
      .set({
        domain,
        username,
        password,
        url,
        notes,
        updatedAt: new Date().toISOString(),
      })
      .where(eq(passwords.id, Number(id)));

    router.back();
  }

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView
        contentContainerStyle={{
          padding: 20,
          gap: 16,
        }}
      >
        <Text style={styles.title}>Update Password</Text>

        <View>
          <Text style={styles.label}>Domain:</Text>
          <TextInput
            value={domain}
            onChangeText={setDomain}
            style={styles.input}
          />
        </View>

        <View>
          <Text style={styles.label}>Username:</Text>
          <TextInput
            value={username}
            onChangeText={setUsername}
            style={styles.input}
          />
        </View>

        <View>
          <Text style={styles.label}>Password:</Text>
          <TextInput
            value={password}
            onChangeText={setPassword}
            style={styles.input}
          />
        </View>

        <View>
          <Text style={styles.label}>URL:</Text>
          <TextInput value={url} onChangeText={setUrl} style={styles.input} />
        </View>

        <View>
          <Text style={styles.label}>Notes:</Text>
          <TextInput
            value={notes}
            onChangeText={setNotes}
            multiline
            style={styles.input}
          />
        </View>
        <Button title="Update Password" onPress={updatePassword} />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  title: {
    fontSize: 32,
    fontWeight: "bold",
    textAlign: "center",
    marginBottom: 10,
  },

  label: {
    fontSize: 16,
    fontWeight: 600,
  },

  input: {
    backgroundColor: "#fff",
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
  },
});
