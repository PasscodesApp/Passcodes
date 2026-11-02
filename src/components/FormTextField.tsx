import { forwardRef, useEffect, useRef, useState } from "react";
import {
  TextInput,
  type TextInputHandles,
  type TextInputProps,
} from "react-native-paper";

type Props = TextInputProps;

const FormTextField = forwardRef<TextInputHandles, Props>(
  ({ editable = true, ...props }, ref) => {
    const inputRef = useRef<TextInputHandles | null>(null);

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
        ref={(instance: TextInputHandles | null) => {
          inputRef.current = instance;

          if (typeof ref === "function") {
            ref(instance);
          } else if (ref) {
            ref.current = instance;
          }
        }}
        variant="outlined"
        style={{ fontSize: 12 }}
        editable={internalEditable}
        {...props}
      />
    );
  },
);

FormTextField.displayName = "FormTextField";

export default FormTextField;
