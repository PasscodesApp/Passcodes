import "tsx/cjs";

import { ConfigContext, ExpoConfig } from "expo/config";

import constants from "@/libs/constants";
import buildPropertiesPlugin from "expo-build-properties/plugin";
import devBuildPlugin from "expo-dev-client/plugin";
import localAuthenticationPlugin from "expo-local-authentication/plugin";
import routerPlugin from "expo-router/plugin";
import expoScreenOrientationPlugin from "expo-screen-orientation/plugin";
import sharingPlugin from "expo-sharing/plugin";
import splashScreenPlugin from "expo-splash-screen/plugin";
import sqlitePlugin from "expo-sqlite/plugin";
import statusBarPlugin from "expo-status-bar/plugin";

const IS_DEV_BUILD = process.env.APP_VARIANT === "development";
const IS_PREVIEW_BUILD = process.env.APP_VARIANT === "preview";
const IS_PRODUCTION_BUILD = process.env.APP_VARIANT === "production";

type AdaptiveLauncherAppIcon = {
  backgroundColor: string;
  foregroundImage: string;
  backgroundImage: string;
  monochromeImage: string;
};

type APK_ABIS = "armeabi-v7a" | "arm64-v8a" | "x86" | "x86_64";

const UNIVERSAL_ABIS: APK_ABIS[] = [
  "armeabi-v7a",
  "arm64-v8a",
  "x86",
  "x86_64",
];

let appNameSuffix = "";
let packageNameSuffix = "";
let versionNameSuffix = "";
let launcherAppIcon = "./assets/images/passcodes-icon.png";
let adaptiveLauncherAppIcon: AdaptiveLauncherAppIcon | undefined = {
  backgroundColor: "#34597f",
  foregroundImage: "./assets/images/android-icon-launcher-foreground.png",
  backgroundImage: "./assets/images/android-icon-launcher-background.png",
  monochromeImage: "./assets/images/android-icon-launcher-monochrome.png",
};

let buildAPKABIS: APK_ABIS[] = process.env.ANDROID_ABIS
  ? (process.env.ANDROID_ABIS.split(",") as APK_ABIS[])
  : UNIVERSAL_ABIS;

if (IS_DEV_BUILD) {
  appNameSuffix = " Dev";
  packageNameSuffix = ".dev";
  versionNameSuffix = "-Dev";
  launcherAppIcon = "./assets/images/passcodes-dev-icon.png";
} else if (IS_PREVIEW_BUILD) {
  appNameSuffix = " Preview";
  packageNameSuffix = ".preview";
  versionNameSuffix = "-Preview";
  launcherAppIcon = "./assets/images/passcodes-preview-icon.png";
}

if (!IS_PRODUCTION_BUILD) {
  adaptiveLauncherAppIcon = undefined;
  buildAPKABIS = process.env.ANDROID_ABIS
    ? (process.env.ANDROID_ABIS.split(",") as APK_ABIS[])
    : ["arm64-v8a", "x86_64"];
}

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: constants.appname + appNameSuffix,
  slug: "passcodes",
  version: constants.build.version + versionNameSuffix,

  orientation: "default",
  icon: launcherAppIcon,
  scheme: "passcodes",
  userInterfaceStyle: "automatic",
  githubUrl: "https://github.com/PasscodesApp/Passcodes",
  platforms: ["android", "ios"],

  ios: {
    buildNumber: constants.build.versionCodeIos,
    version: constants.build.version,
    icon: launcherAppIcon,
    bundleIdentifier:
      "com.jeeldobariya.passcodes.earlybeta" + packageNameSuffix,
    supportsTablet: true,
    infoPlist: {
      ITSAppUsesNonExemptEncryption: false,
    },
  },

  android: {
    versionCode: constants.build.versionCodeAndroid,
    version: constants.build.version + versionNameSuffix,
    package: "com.jeeldobariya.passcodes" + packageNameSuffix,
    icon: launcherAppIcon,
    adaptiveIcon: {
      backgroundColor: adaptiveLauncherAppIcon?.backgroundColor,
      foregroundImage: adaptiveLauncherAppIcon?.foregroundImage,
      backgroundImage: adaptiveLauncherAppIcon?.backgroundImage,
      monochromeImage: adaptiveLauncherAppIcon?.monochromeImage,
    },
    predictiveBackGestureEnabled: false,
  },

  web: {
    output: "static",
    favicon: launcherAppIcon,
  },

  plugins: [
    routerPlugin(),
    splashScreenPlugin({
      image: launcherAppIcon,
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

        // NOTE: buildArchs: APK_ABIS[] = ["armeabi-v7a", "arm64-v8a", "x86", "x86_64"],
        buildArchs: buildAPKABIS,
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
    "@react-native-vector-icons/fontawesome6",
    "./plugins/withProtectUserData.ts",
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
