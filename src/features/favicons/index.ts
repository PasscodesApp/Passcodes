/**
 * Website icons ("favicons") for saved passwords.
 *
 * - `Favicon`: the icon (with placeholder) for a website host
 * - `getFaviconHost`: which saved addresses get an icon
 * - providers + setting: where icons come from (Settings > Website icons)
 * - cache: icons are downloaded once and kept on the device
 *
 * Everything else in the app imports from here only.
 */
export { default as Favicon } from "./Favicon";
export { default as FaviconProviderDialog } from "./FaviconProviderDialog";
export {
  FAVICON_PROVIDERS,
  getFaviconHost,
  getFaviconProviderLabel,
  type FaviconProvider,
} from "./providers";
export { useFaviconProvider } from "./setting";
