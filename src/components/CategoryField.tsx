import CategoryNameDialog from "@/components/CategoryNameDialog";
import FormTextField from "@/components/FormTextField";
import Text from "@/components/Text";
import { useCategoryRepository } from "@/contexts/RepositoryContext";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { useState } from "react";
import { Modal, Pressable, ScrollView, View } from "react-native";
import {
  ActivityIndicator,
  Button,
  Divider,
  RadioButton,
  TextInput,
  useTheme,
} from "react-native-paper";

const UNCATEGORIZED_VALUE = "uncategorized";

type CategoryFieldProps = {
  /** Selected category id, `null` = Uncategorized. */
  value: number | null;
  onChange: (categoryId: number | null) => void;
  editable?: boolean;
};

/**
 * Form field that shows the selected category and, when editable, opens a
 * selector (with a "Create Category" step) on press.
 *
 * Used by BOTH the Add and the Edit password screens so they behave
 * identically.
 *
 * NOTE: Uses React Native's `Modal` (not react-native-paper's Portal based
 * `Modal` / `Dialog`) because the Add screen is a native `formSheet`, and
 * Paper's Portal renders underneath native screens.
 */
export default function CategoryField({
  value,
  onChange,
  editable = true,
}: CategoryFieldProps) {
  const categoryRepository = useCategoryRepository();

  const {
    data: categoryList = [],
    error,
    updatedAt,
  } = useLiveQuery(categoryRepository.observeAll(), []);

  const [step, setStep] = useState<"closed" | "list" | "create">("closed");

  const selectedName =
    categoryList.find((category) => category.id === value)?.name ??
    "Uncategorized";

  return (
    <>
      <Pressable
        disabled={!editable}
        onPress={() => setStep("list")}
        accessibilityRole="button"
        accessibilityLabel={`Category: ${selectedName}`}
        accessibilityHint={editable ? "Opens the category list" : undefined}
      >
        <View pointerEvents="none">
          <FormTextField
            label="Category"
            value={selectedName}
            editable={false}
            right={
              editable ? (
                <TextInput.Icon
                  icon={({ size, color }) => (
                    <FontAwesome6
                      name="chevron-down"
                      size={size}
                      color={color}
                      iconStyle="solid"
                    />
                  )}
                />
              ) : undefined
            }
          />
        </View>
      </Pressable>

      {step === "list" && (
        <CategorySelectorModal
          value={value}
          categories={categoryList}
          isLoading={updatedAt === undefined && !error}
          hasError={!!error}
          onSelect={(categoryId) => {
            onChange(categoryId);
            setStep("closed");
          }}
          onCreate={() => setStep("create")}
          onClose={() => setStep("closed")}
        />
      )}

      {step === "create" && (
        <CategoryNameDialog
          title="Create Category"
          confirmLabel="Create"
          onSubmit={async (name) => {
            const created = await categoryRepository.create(name);

            // Select it right away and go back to the password form.
            onChange(created.id);
            setStep("closed");
          }}
          onCancel={() => setStep("list")}
        />
      )}
    </>
  );
}

// xxxxx Selector xxxxx

type CategorySelectorModalProps = {
  value: number | null;
  categories: { id: number; name: string }[];
  isLoading: boolean;
  hasError: boolean;
  onSelect: (categoryId: number | null) => void;
  onCreate: () => void;
  onClose: () => void;
};

function CategorySelectorModal({
  value,
  categories,
  isLoading,
  hasError,
  onSelect,
  onCreate,
  onClose,
}: CategorySelectorModalProps) {
  const theme = useTheme();

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
          <Text variant="titleLarge">Select Category</Text>

          {isLoading ? (
            <ActivityIndicator style={{ margin: 20 }} />
          ) : hasError ? (
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
                value={value === null ? UNCATEGORIZED_VALUE : String(value)}
                onValueChange={(selected) =>
                  onSelect(
                    selected === UNCATEGORIZED_VALUE ? null : Number(selected),
                  )
                }
              >
                <RadioButton.Item
                  label="Uncategorized"
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
                  No categories yet.
                </Text>
              )}
            </ScrollView>
          )}

          <Divider />

          <View style={{ flexDirection: "row-reverse", gap: 4 }}>
            <Button
              icon={({ size, color }) => (
                <FontAwesome6
                  name="plus"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={onCreate}
            >
              Create Category
            </Button>

            <Button onPress={onClose}>Cancel</Button>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}
