import "tsx/cjs";

import { ConfigContext, ExpoConfig } from "expo/config";

import AppConfig from "@/config";
import buildPropertiesPlugin from "expo-build-properties/plugin";
import devBuildPlugin from "expo-dev-client/plugin";
import localAuthenticationPlugin from "expo-local-authentication/plugin";
import routerPlugin from "expo-router/plugin";
import expoScreenOrientationPlugin from "expo-screen-orientation/plugin";
import sharingPlugin from "expo-sharing/plugin";
import splashScreenPlugin from "expo-splash-screen/plugin";
import sqlitePlugin from "expo-sqlite/plugin";
import statusBarPlugin from "expo-status-bar/plugin";
import withProtectUserDataPlugin from "./plugins/withProtectUserData/plugin";

const ENV_APP_VARIANT = process.env["APP_VARIANT"];
const ENV_ANDROID_ABIS = process.env["ANDROID_ABIS"];

/**
 * ---------------------------------------------------------------------------
 * Types
 * ---------------------------------------------------------------------------
 */

type BuildType = "development" | "preview" | "production";

type APK_ABIS = "armeabi-v7a" | "arm64-v8a" | "x86" | "x86_64";

type AdaptiveLauncherAppIcon = {
  backgroundColor: string;
  foregroundImage: string;
  backgroundImage: string;
  monochromeImage: string;
};

type BuildConfig = {
  type: BuildType;

  nameSuffix: string;
  packageNameSuffix: string;
  versionNameSuffix: string;

  icon: string;
  adaptiveIcon: AdaptiveLauncherAppIcon | undefined;

  androidAbis: APK_ABIS[];
};

/**
 * ---------------------------------------------------------------------------
 * Build constants
 * ---------------------------------------------------------------------------
 */

const UNIVERSAL_ABIS: APK_ABIS[] = [
  "armeabi-v7a",
  "arm64-v8a",
  "x86",
  "x86_64",
];

const DEFAULT_NON_PRODUCTION_ABIS: APK_ABIS[] = ["arm64-v8a", "x86_64"];

const DEFAULT_ICON = "./assets/images/passcodes-icon.png";

const ADAPTIVE_ICON: AdaptiveLauncherAppIcon = {
  backgroundColor: "#34597f",
  foregroundImage: "./assets/images/android-icon-launcher-foreground.png",
  backgroundImage: "./assets/images/android-icon-launcher-background.png",
  monochromeImage: "./assets/images/android-icon-launcher-monochrome.png",
};

/**
 * ---------------------------------------------------------------------------
 * Build type & varaints
 * ---------------------------------------------------------------------------
 * Missing or invalid APP_VARIANT intentionally means production.
 */
const BUILD_TYPE: BuildType =
  ENV_APP_VARIANT === "development" ||
  ENV_APP_VARIANT === "preview" ||
  ENV_APP_VARIANT === "production"
    ? ENV_APP_VARIANT
    : "production";

const BUILD_VARAIANTS: APK_ABIS[] | undefined = ENV_ANDROID_ABIS
  ? (ENV_ANDROID_ABIS.split(",") as APK_ABIS[])
  : undefined;

/**
 * ---------------------------------------------------------------------------
 * Build configuration
 * ---------------------------------------------------------------------------
 * This is the single place that describes how each build variant differs.
 */
const BUILD_CONFIGS: Record<BuildType, BuildConfig> = {
  development: {
    type: "development",

    nameSuffix: " Dev",
    packageNameSuffix: ".dev",
    versionNameSuffix: "-Dev",

    icon: "./assets/images/passcodes-dev-icon.png",
    adaptiveIcon: undefined,

    androidAbis: BUILD_VARAIANTS
      ? BUILD_VARAIANTS
      : DEFAULT_NON_PRODUCTION_ABIS,
  },

  preview: {
    type: "preview",

    nameSuffix: " Preview",
    packageNameSuffix: ".preview",
    versionNameSuffix: "-Preview",

    icon: "./assets/images/passcodes-preview-icon.png",
    adaptiveIcon: undefined,

    androidAbis: BUILD_VARAIANTS
      ? BUILD_VARAIANTS
      : DEFAULT_NON_PRODUCTION_ABIS,
  },

  production: {
    type: "production",

    nameSuffix: "",
    packageNameSuffix: "",
    versionNameSuffix: "",

    icon: DEFAULT_ICON,
    adaptiveIcon: ADAPTIVE_ICON,

    androidAbis: BUILD_VARAIANTS ? BUILD_VARAIANTS : UNIVERSAL_ABIS,
  },
};

