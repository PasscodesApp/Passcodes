import { Link, LinkProps } from "expo-router";
import { Text } from "react-native";

type Props = LinkProps & {
  text: string;
};

export default function LinkButton({ text, ...props }: Props) {
  return (
    <Link
      {...props}
      style={[
        props.style,
        {
          borderRadius: 12,
          borderWidth: 2,
          paddingInline: 6,
          paddingBlock: 12,
        },
      ]}
      asChild
    >
      <Text style={{ fontSize: 16 }}>{text}</Text>
    </Link>
  );
}
