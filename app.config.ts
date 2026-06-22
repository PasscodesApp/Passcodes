import { ConfigContext, ExpoConfig } from "expo/config";

import localAuthenticationPlugin from "expo-local-authentication/plugin";
import routerPlugin from "expo-router/plugin";
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
  name: "Passcodes" + appNameSuffix,
  slug: "passcodes",
  version: "v3.1.0-Stable" + versionNameSuffix,

  orientation: "portrait",
  icon: "./assets/images/passcodes_icon.png",
  scheme: "passcodes",
  userInterfaceStyle: "automatic",
  githubUrl: "https://github.com/PasscodesApp/Passcodes",
  platforms: ["android"],

  ios: {
    supportsTablet: true,
  },

  android: {
    versionCode: 9,
    version: "v3.1.0-Stable" + versionNameSuffix,
    package: "com.jeeldobariya.passcodes" + packageNameSuffix,
    icon: launcherAppIcon,
    adaptiveIcon: IS_DEV
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
    predictiveBackGestureEnabled: false,
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
      backgroundColor: "#d0e3f7",
      dark: {
        backgroundColor: "#34597f",
      },
    }),
    [
      "expo-build-properties",
      {
        android: {
          usePrecompiledHeaders: true,
          enableMinifyInReleaseBuilds: true,
          enableShrinkResourcesInReleaseBuilds: true,
        },
      },
    ],
    localAuthenticationPlugin({
      faceIDPermission: "Allow Passcodes to use Face ID.",
    }),
    statusBarPlugin({ style: "dark" }),
    sqlitePlugin(),
    "./plugins/withHasFragileUserData.ts",
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
