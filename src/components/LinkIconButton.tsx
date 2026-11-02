import { router, type Href } from "expo-router";
import { useColorScheme } from "react-native";
import { IconButton, type IconButtonProps } from "react-native-paper";

type IconProps = Omit<IconButtonProps, "onPress"> & {
  href: Href;
  icon: IconButtonProps["icon"];
  darkModeColor: string;
};

export function LinkIconButton({ href, icon, darkModeColor }: IconProps) {
  const scheme = useColorScheme();

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
