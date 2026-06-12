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
  return (
    <View>
      <Text style={styles.label}>
        {label}
        {isRequired && <Text style={{ color: "#ee0000" }}> *</Text>}
      </Text>

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
