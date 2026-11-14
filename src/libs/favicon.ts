/**
 * Website icons ("favicons") for saved passwords: which sites get one, and
 * where the image comes from.
 *
 * PRIVACY: getting an icon tells the icon service which websites are saved.
 * That is why the source is a setting (Settings > Website icons, including
 * "Off") and why nothing is requested for entries without a web address.
 */

export type FaviconProvider = "google" | "duckduckgo" | "faviconim" | "off";

export const DEFAULT_FAVICON_PROVIDER: FaviconProvider = "google";

export const FAVICON_PROVIDERS: ReadonlyArray<{
  id: FaviconProvider;
  label: string;
  description: string;
}> = [
  {
    id: "google",
    label: "Google",
    description: "Best coverage. Google sees the site names.",
  },
  {
    id: "duckduckgo",
    label: "DuckDuckGo",
    description: "Privacy-focused. DuckDuckGo sees the site names.",
  },
  {
    id: "faviconim",
    label: "Favicon.im",
    description: "Independent service. It sees the site names.",
  },
  {
    id: "off",
    label: "Off",
    description: "Nothing is downloaded. Placeholder icons only.",
  },
];

export function getFaviconProviderLabel(provider: FaviconProvider): string {
  return FAVICON_PROVIDERS.find((item) => item.id === provider)?.label ?? "";
}

/**
 * Image address for a host from the chosen provider, or `null` for "off".
 * To add a provider or point at your own server later, change only this
 * function and the list above.
 */
export function faviconUrl(
  host: string,
  provider: FaviconProvider,
): string | null {
  switch (provider) {
    case "google":
      return `https://www.google.com/s2/favicons?domain=${host}&sz=128`;
    case "duckduckgo":
      return `https://icons.duckduckgo.com/ip3/${host}.ico`;
    case "faviconim":
      return `https://favicon.im/${host}?larger=true`;
    case "off":
      return null;
  }
}

const WEB_ADDRESS = /^(?:(https?):\/\/)?(?:[^@/?#]*@)?([^/:?#\s]+)/i;

/**
 * The website host of a saved address ("https://www.example.com/login" ->
 * "www.example.com"), or `null` when there is nothing sensible to look up:
 * empty, a non-web address (e.g. an app link), a name without a dot, or a
 * private / local address (never probed from a password manager).
 */
export function getFaviconHost(url: string | null | undefined): string | null {
  const text = url?.trim();

  if (!text) {
    return null;
  }

  // An explicit non-web scheme (androidapp://, ftp://, ...) has no website.
  if (/^[a-z][a-z0-9+.-]*:\/\//i.test(text) && !/^https?:\/\//i.test(text)) {
    return null;
  }

  const host = WEB_ADDRESS.exec(text)?.[2]?.toLowerCase();

  if (!host || !host.includes(".") || host.endsWith(".")) {
    return null;
  }

  if (!/^[a-z0-9.-]+$/.test(host)) {
    return null;
  }

  const isPrivate =
    host.endsWith(".local") ||
    host.endsWith(".internal") ||
    host.endsWith(".lan") ||
    /^(10|127)\./.test(host) ||
    /^192\.168\./.test(host) ||
    /^172\.(1[6-9]|2\d|3[01])\./.test(host) ||
    /^169\.254\./.test(host);

  return isPrivate ? null : host;
}
