import CategoryNameDialog from "@/components/CategoryNameDialog";
import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import { useToast } from "@/contexts/ToastContext";
import {
  UNCATEGORIZED_LABEL,
  type DeleteCategoryPasswordsMode,
} from "@/repositories/CategoryRepository";
import { useCallback, useState } from "react";
import { Alert } from "react-native";

/** The UI calls the same thing "category" or "folder" depending on layout. */
export type CategoryNoun = "category" | "folder";

export type CategoryRef = { id: number; name: string };

type DialogState =
  | { mode: "create" }
  | { mode: "rename"; category: CategoryRef }
  | null;

function passwordsLabel(count: number) {
  return `${count} ${count === 1 ? "password" : "passwords"}`;
}

/**
 * Create / rename / delete flows for categories, shared by both layouts.
 *
 * Returns stable callbacks plus `dialogElement`, which the screen renders
 * once (it is `null` unless a name dialog is open).
 */
export function useCategoryActions(noun: CategoryNoun) {
  const categoryRepository = useCategoryRepository();
  const passwordRepository = usePasswordRepository();
  const { showToast } = useToast();

  const [dialog, setDialog] = useState<DialogState>(null);

  const Noun = noun === "folder" ? "Folder" : "Category";

  const runDelete = useCallback(
    async (
      category: CategoryRef,
      mode: DeleteCategoryPasswordsMode,
      count: number,
      onDeleted?: () => void,
    ) => {
      try {
        await categoryRepository.delete(category.id, mode);
        onDeleted?.();

        if (count === 0) {
          showToast(`${Noun} deleted`);
        } else if (mode === "delete") {
          showToast(`${Noun} and ${passwordsLabel(count)} deleted`);
        } else {
          showToast(
            `${Noun} deleted, ${passwordsLabel(count)} moved to ${UNCATEGORIZED_LABEL}`,
          );
        }
      } catch (err) {
        console.error(`Failed to delete ${noun}:`, err);
        showToast(`Failed to delete ${noun}; please try again`, "error");
      }
    },
    [categoryRepository, showToast, Noun, noun],
  );

  const confirmDelete = useCallback(
    async (category: CategoryRef, onDeleted?: () => void) => {
      let count = 0;

      try {
        count = await passwordRepository.countByCategory(category.id);
      } catch (err) {
        console.error("Failed to count passwords:", err);
        showToast(`Failed to delete ${noun}; please try again`, "error");
        return;
      }

      if (count === 0) {
        Alert.alert(`Delete ${noun}?`, `"${category.name}"`, [
          { text: "Cancel", style: "cancel" },
          {
            text: "Delete",
            style: "destructive",
            onPress: () => runDelete(category, "uncategorize", 0, onDeleted),
          },
        ]);

        return;
      }

      Alert.alert(
        `Delete "${category.name}"?`,
        `This ${noun} has ${passwordsLabel(count)}. What should happen to them?`,
        [
          { text: "Cancel", style: "cancel" },
          {
            text: "Delete passwords too",
            style: "destructive",
            onPress: () =>
              // Destructive path needs its own, separate confirmation.
              Alert.alert(
                `Delete ${passwordsLabel(count)}?`,
                `"${category.name}" and its ${passwordsLabel(count)} will be deleted permanently. This can't be undone.`,
                [
                  { text: "Cancel", style: "cancel" },
                  {
                    text: "Delete",
                    style: "destructive",
                    onPress: () => runDelete(category, "delete", count, onDeleted),
                  },
                ],
              ),
          },
          {
            // The safe default.
            text: `Move to ${UNCATEGORIZED_LABEL}`,
            onPress: () => runDelete(category, "uncategorize", count, onDeleted),
          },
        ],
      );
    },
    [passwordRepository, runDelete, showToast, noun],
  );

  const openCreate = useCallback(() => setDialog({ mode: "create" }), []);

  /** Rename / Delete menu for one category. */
  const openActions = useCallback(
    (category: CategoryRef, onDeleted?: () => void) => {
      Alert.alert(category.name, undefined, [
        { text: "Cancel", style: "cancel" },
        {
          text: "Rename",
          onPress: () => setDialog({ mode: "rename", category }),
        },
        {
          text: "Delete",
          style: "destructive",
          onPress: () => confirmDelete(category, onDeleted),
        },
      ]);
    },
    [confirmDelete],
  );

  const dialogElement =
    dialog?.mode === "create" ? (
      <CategoryNameDialog
        title={`Create ${Noun}`}
        confirmLabel="Create"
        onSubmit={async (name) => {
          await categoryRepository.create(name);
          setDialog(null);
          showToast(`${Noun} created`);
        }}
        onCancel={() => setDialog(null)}
      />
    ) : dialog?.mode === "rename" ? (
      <CategoryNameDialog
        title={`Rename ${Noun}`}
        confirmLabel="Rename"
        initialName={dialog.category.name}
        onSubmit={async (name) => {
          await categoryRepository.rename(dialog.category.id, name);
          setDialog(null);
          showToast(`${Noun} renamed`);
        }}
        onCancel={() => setDialog(null)}
      />
    ) : null;

  return { openCreate, openActions, dialogElement };
}
