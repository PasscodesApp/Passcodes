import PasswordListItem from "@/components/PasswordListItem";
import Text from "@/components/Text";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import type { passwords } from "@/db/schema";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { FlashList } from "@shopify/flash-list";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { router, Stack } from "expo-router";
import { useMemo, useState } from "react";
import { Pressable, StyleSheet, View } from "react-native";
import { Button, FAB, IconButton, useTheme } from "react-native-paper";

type Password = typeof passwords.$inferSelect;

const UNCATEGORIZED_KEY = "uncategorized";

type Row =
  | {
      kind: "section";
      key: string;
      name: string;
      count: number;
      passwords: Password[];
      expanded: boolean;
    }
  | {
      kind: "category";
      key: string;
      categoryId: number | null;
      name: string;
      count: number;
    };

export default function LoadPasswordScreen() {
  const [forceGrid, setForceGrid] = useState(false);
  const numColumns = forceGrid ? 2 : 1;

  const [viewMode, setViewMode] = useState<"grouped" | "categories">("grouped");
  const [collapsed, setCollapsed] = useState<ReadonlySet<string>>(new Set());

  const [searchQuery, setSearchQuery] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [refreshing, setRefreshing] = useState(false);

  const theme = useTheme();

  const passwordRepository = usePasswordRepository();
  const categoryRepository = useCategoryRepository();

  const { data: passwordList = [] } = useLiveQuery(
    passwordRepository.observeAll(),
    [refreshKey],
  );

  const { data: categoryList = [] } = useLiveQuery(
    categoryRepository.observeAll(),
    [],
  );

  const query = searchQuery.trim().toLowerCase();
  const isSearching = query !== "";

  const categoryNameById = useMemo(
    () => new Map(categoryList.map((category) => [category.id, category.name])),
    [categoryList],
  );

  /**
   * A password whose category no longer exists is treated as Uncategorized,
   * so it can never silently disappear from the list.
   */
  function resolveCategoryId(password: Password): number | null {
    return password.categoryId !== null &&
      categoryNameById.has(password.categoryId)
      ? password.categoryId
      : null;
  }

  const filteredPasswords = useMemo(() => {
    if (!query) {
      return passwordList;
    }

    return passwordList.filter((password) => {
      const domain = password.domain?.toLowerCase() ?? "";
      const username = password.username?.toLowerCase() ?? "";
      const url = password.url?.toLowerCase() ?? "";
      const notes = password.notes?.toLowerCase() ?? "";
      const categoryName = (
        (password.categoryId !== null
          ? categoryNameById.get(password.categoryId)
          : undefined) ?? "Uncategorized"
      ).toLowerCase();

      return (
        domain.includes(query) ||
        username.includes(query) ||
        url.includes(query) ||
        notes.includes(query) ||
        categoryName.includes(query)
      );
    });
  }, [passwordList, query, categoryNameById]);

  /** Grouped view: one collapsible section per non-empty category. */
  const sectionRows = useMemo<Row[]>(() => {
    const byCategory = new Map<number | null, Password[]>();

    for (const password of filteredPasswords) {
      const categoryId = resolveCategoryId(password);
      const bucket = byCategory.get(categoryId);

      if (bucket) {
        bucket.push(password);
      } else {
        byCategory.set(categoryId, [password]);
      }
    }

    const rows: Row[] = [];

    for (const category of categoryList) {
      const items = byCategory.get(category.id);

      if (items && items.length > 0) {
        const key = String(category.id);

        rows.push({
          kind: "section",
          key,
          name: category.name,
          count: items.length,
          passwords: items,
          // While searching, always show the matches.
          expanded: isSearching || !collapsed.has(key),
        });
      }
    }

    const uncategorized = byCategory.get(null);

    if (uncategorized && uncategorized.length > 0) {
      rows.push({
        kind: "section",
        key: UNCATEGORIZED_KEY,
        name: "Uncategorized",
        count: uncategorized.length,
        passwords: uncategorized,
        expanded: isSearching || !collapsed.has(UNCATEGORIZED_KEY),
      });
    }

    return rows;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filteredPasswords, categoryList, categoryNameById, collapsed, isSearching]);

  /** Category view: the categories themselves, with counts (all passwords). */
  const categoryRows = useMemo<Row[]>(() => {
    const counts = new Map<number | null, number>();

    for (const password of passwordList) {
      const categoryId = resolveCategoryId(password);
      counts.set(categoryId, (counts.get(categoryId) ?? 0) + 1);
    }

    const rows: Row[] = categoryList.map((category) => ({
      kind: "category",
      key: String(category.id),
      categoryId: category.id,
      name: category.name,
      count: counts.get(category.id) ?? 0,
    }));

    const uncategorizedCount = counts.get(null) ?? 0;

    if (uncategorizedCount > 0) {
      rows.push({
        kind: "category",
        key: UNCATEGORIZED_KEY,
        categoryId: null,
        name: "Uncategorized",
        count: uncategorizedCount,
      });
    }

    return rows;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [passwordList, categoryList, categoryNameById]);

  // Search finds passwords, so while searching always show grouped results.
  const showCategoryView = viewMode === "categories" && !isSearching;
  const rows = showCategoryView ? categoryRows : sectionRows;

  async function onRefresh() {
    setRefreshing(true);
    setRefreshKey((k) => k + 1);
    setRefreshing(false);
  }

  function toggleSection(key: string) {
    setCollapsed((current) => {
      const next = new Set(current);

      if (next.has(key)) {
        next.delete(key);
      } else {
        next.add(key);
      }

      return next;
    });
  }

  function openCategory(categoryId: number | null) {
    router.push({
      pathname: "/category-passwords",
      params: {
        categoryId: categoryId === null ? UNCATEGORIZED_KEY : categoryId,
      },
    });
  }

  return (
    <>
      <Stack.Screen
        options={{
          title: "Password Manager",
          headerRight: (props) => (
            <View style={{ flexDirection: "row" }}>
              <IconButton
                accessibilityLabel={
                  viewMode === "grouped"
                    ? "Show categories"
                    : "Show grouped passwords"
                }
                icon={() => (
                  <FontAwesome6
                    name={viewMode === "grouped" ? "folder" : "layer-group"}
                    iconStyle="solid"
                    size={20}
                    color={props.tintColor}
                  />
                )}
                onPress={() => {
                  setViewMode((mode) =>
                    mode === "grouped" ? "categories" : "grouped",
                  );
                }}
              />

              {!showCategoryView && (
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
              )}
            </View>
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
        data={rows}
        extraData={numColumns}
        keyExtractor={(item) => item.key}
        getItemType={(item) => item.kind}
        refreshing={refreshing}
        onRefresh={onRefresh}
        contentContainerStyle={{
          padding: 20,
        }}
        showsVerticalScrollIndicator={false}
        ListHeaderComponent={
          showCategoryView ? (
            <View style={{ alignItems: "center", gap: 4, marginBottom: 12 }}>
              {categoryList.length === 0 && (
                <Text style={{ textAlign: "center", fontSize: 12, color: "gray" }}>
                  No categories yet. Create your first category.
                </Text>
              )}

              <Button
                icon={({ size, color }) => (
                  <FontAwesome6
                    name={categoryList.length === 0 ? "plus" : "gear"}
                    size={size}
                    color={color}
                    iconStyle="solid"
                  />
                )}
                onPress={() =>
                  router.push({
                    pathname: "/manage-categories",
                    params: categoryList.length === 0 ? { create: "1" } : {},
                  })
                }
              >
                {categoryList.length === 0
                  ? "Create Category"
                  : "Manage Categories"}
              </Button>
            </View>
          ) : null
        }
        ListEmptyComponent={
          <Text
            style={{
              textAlign: "center",
              marginTop: 40,
              fontSize: 12,
              color: "gray",
            }}
          >
            {isSearching ? "No passwords found!!" : "No data!!"}
          </Text>
        }
        renderItem={({ item }) =>
          item.kind === "section" ? (
            <View style={{ marginBottom: 16 }}>
              <SectionHeader
                name={item.name}
                count={item.count}
                expanded={item.expanded}
                disabled={isSearching}
                onPress={() => toggleSection(item.key)}
              />

              {item.expanded && (
                <View style={{ flexDirection: "row", flexWrap: "wrap" }}>
                  {item.passwords.map((password) => (
                    <View
                      key={password.id}
                      style={{ width: numColumns === 2 ? "50%" : "100%" }}
                    >
                      <PasswordListItem
                        item={password}
                        style={{ flex: 1 }}
                      />
                    </View>
                  ))}
                </View>
              )}
            </View>
          ) : (
            <CategoryRow
              name={item.name}
              count={item.count}
              onPress={() => openCategory(item.categoryId)}
            />
          )
        }
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

// xxxxx Components xxxxx

function SectionHeader({
  name,
  count,
  expanded,
  disabled,
  onPress,
}: {
  name: string;
  count: number;
  expanded: boolean;
  disabled: boolean;
  onPress: () => void;
}) {
  const theme = useTheme();

  return (
    <Pressable
      disabled={disabled}
      onPress={onPress}
      accessibilityRole="button"
      accessibilityState={{ expanded }}
      accessibilityLabel={`${name}, ${count} ${count === 1 ? "password" : "passwords"}`}
      style={{
        flexDirection: "row",
        alignItems: "center",
        gap: 12,
        paddingVertical: 12,
        paddingHorizontal: 4,
        marginBottom: 8,
        borderBottomWidth: StyleSheet.hairlineWidth,
        borderBottomColor: theme.colors.outlineVariant,
      }}
    >
      <View style={{ width: 16, alignItems: "center" }}>
        <FontAwesome6
          name={expanded ? "chevron-down" : "chevron-right"}
          size={14}
          color={theme.colors.onSurface}
          iconStyle="solid"
        />
      </View>

      <Text variant="titleMedium" style={{ flex: 1 }} numberOfLines={1}>
        {name}
      </Text>

      <Text style={{ color: theme.colors.onSurfaceVariant }}>{count}</Text>
    </Pressable>
  );
}

function CategoryRow({
  name,
  count,
  onPress,
}: {
  name: string;
  count: number;
  onPress: () => void;
}) {
  const theme = useTheme();

  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={`${name}, ${count} ${count === 1 ? "password" : "passwords"}`}
      style={{
        flexDirection: "row",
        alignItems: "center",
        gap: 12,
        backgroundColor: theme.colors.surface,
        borderRadius: 16,
        padding: 16,
        margin: 2,
        marginBottom: 8,
      }}
    >
      <Text variant="titleMedium" style={{ flex: 1 }} numberOfLines={1}>
        {name}
      </Text>

      <Text style={{ color: theme.colors.onSurfaceVariant }}>{count}</Text>

      <FontAwesome6
        name="chevron-right"
        size={14}
        color={theme.colors.onSurfaceVariant}
        iconStyle="solid"
      />
    </Pressable>
  );
}
