import { forwardRef, useEffect, useRef, useState } from "react";
import { TextInput as RNTextInput } from "react-native";
import { TextInput, TextInputProps } from "react-native-paper";

type Props = TextInputProps;

const FormTextField = forwardRef<RNTextInput, Props>(
  ({ editable = true, ...props }, ref) => {
    const inputRef = useRef<RNTextInput>(null);

    const [internalEditable, setInternalEditable] = useState(editable);

    useEffect(() => {
      if (editable === internalEditable) {
        return;
      }

      if (!editable) {
        inputRef.current?.blur();
      }

      setInternalEditable(editable);
    }, [editable, internalEditable]);

    return (
      <TextInput
        ref={(instance: RNTextInput) => {
          inputRef.current = instance;

          if (typeof ref === "function") {
            ref(instance);
          } else if (ref) {
            ref.current = instance;
          }
        }}
        mode="outlined"
        style={{ fontSize: 12 }}
        editable={internalEditable}
        {...props}
      />
    );
  },
);

FormTextField.displayName = "FormTextField";

export default FormTextField;
