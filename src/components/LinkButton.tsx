import { Href, router } from "expo-router";
import { Button, type ButtonProps, useTheme } from "react-native-paper";
import Text from "./Text";

type Props = Omit<ButtonProps, "children" | "onPress"> & {
  text: string;
  href: Href;
  variant?: "primary" | "secondary";
};

export default function LinkButton({
  text,
  href,
  variant = "secondary",
  ...props
}: Props) {
  let theme = useTheme();

  return (
    <Button
      mode={variant === "secondary" ? "outlined" : "contained"}
      onPress={() => {
        router.navigate(href);
      }}
    >
      <Text
        style={{
          fontSize: 12,
          color:
            variant === "secondary"
              ? theme.colors.secondary
              : theme.colors.onPrimary,
        }}
      >
        {text}
      </Text>
    </Button>
  );
}
