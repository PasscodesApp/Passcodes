import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6/static";
import { memo } from "react";
import { Image, View } from "react-native";
import { useTheme } from "react-native-paper";

import { useFaviconProvider } from "./setting";
import { useFavicon } from "./useFavicon";

type FaviconProps = {
  /** Website host the icon belongs to (see `getFaviconHost`). */
  host: string;
  size: number;
};

/**
 * A website icon with a placeholder (a globe) that stays in place while the
 * icon is loading, when it can't be fetched, and when icons are turned off.
 * Icons come from the on-device cache (see ./cache.ts).
 */
function Favicon({ host, size }: FaviconProps) {
  const theme = useTheme();
  const provider = useFaviconProvider();
  const { uri, onImageError } = useFavicon(host, provider);

  return (
    <View
      style={{
        width: size,
        height: size,
        borderRadius: size * 0.25,
        overflow: "hidden",
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: theme.colors.surfaceVariant,
      }}
    >
      {uri ? (
        <Image
          source={{ uri }}
          style={{ width: size, height: size }}
          resizeMode="contain"
          accessible={false}
          onError={onImageError}
        />
      ) : (
        <FontAwesome6
          name="globe"
          iconStyle="solid"
          size={size * 0.5}
          color={theme.colors.onSurfaceVariant}
        />
      )}
    </View>
  );
}

export default memo(Favicon);
