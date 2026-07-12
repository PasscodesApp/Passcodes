import { useState } from "react";
import { TextInput, TextInputProps } from "react-native-paper";

type SecureTextFieldProps = Omit<TextInputProps, "secureTextEntry" | "right">;

export default function SecureTextField({ ...props }: SecureTextFieldProps) {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  return (
    <TextInput
      mode="outlined"
      autoCapitalize="none"
      autoCorrect={false}
      secureTextEntry={!isPasswordVisible}
      right={
        <TextInput.Icon
          icon={isPasswordVisible ? "eye-off" : "eye"}
          onPress={() => setIsPasswordVisible((v) => !v)}
          forceTextInputFocus={false}
        />
      }
      {...props}
    />
  );
}
