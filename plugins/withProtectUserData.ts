import { ConfigPlugin, withAndroidManifest } from "expo/config-plugins";

const withProtectUserData: ConfigPlugin = (config) => {
  return withAndroidManifest(config, (config) => {
    const mainApplication = config?.modResults?.manifest?.application?.[0];

    if (mainApplication) {
      mainApplication.$["android:hasFragileUserData"] = "true";
      mainApplication.$["android:allowBackup"] = "false";
    }

    return config;
  });
};

export default withProtectUserData;
