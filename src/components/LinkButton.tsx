import { Link, LinkProps, useTheme } from "expo-router";
import { Text } from "react-native";

type Props = LinkProps & {
  text: string;
};

export default function LinkButton({ text, ...props }: Props) {
  let theme = useTheme();

  return (
    <Link
      asChild
      {...props}
      style={[
        {
          borderRadius: 12,
          borderWidth: 2,
          paddingInline: 8,
          paddingBlock: 12,
          textAlign: "center",
          borderColor: theme.colors.border,
          color: theme.colors.text,
        },
        props.style,
      ]}
    >
      <Text style={{ fontSize: 12 }}>{text}</Text>
    </Link>
  );
}
