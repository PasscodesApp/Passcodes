import { useSecretScreenController } from "@/features/screenshot-prevention";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { useState } from "react";
import { TextInput, TextInputProps } from "react-native-paper";
import FormTextField from "./FormTextField";

type StrippedTextFieldProps = Omit<TextInputProps, "secureTextEntry" | "right">;
type SecureTextFieldProps = StrippedTextFieldProps & {
  /* generally, you can set to name of screen route, we need it for screen shot prevention */
  screenCaptureKey: string;
};

export default function SecureTextField({
  screenCaptureKey,
  ...props
}: SecureTextFieldProps) {
  let { preventScreenShots, allowScreenShots } =
    useSecretScreenController(screenCaptureKey);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  return (
    <FormTextField
      mode="outlined"
      autoCapitalize="none"
      autoCorrect={false}
      secureTextEntry={!isPasswordVisible}
      right={
        <TextInput.Icon
          onPress={() => {
            setIsPasswordVisible((prev) => {
              // Note: this checks are hard to understand,
              // because we using previous state, read it twice!!
              if (!prev) {
                preventScreenShots();
              } else {
                allowScreenShots();
              }

              return !prev;
            });
          }}
          icon={({ size, color }) => (
            <FontAwesome6
              name={isPasswordVisible ? "eye-slash" : "eye"}
              size={size}
              color={color}
              iconStyle="solid"
            />
          )}
          forceTextInputFocus={false}
        />
      }
      {...props}
    />
  );
}
