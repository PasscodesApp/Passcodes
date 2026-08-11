import { PasscodesAutofillServiceModuleInterface } from "./PasscodesAutofillService.types";

const PasscodesAutofillServiceWeb: PasscodesAutofillServiceModuleInterface = {
  isAutofillServiceEnabled(): boolean {
    return false; // Web/iOS default fallback
  },
  openAutofillSettings(): void {
    console.warn(
      "Autofill settings are only available on Android native builds.",
    );
  },
};

export default PasscodesAutofillServiceWeb;
