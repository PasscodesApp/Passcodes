import PasswordListItem from "@/components/PasswordListItem";
import Text from "@/components/Text";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { FlashList } from "@shopify/flash-list";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { router, Stack, useLocalSearchParams } from "expo-router";
import { useState } from "react";
import { View } from "react-native";
import { Button, FAB, IconButton } from "react-native-paper";

/**
 * Passwords of ONE category (flat, no nesting).
 * Route param `categoryId`: a category id, or "uncategorized".
 */
export default function CategoryPasswordsScreen() {
  const { categoryId: categoryIdParam } = useLocalSearchParams<{
    categoryId?: string;
  }>();

  const parsed = Number(categoryIdParam);
  const categoryId = Number.isInteger(parsed) && parsed > 0 ? parsed : null;

  const [forceGrid, setForceGrid] = useState(false);
  const numColumns = forceGrid ? 2 : 1;

  const passwordRepository = usePasswordRepository();
  const categoryRepository = useCategoryRepository();

  const { data: passwordList = [] } = useLiveQuery(
    passwordRepository.observeByCategory(categoryId),
    [categoryId],
  );

  const { data: categoryList = [] } = useLiveQuery(
    categoryRepository.observeAll(),
    [],
  );

  const title =
    categoryId === null
      ? "Uncategorized"
      : (categoryList.find((category) => category.id === categoryId)?.name ??
        "Category");

  // Preselect this category on the Add screen (Uncategorized = no param).
  function addPassword() {
    router.push({
      pathname: "/save-password",
      params: categoryId === null ? {} : { categoryId },
    });
  }

  return (
    <>
      <Stack.Screen
        options={{
          title,
          headerRight: (props) => (
            <IconButton
              accessibilityLabel={
                numColumns === 1 ? "Show as grid" : "Show as list"
              }
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
      />

      <FlashList
        data={passwordList}
        key={numColumns}
        numColumns={numColumns}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={{
          padding: 20,
        }}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={{ alignItems: "center", marginTop: 40, gap: 12 }}>
            <Text style={{ textAlign: "center", fontSize: 12, color: "gray" }}>
              No passwords in this category.
            </Text>

            <Button
              icon={({ size, color }) => (
                <FontAwesome6
                  name="plus"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={addPassword}
            >
              Add Password
            </Button>
          </View>
        }
        renderItem={({ item }) => <PasswordListItem item={item} />}
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
        onPress={addPassword}
      />
    </>
  );
}
