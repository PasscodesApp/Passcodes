import AddPasswordFab from "@/components/AddPasswordFab";
import GridToggleButton from "@/components/GridToggleButton";
import PasswordListItem from "@/components/PasswordListItem";
import SelectionBar from "@/components/SelectionBar";
import Text from "@/components/Text";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import { UNCATEGORIZED_LABEL } from "@/repositories/CategoryRepository";
import { useCategoryActions } from "@/libs/category_actions";
import {
  clearSelection,
  useSelectionBackHandler,
} from "@/libs/selection_store";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { FlashList } from "@shopify/flash-list";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import {
  router,
  Stack,
  useFocusEffect,
  useLocalSearchParams,
} from "expo-router";
import { useCallback, useMemo, useState } from "react";
import { View } from "react-native";
import { Button, IconButton } from "react-native-paper";

/**
 * The passwords of ONE folder (flat, no nesting).
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

  const { openActions, dialogElement } = useCategoryActions("folder");

  useFocusEffect(useCallback(() => clearSelection, []));
  useSelectionBackHandler();

  const title =
    categoryId === null
      ? UNCATEGORIZED_LABEL
      : (categoryList.find((category) => category.id === categoryId)?.name ??
        "Folder");

  const toggleGrid = useCallback(() => setForceGrid((g) => !g), []);

  const visibleIds = useMemo(
    () => passwordList.map((password) => password.id),
    [passwordList],
  );

  return (
    <>
      <Stack.Screen
        options={{
          title,
          headerRight: (props) => (
            <View style={{ flexDirection: "row" }}>
              <GridToggleButton
                isGrid={numColumns === 2}
                color={props.tintColor}
                onPress={toggleGrid}
              />

              {/* Overflow menu last (far right), like every Android app bar.
                  Unsorted has none: it can't be renamed or deleted. */}
              {categoryId !== null && (
                <IconButton
                  accessibilityLabel="Folder actions"
                  icon={() => (
                    <FontAwesome6
                      name="ellipsis-vertical"
                      iconStyle="solid"
                      size={20}
                      color={props.tintColor}
                    />
                  )}
                  onPress={() =>
                    openActions({ id: categoryId, name: title }, () =>
                      router.back(),
                    )
                  }
                />
              )}
            </View>
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
          paddingBottom: 110, // room for the FAB / selection bar
        }}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={{ alignItems: "center", marginTop: 40, gap: 12 }}>
            <Text style={{ textAlign: "center", fontSize: 12, color: "gray" }}>
              No passwords in this folder.
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
              onPress={() =>
                router.push({
                  pathname: "/save-password",
                  params:
                    categoryId === null
                      ? { lockCategory: "1" }
                      : { categoryId, lockCategory: "1" },
                })
              }
            >
              Add Password
            </Button>
          </View>
        }
        renderItem={({ item }) => <PasswordListItem item={item} />}
      />

      <SelectionBar allIds={visibleIds} />

      <AddPasswordFab categoryId={categoryId} />

      {dialogElement}
    </>
  );
}
