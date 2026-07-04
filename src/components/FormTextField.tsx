import { useTheme } from "expo-router";
import {
  StyleSheet,
  Text,
  TextInput,
  TextInputProps,
  View,
} from "react-native";

type Props = TextInputProps & {
  label: string;
  isRequired?: boolean;
};

export default function FormTextField({
  label,
  isRequired = false,
  ...props
}: Props) {
  let theme = useTheme();

  return (
    <View>
      <Text style={[styles.label, { color: theme.colors.text }]}>
        {label}
        {isRequired && <Text style={{ color: "#ee0000" }}> *</Text>}
      </Text>

      <TextInput
        {...props}
        style={[
          styles.input,
          {
            color: theme.colors.text,
            borderColor: theme.colors.border,
          },
          props.multiline && {
            minHeight: 100,
            textAlignVertical: "top",
          },
          props.style,
        ]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  label: {
    fontSize: 16,
    fontWeight: "600",
    marginBottom: 6,
  },

  input: {
    borderWidth: 1,
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
  },
});
