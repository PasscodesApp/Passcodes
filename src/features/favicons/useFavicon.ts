import { useCallback, useEffect, useState } from "react";

import type { FaviconProvider } from "./providers";
import {
  discardFavicon,
  fetchFavicon,
  readCachedFavicon,
} from "./cache";

/**
 * The local image address of a website's icon, or `null` while there is none
 * (not downloaded yet, lookup failed, or the provider is "off"): show the
 * placeholder then.
 *
 * A stored icon is returned immediately (no flash). Otherwise one download
 * is started for the chosen provider; the result is kept on the device.
 */
export function useFavicon(host: string, provider: FaviconProvider) {
  const [uri, setUri] = useState<string | null>(
    () => readCachedFavicon(host, provider).uri,
  );

  useEffect(() => {
    let active = true;
    const cached = readCachedFavicon(host, provider);

    setUri(cached.uri);

    if (cached.needsFetch && provider !== "off") {
      fetchFavicon(host, provider).then((fetched) => {
        if (active && fetched) {
          setUri(fetched);
        }
      });
    }

    return () => {
      active = false;
    };
  }, [host, provider]);

  /** Call when the stored image can't be drawn. */
  const onImageError = useCallback(() => {
    setUri(null);
    void discardFavicon(host, provider);
  }, [host, provider]);

  return { uri, onImageError };
}
