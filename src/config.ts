export type SocialLinks = `https://${string}` | `mailto:${string}`;

const config = {
  // ----------------------
  // Main Config
  // ----------------------
  APP_NAME: "Passcodes",
  VERSION: "v4.0.0 - RC.1",

  // ----------------------
  // Build Config
  // ----------------------
  BUILD_VERSION: "v4.0.0-RC.1", // same as VERSION, just without spaces.
  BUILD_ANDROID_VERSIONCODE: 10,
  BUILD_IOS_VERSIONCODE: "1.0.0",

  // ----------------------
  // Social Config
  // ----------------------
  SOCIAL_EMAIL_US: "mailto:jeeldobariya38@gmail.com" as SocialLinks,
  SOCIAL_GITHUB: "https://github.com/PasscodesApp/Passcodes" as SocialLinks,
  SOCIAL_TELEGRAM: "https://t.me/passcodescommunity" as SocialLinks,
  SOCIAL_DISCORD: "https://discord.gg/kSSkYq7KAQ" as SocialLinks,

  // ----------------------
  // Database Config
  // ----------------------
  DATABASE_NAME: "master.db",
  OLD_MISTAKEN_DATABASE_NAME: "test2.db", // This variable should never be editted in anycases...
};

export type AppConfig = typeof config;
export default config;
