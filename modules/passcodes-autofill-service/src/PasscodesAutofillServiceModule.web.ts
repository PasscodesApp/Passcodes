import {
  FillMode,
  PasscodesAutofillServiceModuleInterface,
} from "./PasscodesAutofillService.types";

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
    return false; // Not applicable outside Android native builds.
  },

  async setInlineSuggestionsEnabled(enabled: boolean): Promise<boolean> {
    console.warn(
      "Inline suggestions are only configurable on Android native builds.",
    );

    // Not applicable outside Android native builds; return the requested value.
    return enabled;
  },

  async isBiometricLockEnabled(): Promise<boolean> {
    return false; // Not applicable outside Android native builds.
  },

  async setBiometricLockEnabled(enabled: boolean): Promise<boolean> {
    console.warn(
      "The biometric lock is only configurable on Android native builds.",
    );

    // Not applicable outside Android native builds; return the requested value.
    return enabled;
  },

  async getFillMode(): Promise<FillMode> {
    return "auto"; // Not applicable outside Android native builds.
  },

  async setFillMode(mode: FillMode): Promise<FillMode> {
    console.warn(
      "The fill mode is only configurable on Android native builds.",
    );

    // Not applicable outside Android native builds; return the requested value.
    return mode;
  },
};

export default PasscodesAutofillServiceWeb;
