import { requireOptionalNativeModule } from "expo";

/** One installed app. */
export type InstalledApplication = {
  /** e.g. "com.instagram.android" */
  packageName: string;
  /** e.g. "Instagram" */
  label: string;
  isSystemApp: boolean;
};

type NativeModule = {
  getInstalledApplications(): Promise<InstalledApplication[]>;
  getApplicationLabel(packageName: string): Promise<string | null>;
  getApplicationIcon(
    packageName: string,
    sizePx: number,
  ): Promise<string | null>;
};

/**
 * `null` everywhere except Android (and in a build made before this module
 * existed), so importing this file never crashes on iOS, web or an old build.
 */
export default requireOptionalNativeModule<NativeModule>(
  "PasscodesInstalledApps",
);
