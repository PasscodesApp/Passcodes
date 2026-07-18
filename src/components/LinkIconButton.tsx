import { router, type Href } from "expo-router";
import { useColorScheme } from "react-native";
import { IconButton, type IconButtonProps } from "react-native-paper";
import { type IconSource } from "react-native-paper/lib/typescript/components/Icon";

type IconProps = Omit<IconButtonProps, "onPress"> & {
  href: Href;
  icon: IconSource;
  darkModeColor: string;
};

export function LinkIconButton({ href, icon, darkModeColor }: IconProps) {
  let scheme = useColorScheme();

  return (
    <IconButton
      mode="outlined"
      onPress={() => {
        router.navigate(href);
      }}
      icon={icon}
      style={scheme === "dark" ? { backgroundColor: darkModeColor } : undefined}
    />
  );
}
