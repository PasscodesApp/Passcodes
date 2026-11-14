import { faviconUrl } from "@/libs/favicon";
import { memo, useState } from "react";
import { Image, View } from "react-native";
import { useTheme } from "react-native-paper";

type FaviconProps = {
  /** Website host to fetch the icon for (see `getFaviconHost`). */
  host: string;
  size: number;
};

/**
 * A website icon. Shows nothing until the image has really loaded and
 * nothing at all if it can't be loaded (offline, unknown site), so an entry
 * never shows a broken-image box. Give it `key={host}` so a changed address
 * starts fresh.
 */
function Favicon({ host, size }: FaviconProps) {
  const theme = useTheme();
  const [loaded, setLoaded] = useState(false);
  const [failed, setFailed] = useState(false);

  if (failed) {
    return <View style={{ width: size, height: size }} />;
  }

  return (
    <View
      style={{
        width: size,
        height: size,
        borderRadius: size * 0.22,
        overflow: "hidden",
        backgroundColor: loaded ? theme.colors.surfaceVariant : "transparent",
      }}
    >
      <Image
        source={{ uri: faviconUrl(host) }}
        style={{ width: size, height: size, opacity: loaded ? 1 : 0 }}
        resizeMode="contain"
        accessible={false}
        onLoad={() => setLoaded(true)}
        onError={() => setFailed(true)}
      />
    </View>
  );
}

export default memo(Favicon);
