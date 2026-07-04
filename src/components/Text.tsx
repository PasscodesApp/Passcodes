import { useTheme } from "expo-router";
import { Text as RNText, TextProps } from "react-native";

export default function Text(props: TextProps) {
  let theme = useTheme();

  return (
    <RNText {...props} style={[{ color: theme.colors.text }, props.style]}>
      {props.children}
    </RNText>
  );
}
