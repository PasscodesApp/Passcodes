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

  async isInlineSuggestionsEnabled(): Promise<boolean> {
    return true; // Not applicable outside Android native builds.
  },
  async setInlineSuggestionsEnabled(): Promise<void> {
    console.warn(
      "Inline suggestions are only configurable on Android native builds.",
    );
  },
};

export default PasscodesAutofillServiceWeb;
