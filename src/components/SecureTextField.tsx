import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { useState } from "react";
import { TextInput, TextInputProps } from "react-native-paper";
import FormTextField from "./FormTextField";

type SecureTextFieldProps = Omit<TextInputProps, "secureTextEntry" | "right">;

// isPasswordVisible ? "eye-off" : "eye"
export default function SecureTextField({ ...props }: SecureTextFieldProps) {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  return (
    <FormTextField
      mode="outlined"
      autoCapitalize="none"
      autoCorrect={false}
      secureTextEntry={!isPasswordVisible}
      right={
        <TextInput.Icon
          onPress={() => setIsPasswordVisible(!isPasswordVisible)}
          icon={({ size, color }) => (
            <FontAwesome6
              name={isPasswordVisible ? "eye-slash" : "eye"}
              size={size}
              color={color}
              iconStyle="regular"
            />
          )}
          forceTextInputFocus={false}
        />
      }
      {...props}
    />
  );
}
