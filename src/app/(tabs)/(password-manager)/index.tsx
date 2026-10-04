import FolderList from "@/components/FolderList";
import GridToggleButton from "@/components/GridToggleButton";
import GroupedPasswordList from "@/components/GroupedPasswordList";
import HomeFab from "@/components/HomeFab";
import SelectionBar from "@/components/SelectionBar";
import { useCategoryActions, type CategoryNoun } from "@/libs/category_actions";
import { UNCATEGORIZED_KEY, useCategoryGroups } from "@/libs/category_groups";
import { useHomeLayout } from "@/libs/home_layout";
import {
  clearSelection,
  useSelectionBackHandler,
} from "@/libs/selection_store";
import { router, Stack, useFocusEffect } from "expo-router";
import { useCallback, useState } from "react";
import { useTheme } from "react-native-paper";

export default function LoadPasswordScreen() {
  const layout = useHomeLayout();
  const noun: CategoryNoun = layout === "folders" ? "folder" : "category";

  const [forceGrid, setForceGrid] = useState(false);
  const numColumns = forceGrid ? 2 : 1;

  const [searchQuery, setSearchQuery] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [refreshing, setRefreshing] = useState(false);

  const theme = useTheme();

  const query = searchQuery.trim().toLowerCase();
  const isSearching = query !== "";

  // Search finds passwords, so while searching always show the matches.
  const showFolders = layout === "folders" && !isSearching;

  const { groups, folders, visibleIds, hasCategories } = useCategoryGroups(
    query,
    refreshKey,
  );

  const { openCreate, openActions, dialogElement } = useCategoryActions(noun);

  // Leaving the screen ends multi-select; Android back leaves it first.
  useFocusEffect(useCallback(() => clearSelection, []));
  useSelectionBackHandler();

  const toggleGrid = useCallback(() => setForceGrid((g) => !g), []);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    setRefreshKey((k) => k + 1);
    setRefreshing(false);
  }, []);

  const onOpenFolder = useCallback((categoryId: number | null) => {
    router.push({
      pathname: "/category-passwords",
      params: {
        categoryId: categoryId === null ? UNCATEGORIZED_KEY : categoryId,
      },
    });
  }, []);

  return (
    <>
      <Stack.Screen
        options={{
          title: "Password Manager",
          // Always set (never omitted): the header keeps old options
          // otherwise. Folders have no list/grid switch, so render nothing.
          headerRight: (props) =>
            showFolders ? null : (
              <GridToggleButton
                isGrid={numColumns === 2}
                color={props.tintColor}
                onPress={toggleGrid}
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

      {showFolders ? (
        <FolderList
          folders={folders}
          hasCategories={hasCategories}
          refreshing={refreshing}
          onRefresh={onRefresh}
          onOpenFolder={onOpenFolder}
          onOpenActions={openActions}
        />
      ) : (
        <>
          <GroupedPasswordList
            groups={groups}
            numColumns={numColumns}
            forceExpanded={isSearching}
            refreshing={refreshing}
            onRefresh={onRefresh}
            emptyText={isSearching ? "No passwords found!!" : "No data!!"}
            onOpenActions={openActions}
          />

          <SelectionBar allIds={visibleIds} />
        </>
      )}

      <HomeFab layout={layout} noun={noun} onCreateCategory={openCreate} />

      {dialogElement}
    </>
  );
}
