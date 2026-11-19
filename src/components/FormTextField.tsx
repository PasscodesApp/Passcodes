import {
  forwardRef,
  useEffect,
  useRef,
  type ForwardedRef,
} from "react";
import {
  TextInput,
  type TextInputHandles,
  type TextInputProps,
} from "react-native-paper";

type Props = TextInputProps;

// Module level: assigning to a forwarded ref inside the component body is
// flagged by the React Compiler lint (it looks like mutating a prop).
function assignRef(
  ref: ForwardedRef<TextInputHandles>,
  instance: TextInputHandles | null,
) {
  if (typeof ref === "function") {
    ref(instance);
  } else if (ref) {
    ref.current = instance;
  }
}

const FormTextField = forwardRef<TextInputHandles, Props>(
  ({ editable = true, ...props }, ref) => {
    const inputRef = useRef<TextInputHandles | null>(null);

    // Make sure a field that turns read-only never keeps the focus.
    // (No state mirroring `editable`: calling setState in an effect is a
    // react-hooks/set-state-in-effect error; screens also blur before they
    // flip `editable`, see `unfocusAllFields` in password-details.)
    useEffect(() => {
      if (!editable) {
        inputRef.current?.blur();
      }
    }, [editable]);

    return (
      <TextInput
        ref={(instance: TextInputHandles | null) => {
          inputRef.current = instance;
          assignRef(ref, instance);
        }}
        variant="outlined"
        style={{ fontSize: 12 }}
        editable={editable}
        {...props}
      />
    );
  },
);

FormTextField.displayName = "FormTextField";

export default FormTextField;
