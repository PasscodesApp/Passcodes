import { useTheme } from "expo-router";
import { StyleSheet, Text, TextProps } from "react-native";

type Props = TextProps & {
  title: string;
};

export default function ScreenHeading({ title, ...props }: Props) {
  let theme = useTheme();

  return (
    <Text
      {...props}
      style={[styles.title, { color: theme.colors.text }, props.style]}
    >
      {title}
    </Text>
  );
}

const styles = StyleSheet.create({
  title: {
    fontSize: 32,
    fontWeight: "bold",
    textAlign: "center",
  },
});
