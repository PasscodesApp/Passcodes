import Text from "@/components/Text";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { StyleSheet } from "react-native";
import { Button } from "react-native-paper";

type GeneratorOptionProps = {
  label: string;
  example: string;
  enabled: boolean;
  onPress: () => void;
};

export default function GeneratorOption({
  label,
  example,
  enabled,
  onPress,
}: GeneratorOptionProps) {
  return (
    <Button
      mode="text"
      onPress={onPress}
      contentStyle={styles.optionButtonContent}
      style={styles.optionButton}
      icon={({ size, color }) => (
        <FontAwesome6
          name={enabled ? "square-check" : "square"}
          size={size}
          color={color}
          iconStyle="solid"
        />
      )}
    >
      <Text variant="bodySmall">
        {label} ({example})
      </Text>
    </Button>
  );
}

const styles = StyleSheet.create({
  optionButton: {
    marginHorizontal: 0,
  },

  optionButtonContent: {
    justifyContent: "flex-start",
  },
});
