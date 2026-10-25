import { requireNativeModule } from "expo-modules-core";
import { PasscodesNativeDatabaseModuleInterface } from "./PasscodesNativeDatabase.types";

// The string 'PasscodesNativeDatabase' MUST match the Name("...") string defined in the Kotlin Module file.
export default requireNativeModule<PasscodesNativeDatabaseModuleInterface>(
  "PasscodesNativeDatabase",
);
