import NativeModule, { type InstalledApplication } from "./PasscodesInstalledAppsModule";

export type { InstalledApplication };

/** `true` only on Android builds that include this native module. */
export const isInstalledApplicationsAvailable = NativeModule !== null;

/** Installed apps that have a launcher icon, sorted by name. */
export async function getInstalledApplications(): Promise<
  InstalledApplication[]
> {
  return NativeModule ? NativeModule.getInstalledApplications() : [];
}

/** The app's name (e.g. "Instagram"), or `null` if unknown / not visible. */
export async function getApplicationLabel(
  packageName: string,
): Promise<string | null> {
  return NativeModule ? NativeModule.getApplicationLabel(packageName) : null;
}

/** The app's icon as a PNG data URI for `<Image source={{ uri }} />`. */
export async function getApplicationIcon(
  packageName: string,
  sizePx = 96,
): Promise<string | null> {
  return NativeModule
    ? NativeModule.getApplicationIcon(packageName, sizePx)
    : null;
}
