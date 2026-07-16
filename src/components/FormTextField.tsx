import { TextInput, TextInputProps } from "react-native-paper";

type Props = TextInputProps;

export default function FormTextField({ ...props }: Props) {
  return <TextInput mode="outlined" style={{ fontSize: 12 }} {...props} />;
}
