import {
  StyleSheet,
  Text,
  TextInput,
  TextInputProps,
  View,
} from "react-native";

type Props = TextInputProps & {
  label: string;
};

export default function FormTextField({ label, ...props }: Props) {
  return (
    <View>
      <Text style={styles.label}>{label}</Text>

      <TextInput
        {...props}
        style={[
          styles.input,
          !props.editable && styles.readOnlyInput,
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
    backgroundColor: "#fff",
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
  },

  readOnlyInput: {
    backgroundColor: "#e1e1e1",
  },
});
