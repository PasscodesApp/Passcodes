import { StyleSheet, type Text as NativeText } from "react-native";
import { TextProps } from "react-native-paper";
import Text from "./Text";

type Props = Omit<TextProps<NativeText>, "children"> & {
  title: string;
};

export default function ScreenHeading({ title, ...props }: Props) {
  return (
    <Text {...props} style={[styles.title, props.style]}>
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
