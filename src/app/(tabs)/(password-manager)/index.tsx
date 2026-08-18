import PasswordItemCard from "@/components/PasswordItemCard";
import Text from "@/components/Text";
import { passwords } from "@/db/schema";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { FlashList } from "@shopify/flash-list";
import { eq } from "drizzle-orm";
import { drizzle, useLiveQuery } from "drizzle-orm/expo-sqlite";
import { router, Stack } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useMemo, useState } from "react";
import { Alert, Pressable } from "react-native";
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

  const filteredPasswords = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();

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
      <Stack.Screen
        options={{
          title: "Password Manager",
          headerRight: (props) => (
            <IconButton
              icon={() => (
                <FontAwesome6
                  name={numColumns === 1 ? "grip" : "list"}
                  iconStyle="solid"
                  size={20}
                  color={props.tintColor}
                />
              )}
              onPress={() => {
                setForceGrid((g) => !g);
              }}
            />
          ),
        }}
      >
        <Stack.Screen.Title>Password Manager</Stack.Screen.Title>

        <Stack.SearchBar
          textColor={theme.colors.onSurface}
          hintTextColor={theme.colors.onSurface}
          tintColor={theme.colors.onSurface}
          headerIconColor={theme.colors.onSurface}
          barTintColor={theme.colors.surfaceDisabled}
          placeholder="Search passwords..."
          placement="stacked"
          onChangeText={(event) => {
            setSearchQuery(event.nativeEvent.text);
          }}
          onSearchButtonPress={(event) => {
            setSearchQuery(event.nativeEvent.text);
          }}
        />
      </Stack.Screen>

      <FlashList
        data={filteredPasswords}
        key={numColumns}
        numColumns={numColumns}
        keyExtractor={(item) => item.id.toString()}
        refreshing={refreshing}
        onRefresh={onRefresh}
        contentContainerStyle={{
          padding: 20,
        }}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <Text
            style={{
              textAlign: "center",
              marginTop: 40,
              fontSize: 12,
              color: "gray",
            }}
          >
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
            <PasswordItemCard
              {...item}
              style={{
                height: "100%",
              }}
            />
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
