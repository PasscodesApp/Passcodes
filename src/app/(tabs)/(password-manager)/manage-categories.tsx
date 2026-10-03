import CategoryNameDialog from "@/components/CategoryNameDialog";
import Text from "@/components/Text";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import { useToast } from "@/contexts/ToastContext";
import {
  CategoryRepository,
  type DeleteCategoryPasswordsMode,
} from "@/repositories/CategoryRepository";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { useLocalSearchParams } from "expo-router";
import { Fragment, useMemo, useState } from "react";
import { Alert, ScrollView, View } from "react-native";
import {
  ActivityIndicator,
  Button,
  Card,
  Divider,
  FAB,
  IconButton,
  List,
  useTheme,
} from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

type Category = { id: number; name: string };

type DialogState =
  | { mode: "create" }
  | { mode: "rename"; category: Category }
  | null;

function passwordsLabel(count: number) {
  return `${count} ${count === 1 ? "password" : "passwords"}`;
}

export default function ManageCategoriesScreen() {
  const { create } = useLocalSearchParams<{ create?: string }>();

  const theme = useTheme();
  const { showToast } = useToast();

  const categoryRepository = useCategoryRepository();
  const passwordRepository = usePasswordRepository();

  const {
    data: categoryList = [],
    error,
    updatedAt,
  } = useLiveQuery(categoryRepository.observeAll(), []);

  // Only `category_id` is loaded, enough to count, no password secrets.
  const { data: passwordCategoryIds = [] } = useLiveQuery(
    passwordRepository.observeCategoryIds(),
    [],
  );

  const counts = useMemo(
    () => CategoryRepository.countPasswordsByCategory(passwordCategoryIds),
    [passwordCategoryIds],
  );

  const [dialog, setDialog] = useState<DialogState>(
    create === "1" ? { mode: "create" } : null,
  );

  const isLoading = updatedAt === undefined && !error;

  // xxxxx Delete flow xxxxx

  async function runDelete(
    category: Category,
    mode: DeleteCategoryPasswordsMode,
    count: number,
  ) {
    try {
      await categoryRepository.delete(category.id, mode);

      if (count === 0) {
        showToast("Category deleted");
      } else if (mode === "delete") {
        showToast(`Category and ${passwordsLabel(count)} deleted`);
      } else {
        showToast(`Category deleted, ${passwordsLabel(count)} moved to Uncategorized`);
      }
    } catch (err) {
      console.error("Failed to delete category:", err);
      showToast("Failed to delete category; please try again", "error");
    }
  }

  function confirmDeletePasswords(category: Category, count: number) {
    Alert.alert(
      `Delete ${passwordsLabel(count)}?`,
      `"${category.name}" and its ${passwordsLabel(count)} will be deleted permanently. This can't be undone.`,
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: () => runDelete(category, "delete", count),
        },
      ],
    );
  }

  function confirmDelete(category: Category) {
    const count = counts.get(category.id) ?? 0;

    if (count === 0) {
      Alert.alert("Delete category?", `"${category.name}"`, [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: () => runDelete(category, "uncategorize", 0),
        },
      ]);

      return;
    }

    Alert.alert(
      `Delete "${category.name}"?`,
      `This category has ${passwordsLabel(count)}. What should happen to them?`,
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete passwords too",
          style: "destructive",
          onPress: () => confirmDeletePasswords(category, count),
        },
        {
          // The safe default.
          text: "Move to Uncategorized",
          onPress: () => runDelete(category, "uncategorize", count),
        },
      ],
    );
  }

  // xxxxx Render xxxxx

  return (
    <SafeAreaView style={{ flex: 1 }}>
      {isLoading ? (
        <ActivityIndicator style={{ margin: 40 }} />
      ) : error ? (
        <Text
          style={{
            textAlign: "center",
            marginTop: 40,
            fontSize: 12,
            color: theme.colors.error,
          }}
        >
          Failed to load categories, please try again!!
        </Text>
      ) : categoryList.length === 0 ? (
        <View style={{ alignItems: "center", marginTop: 40, gap: 12 }}>
          <Text variant="titleMedium">No categories yet.</Text>

          <Text style={{ fontSize: 12, color: "gray" }}>
            Create your first category.
          </Text>

          <Button
            mode="contained"
            icon={({ size, color }) => (
              <FontAwesome6
                name="plus"
                size={size}
                color={color}
                iconStyle="solid"
              />
            )}
            onPress={() => setDialog({ mode: "create" })}
          >
            Create Category
          </Button>
        </View>
      ) : (
        <ScrollView
          contentContainerStyle={{
            margin: 20,
            gap: 16,
            paddingBottom: 100,
          }}
        >
          <Card>
            {categoryList.map((category, index) => (
              <Fragment key={category.id}>
                {index > 0 && <Divider />}

                <List.Item
                  title={category.name}
                  description={passwordsLabel(counts.get(category.id) ?? 0)}
                  left={(props) => (
                    <FontAwesome6
                      {...props}
                      style={[props.style, { marginVertical: "auto" }]}
                      name="folder"
                      iconStyle="solid"
                      size={20}
                    />
                  )}
                  right={() => (
                    <View style={{ flexDirection: "row" }}>
                      <IconButton
                        accessibilityLabel={`Rename ${category.name}`}
                        icon={() => (
                          <FontAwesome6
                            name="pencil"
                            size={18}
                            color={theme.colors.onSurface}
                            iconStyle="solid"
                          />
                        )}
                        onPress={() => setDialog({ mode: "rename", category })}
                      />

                      <IconButton
                        accessibilityLabel={`Delete ${category.name}`}
                        icon={() => (
                          <FontAwesome6
                            name="trash"
                            size={18}
                            color={theme.colors.error}
                            iconStyle="solid"
                          />
                        )}
                        onPress={() => confirmDelete(category)}
                      />
                    </View>
                  )}
                />
              </Fragment>
            ))}
          </Card>

          <Text
            style={{
              fontSize: 12,
              color: theme.colors.onSurfaceVariant,
              textAlign: "center",
            }}
          >
            Passwords without a category are shown as Uncategorized.
          </Text>
        </ScrollView>
      )}

      {!isLoading && !error && categoryList.length > 0 && (
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
          onPress={() => setDialog({ mode: "create" })}
        />
      )}

      {dialog?.mode === "create" && (
        <CategoryNameDialog
          title="Create Category"
          confirmLabel="Create"
          onSubmit={async (name) => {
            await categoryRepository.create(name);
            setDialog(null);
            showToast("Category created");
          }}
          onCancel={() => setDialog(null)}
        />
      )}

      {dialog?.mode === "rename" && (
        <CategoryNameDialog
          title="Rename Category"
          confirmLabel="Rename"
          initialName={dialog.category.name}
          onSubmit={async (name) => {
            await categoryRepository.rename(dialog.category.id, name);
            setDialog(null);
            showToast("Category renamed");
          }}
          onCancel={() => setDialog(null)}
        />
      )}
    </SafeAreaView>
  );
}
