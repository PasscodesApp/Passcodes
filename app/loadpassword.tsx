import { passwords } from "@/db/schema";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { useSQLiteContext } from "expo-sqlite";
import { useEffect, useState } from "react";
import { ScrollView, Text, View } from "react-native";

export default function LoadPassword() {
  const db = useSQLiteContext();
  const drizzleDb = drizzle(db);
  let [passwordList, setPasswordList] = useState<any[]>([]);

  useEffect(() => {
    drizzleDb
      .select()
      .from(passwords)
      .then((result) => setPasswordList(result));
  }, []);

  return (
    <ScrollView
      contentContainerStyle={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Text>Load Password</Text>

      {passwordList.length > 0 ? (
        passwordList.map((passwords) => (
          <View
            key={passwords.id}
            style={{ margin: 6, borderWidth: 4, borderRadius: 10, padding: 4 }}
          >
            <Text>Domain: {passwords.domain}</Text>
            <Text>Username: {passwords.username}</Text>
            <Text>Password: {passwords.passsword}</Text>
            <Text>URL: {passwords.url}</Text>
            <Text>Notes: {passwords.notes}</Text>
            <Text>Updated-At: {passwords.updatedAt}</Text>
          </View>
        ))
      ) : (
        <Text>No Data</Text>
      )}
    </ScrollView>
  );
}
