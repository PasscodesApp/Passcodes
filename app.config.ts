import "tsx/cjs";

import { ConfigContext, ExpoConfig } from "expo/config";

import constants from "@/libs/constants";
import buildPropertiesPlugin from "expo-build-properties/plugin";
import devBuildPlugin from "expo-dev-client/plugin";
import localAuthenticationPlugin from "expo-local-authentication/plugin";
import routerPlugin from "expo-router/plugin";
import sharingPlugin from "expo-sharing/plugin";
import splashScreenPlugin from "expo-splash-screen/plugin";
import sqlitePlugin from "expo-sqlite/plugin";
import statusBarPlugin from "expo-status-bar/plugin";

const IS_DEV = process.env.APP_VARIANT === "development";
const IS_PREVIEW = process.env.APP_VARIANT === "preview";

let appNameSuffix = "";
let packageNameSuffix = "";
let versionNameSuffix = "";
let launcherAppIcon = "./assets/images/android-icon-launcher.png";

if (IS_DEV) {
  appNameSuffix = " Dev";
  packageNameSuffix = ".dev";
  versionNameSuffix = "-Dev";
  launcherAppIcon = "./assets/images/dev-android-icon-launcher.png";
} else if (IS_PREVIEW) {
  appNameSuffix = " Preview";
  packageNameSuffix = ".preview";
  versionNameSuffix = "-Preview";
  launcherAppIcon = "./assets/images/dev-android-icon-launcher.png";
}

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: constants.appname + appNameSuffix,
  slug: "passcodes",
  version: constants.version + versionNameSuffix,

  orientation: "portrait",
  icon: "./assets/images/passcodes_icon.png",
  scheme: "passcodes",
  userInterfaceStyle: "automatic",
  githubUrl: "https://github.com/PasscodesApp/Passcodes",
  platforms: ["android", "ios"],

  ios: {
    buildNumber: "1.0.0",
    version: constants.version,
    icon: launcherAppIcon,
    bundleIdentifier:
      "com.jeeldobariya.passcodes.earlybeta" + packageNameSuffix,
    supportsTablet: true,
    infoPlist: {
      ITSAppUsesNonExemptEncryption: false,
    },
  },

  android: {
    versionCode: 10,
    version: constants.version + versionNameSuffix,
    package: "com.jeeldobariya.passcodes" + packageNameSuffix,
    icon: launcherAppIcon,
    adaptiveIcon:
      IS_DEV || IS_PREVIEW
        ? undefined
        : {
            backgroundColor: "#34597f",
            foregroundImage:
              "./assets/images/android-icon-launcher-foreground.png",
            backgroundImage:
              "./assets/images/android-icon-launcher-background.png",
            monochromeImage:
              "./assets/images/android-icon-launcher-monochrome.png",
          },
    predictiveBackGestureEnabled: true,
  },

  web: {
    output: "static",
    favicon: "./assets/images/passcodes_icon.png",
  },

  plugins: [
    routerPlugin(),
    splashScreenPlugin({
      image: "./assets/images/passcodes_icon.png",
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
        buildArchs: ["armeabi-v7a", "arm64-v8a", "x86", "x86_64"],
      },
    }),
    localAuthenticationPlugin({
      faceIDPermission: "Allow Passcodes to use Face ID.",
    }),
    statusBarPlugin({ style: "dark" }),
    sqlitePlugin(),
    sharingPlugin(),
    "./plugins/withProtectUserData.ts",
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
