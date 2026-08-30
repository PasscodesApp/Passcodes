import { requireNativeModule } from "expo-modules-core";
import { PasscodesAutofillServiceModuleInterface } from "./PasscodesAutofillService.types";

// The string 'PasscodesAutofillService' MUST match the Name("...") string defined in your Kotlin Module file.
export default requireNativeModule<PasscodesAutofillServiceModuleInterface>(
  "PasscodesAutofillService",
);
