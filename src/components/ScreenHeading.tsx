import { StyleSheet, Text, TextProps } from "react-native";

type Props = TextProps & {
  title: string;
};

export default function ScreenHeading({ title, ...props }: Props) {
  return (
    <Text {...props} style={[props.style, styles.title]}>
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
