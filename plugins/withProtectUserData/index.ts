import { ConfigPlugin, withAndroidManifest } from "expo/config-plugins";
import { ProtectUserDataProps } from "./plugin";

const withProtectUserDataPlugin: ConfigPlugin<ProtectUserDataProps> = (
  config,
  props,
) => {
  return withAndroidManifest(config, (config) => {
    const mainApplication = config?.modResults?.manifest?.application?.[0];

    if (mainApplication) {
      mainApplication.$["android:hasFragileUserData"] = "true";
      mainApplication.$["android:allowBackup"] = props.allowBackup
        ? "true"
        : "false";
    }

    return config;
  });
};

export default withProtectUserDataPlugin;
