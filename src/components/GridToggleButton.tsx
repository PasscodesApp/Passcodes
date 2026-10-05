import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import type { ColorValue } from "react-native";
import { IconButton } from "react-native-paper";

/**
 * Appbar button that switches a password list between 1 column (list) and
 * 2 columns (grid). Shared by the home screen and the folder screen.
 */
export default function GridToggleButton({
  isGrid,
  onPress,
  color,
}: {
  isGrid: boolean;
  onPress: () => void;
  color?: ColorValue | undefined;
}) {
  return (
    <IconButton
      accessibilityLabel={isGrid ? "Show as list" : "Show as grid"}
      icon={() => (
        <FontAwesome6
          name={isGrid ? "list" : "grip"}
          iconStyle="solid"
          size={20}
          color={color}
        />
      )}
      onPress={onPress}
    />
  );
}
