import { ConfigPlugin, withAndroidManifest } from "@expo/config-plugins";

const withHasFragileUserData: ConfigPlugin = (config) => {
  return withAndroidManifest(config, async (modContext) => {
    const androidManifest = modContext.modResults.manifest;

    // Ensure the <application> tag array exists and has at least one entry
    if (androidManifest.application && androidManifest.application.length > 0) {
      const mainApplication = androidManifest.application[0];

      // Inject the attribute safely into the attributes object ($)
      mainApplication.$ = {
        ...mainApplication.$,
        "android:hasFragileUserData": "true",
      };
    }

    return modContext;
  });
};

export default withHasFragileUserData;
