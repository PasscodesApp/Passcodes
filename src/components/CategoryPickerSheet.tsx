import Text from "@/components/Text";
import { useCategoryRepository } from "@/contexts/RepositoryContext";
import { UNCATEGORIZED_LABEL } from "@/repositories/CategoryRepository";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { Modal, Pressable, ScrollView, View } from "react-native";
import {
  ActivityIndicator,
  Button,
  Divider,
  RadioButton,
  useTheme,
} from "react-native-paper";

const UNCATEGORIZED_VALUE = "uncategorized";

type CategoryPickerSheetProps = {
  title?: string;
  /** Currently selected id, `null` = Uncategorized, `undefined` = none. */
  value?: number | null;
  onSelect: (categoryId: number | null, name: string) => void;
  onClose: () => void;
};

/**
 * Bottom sheet that lists Uncategorized + all categories (select only,
 * categories are created from the home screen).
 *
 * Shared by the Category field (Add / Edit password) and by "Move" in the
 * multi-select bar.
 *
 * Uses React Native's `Modal` because Paper's Portal renders underneath the
 * native formSheet of the Add screen.
 */
export default function CategoryPickerSheet({
  title = "Select Category",
  value,
  onSelect,
  onClose,
}: CategoryPickerSheetProps) {
  const theme = useTheme();
  const categoryRepository = useCategoryRepository();

  const {
    data: categories = [],
    error,
    updatedAt,
  } = useLiveQuery(categoryRepository.observeAll(), []);

  const isLoading = updatedAt === undefined && !error;

  return (
    <Modal
      visible
      transparent
      animationType="fade"
      statusBarTranslucent
      onRequestClose={onClose}
    >
      <Pressable
        style={{
          flex: 1,
          backgroundColor: "rgba(0, 0, 0, 0.5)",
          justifyContent: "flex-end",
        }}
        onPress={onClose}
      >
        {/* Inner Pressable swallows touches so tapping the sheet doesn't close it. */}
        <Pressable
          onPress={() => {}}
          style={{
            backgroundColor: theme.colors.surface,
            padding: 20,
            gap: 16,
            maxHeight: "80%",
            borderTopLeftRadius: 20,
            borderTopRightRadius: 20,
          }}
        >
          <Text variant="titleLarge">{title}</Text>

          {isLoading ? (
            <ActivityIndicator style={{ margin: 20 }} />
          ) : error ? (
            <Text
              style={{
                textAlign: "center",
                fontSize: 12,
                color: theme.colors.error,
                marginVertical: 12,
              }}
            >
              Failed to load categories, please try again!!
            </Text>
          ) : (
            <ScrollView
              showsVerticalScrollIndicator={false}
              style={{ flexGrow: 0 }}
            >
              <RadioButton.Group
                value={
                  value === undefined
                    ? ""
                    : value === null
                      ? UNCATEGORIZED_VALUE
                      : String(value)
                }
                onValueChange={(selected) => {
                  if (selected === UNCATEGORIZED_VALUE) {
                    onSelect(null, UNCATEGORIZED_LABEL);
                    return;
                  }

                  const id = Number(selected);
                  const name =
                    categories.find((category) => category.id === id)?.name ??
                    "category";

                  onSelect(id, name);
                }}
              >
                <RadioButton.Item
                  label={UNCATEGORIZED_LABEL}
                  value={UNCATEGORIZED_VALUE}
                  position="leading"
                  labelStyle={{ textAlign: "left" }}
                />

                {categories.map((category) => (
                  <RadioButton.Item
                    key={category.id}
                    label={category.name}
                    value={String(category.id)}
                    position="leading"
                    labelStyle={{ textAlign: "left" }}
                  />
                ))}
              </RadioButton.Group>

              {categories.length === 0 && (
                <Text
                  style={{
                    textAlign: "center",
                    fontSize: 12,
                    color: "gray",
                    marginVertical: 12,
                  }}
                >
                  No categories yet. Create one from the home screen.
                </Text>
              )}
            </ScrollView>
          )}

          <Divider />

          <View style={{ flexDirection: "row-reverse" }}>
            <Button onPress={onClose}>Cancel</Button>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}
