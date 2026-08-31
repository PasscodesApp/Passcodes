import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { View } from "react-native";
import { Button } from "react-native-paper";

export default function PasswordDetailCardActions({
  isEditing,
  onOpen,
  onCancel,
}: {
  isEditing: boolean;
  onOpen: () => void;
  onCancel: () => void;
}) {
  return (
    <View
      style={{
        flexDirection: "row-reverse",
        gap: 4,
      }}
    >
      {!isEditing && (
        <Button
          icon={({ size, color }) => (
            <FontAwesome6
              name="link"
              size={size}
              color={color}
              iconStyle="solid"
            />
          )}
          onPress={onOpen}
        >
          Open
        </Button>
      )}

      {isEditing && (
        <Button
          icon={({ size, color }) => (
            <FontAwesome6
              name="xmark"
              size={size}
              color={color}
              iconStyle="solid"
            />
          )}
          onPress={onCancel}
        >
          Cancel
        </Button>
      )}
    </View>
  );
}
