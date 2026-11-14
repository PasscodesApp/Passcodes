/**
 * Website icons ("favicons") for saved passwords.
 *
 * PRIVACY: showing an icon means asking an icon service for it, which tells
 * that service which websites are saved. That is why this is a setting
 * (Settings > Website icons) and why nothing is requested for entries without
 * a web address. To use another icon source, or your own server later,
 * change `faviconUrl` only.
 */

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

/** Where the icon image for a host comes from (PNG, returns 404 if unknown). */
export function faviconUrl(host: string): string {
  return `https://icons.duckduckgo.com/ip3/${host}.ico`;
}
