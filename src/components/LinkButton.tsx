import { Href, router } from "expo-router";
import { Button, ButtonProps, useTheme } from "react-native-paper";
import Text from "./Text";

type Props = Omit<ButtonProps, "children"> & {
  text: string;
  href: Href;
};

export default function LinkButton({ text, href, ...props }: Props) {
  let theme = useTheme();

  return (
    <Button
      mode="contained-tonal"
      onPress={() => {
        router.navigate(href);
      }}
    >
      <Text style={{ fontSize: 12, color: theme.colors.primary }}>{text}</Text>
    </Button>
  );
}
