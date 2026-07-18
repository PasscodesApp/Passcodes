import type { Text as NativeText } from "react-native";
import { Text as RNText, TextProps, useTheme } from "react-native-paper";

export default function Text(props: TextProps<NativeText>) {
  const theme = useTheme();

  return (
    <RNText {...props} style={[{ color: theme.colors.onSurface }, props.style]}>
      {props.children}
    </RNText>
  );
}
