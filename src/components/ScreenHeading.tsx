import { StyleSheet, Text } from "react-native";

type Props = {
  title: string;
};

export default function ScreenHeading({ title }: Props) {
  return <Text style={styles.title}>{title}</Text>;
}

const styles = StyleSheet.create({
  title: {
    fontSize: 32,
    fontWeight: "bold",
    textAlign: "center",
  },
});
