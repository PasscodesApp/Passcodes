import { PasscodesNativeDatabaseModuleInterface } from "./PasscodesNativeDatabase.types";

const PasscodesNativeDatabaseWeb: PasscodesNativeDatabaseModuleInterface = {
  async getPasswordCount(): Promise<number> {
    return 0; // Native database access only exists on Android builds.
  },
};

export default PasscodesNativeDatabaseWeb;
