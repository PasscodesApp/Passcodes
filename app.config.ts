import { ExpoConfig } from "expo/config";

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

const config: ExpoConfig = {
  name: "Passcodes" + appNameSuffix,
  slug: "passcodes",
  version: "v3.0.0-Alpha" + versionNameSuffix,

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
    versionCode: 8,
    version: "v3.0.0-Alpha" + versionNameSuffix,
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
    "expo-router",
    [
      "expo-splash-screen",
      {
        image: "./assets/images/passcodes_icon.png",
        imageWidth: 200,
        resizeMode: "contain",
        backgroundColor: "#d0e3f7",
        dark: {
          backgroundColor: "#34597f",
        },
      },
    ],
    [
      "expo-build-properties",
      {
        android: {
          enableMinifyInReleaseBuilds: true,
          enableShrinkResourcesInReleaseBuilds: true,
        },
      },
    ],
    "expo-status-bar",
    "expo-sqlite",
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
};

export default config;
