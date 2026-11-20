import FontAwesome6 from "@react-native-vector-icons/fontawesome6/static";
import { Pressable, StyleSheet, View } from "react-native";
import { Text } from "react-native-paper";

import type { ToastState } from "./types";

type ToastViewProps = {
  toast: ToastState;
  onUndo: () => void;
};

/**
 * The toast pill: icon, message and (when the toast has an `onUndo`) an
 * "Undo" button. Static: no animation.
 *
 * The wrapper ignores touches (`box-none`) so only the Undo button is
 * tappable; everything else passes through to the screen below.
 */
export default function ToastView({ toast, onUndo }: ToastViewProps) {
  const isError = toast.type === "error";

  return (
    <View pointerEvents="box-none" style={styles.wrapper}>
      <View
        style={[
          styles.pill,
          { backgroundColor: isError ? "#7f1e19" : "#323232" },
        ]}
      >
        <FontAwesome6
          name={isError ? "xmark" : "check"}
          size={16}
          color="#ffffff"
          iconStyle="solid"
        />

        <Text variant="bodyMedium" style={styles.message}>
          {toast.message}
        </Text>

        {toast.onUndo !== undefined && (
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="Undo"
            hitSlop={8}
            onPress={onUndo}
            style={styles.undoButton}
          >
            <Text variant="labelLarge" style={styles.undoLabel}>
              Undo
            </Text>
          </Pressable>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    position: "absolute",
    left: 20,
    right: 20,
    bottom: 100,
    alignItems: "center",
  },
  pill: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 24,
    elevation: 4,
  },
  message: {
    color: "#ffffff",
    flexShrink: 1,
  },
  undoButton: {
    marginLeft: 8,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  undoLabel: {
    color: "#8ab4f8",
    fontWeight: "bold",
  },
});