const build = BUILD_CONFIGS[BUILD_TYPE];

/**
 * ---------------------------------------------------------------------------
 * Application identity
 * ---------------------------------------------------------------------------
 */
const APP_IDENTITY = {
  packageName: "com.jeeldobariya.passcodes",
  iosBundleIdentifier: "com.jeeldobariya.passcodes.earlybeta",

  version: AppConfig.BUILD_VERSION,
  androidVersionCode: AppConfig.BUILD_ANDROID_VERSIONCODE,
  iosBuildNumber: AppConfig.BUILD_IOS_VERSIONCODE,

  slug: "passcodes",
};

/**
 * ---------------------------------------------------------------------------
 * Expo configuration
 * ---------------------------------------------------------------------------
 */

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,

  name: `${AppConfig.APP_NAME}${build.nameSuffix}`,
  slug: APP_IDENTITY.slug,
  version: `${APP_IDENTITY.version}${build.versionNameSuffix}`,

  orientation: "default",
  icon: build.icon,
  scheme: "passcodes",
  userInterfaceStyle: "automatic",

  githubUrl: "https://github.com/PasscodesApp/Passcodes",
  platforms: ["android", "ios"],

  ios: {
    buildNumber: APP_IDENTITY.iosBuildNumber,
    version: APP_IDENTITY.version,
    icon: build.icon,

    bundleIdentifier: `${APP_IDENTITY.iosBundleIdentifier}${build.packageNameSuffix}`,

    supportsTablet: true,

    infoPlist: {
      ITSAppUsesNonExemptEncryption: false,
    },
  },

  android: {
    versionCode: APP_IDENTITY.androidVersionCode,
    version: APP_IDENTITY.version,

    package: `${APP_IDENTITY.packageName}${build.packageNameSuffix}`,

    icon: build.icon,

    adaptiveIcon: build.adaptiveIcon ?? {},

    predictiveBackGestureEnabled: false,
  },

  web: {
    output: "static",
    favicon: build.icon,
  },

  plugins: [
    routerPlugin(),

    splashScreenPlugin({
      image: build.icon,
      imageWidth: 200,
      resizeMode: "contain",
      backgroundColor: "#7eabee",
      dark: {
        backgroundColor: "#34597f",
      },
    }),

    devBuildPlugin({
      launchMode: "launcher",
      addGeneratedScheme: false,
    }),

    buildPropertiesPlugin({
      android: {
        usePrecompiledHeaders: true,
        enableMinifyInReleaseBuilds: true,
        enableShrinkResourcesInReleaseBuilds: true,
        buildArchs: build.androidAbis,
      },
    }),

    localAuthenticationPlugin({
      faceIDPermission: "Allow Passcodes to use Face ID.",
    }),

    statusBarPlugin({ style: "dark" }),

    sqlitePlugin(),
    sharingPlugin(),

    expoScreenOrientationPlugin({
      initialOrientation: "PORTRAIT",
    }),

    withProtectUserDataPlugin({
      allowBackup: false, // TODO: we make it to allow backup when we have encryption.
    }),

    "@react-native-vector-icons/fontawesome6",
  ],

  experiments: {
    typedRoutes: true,
    reactCompiler: true,
  },

  extra: {
    router: {},
    eas: {
      projectId: "960e68d8-0220-4068-8f1f-e141d29dbcef",
    },
  },

  owner: "passcodesapp",
});
