import CategoryPickerSheet from "@/components/CategoryPickerSheet";
import FormTextField from "@/components/FormTextField";
import { useCategoryRepository } from "@/contexts/RepositoryContext";
import { UNCATEGORIZED_LABEL } from "@/repositories/CategoryRepository";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { useState } from "react";
import { Pressable, View } from "react-native";
import { TextInput } from "react-native-paper";

type CategoryFieldProps = {
  /** Selected category id, `null` = Uncategorized. */
  value: number | null;
  onChange: (categoryId: number | null) => void;
  editable?: boolean;
};

/**
 * Select-only Category field, shared by the Add and the Edit password
 * screens so they behave identically. Categories are created from the home
 * screen, not here.
 */
export default function CategoryField({
  value,
  onChange,
  editable = true,
}: CategoryFieldProps) {
  const categoryRepository = useCategoryRepository();

  const { data: categoryList = [] } = useLiveQuery(
    categoryRepository.observeAll(),
    [],
  );

  const [isOpen, setIsOpen] = useState(false);

  const selectedName =
    categoryList.find((category) => category.id === value)?.name ??
    UNCATEGORIZED_LABEL;

  return (
    <>
      <Pressable
        disabled={!editable}
        onPress={() => setIsOpen(true)}
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

      {isOpen && (
        <CategoryPickerSheet
          value={value}
          onSelect={(categoryId) => {
            onChange(categoryId);
            setIsOpen(false);
          }}
          onClose={() => setIsOpen(false)}
        />
      )}
    </>
  );
}
