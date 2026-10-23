export interface PasscodesAutofillServiceModuleInterface {
  /**
   * Checks whether the user has enabled this app as their primary Autofill Provider in Android system settings.
   */
  isAutofillServiceEnabled(): boolean;

  /**
   * Opens Android Settings directly to the Autofill service selection screen.
   */
  openAutofillSettings(): void;

  /**
   * Whether keyboard-strip inline Autofill suggestions are offered
   * during a fill request. Defaults to true. Shared with the native
   * Autofill service and the in-app Autofill settings screen — all
   * three read/write the same stored value.
   */
  isInlineSuggestionsEnabled(): Promise<boolean>;

  /**
   * Updates whether keyboard-strip inline Autofill suggestions are offered.
   * Resolves to the value read back after the update has completed.
   * Shared with the native Autofill service and the in-app Autofill
   * settings screen — all three read/write the same stored value.
   */
  setInlineSuggestionsEnabled(enabled: boolean): Promise<boolean>;

  /**
   * Whether a biometric/device-credential prompt is required before a
   * saved credential is filled in. Defaults to false (opt-in). If the device has
   * no biometric or PIN/pattern/password configured, the gate is
   * skipped automatically regardless of this setting.
   */
  isBiometricLockEnabled(): Promise<boolean>;

  /**
   * Enables or disables the biometric lock before filling saved credentials.
   * Resolves after the preference has been updated.
   */
  setBiometricLockEnabled(enabled: boolean): Promise<boolean>;

  /**
   * Which saved credentials the fill picker offers:
   *  - "smart": only credentials matching the site/app; nothing if none match.
   *  - "auto" (default): matching credentials first, else everything.
   *  - "dumb": always everything (capped to the most recent entries).
   * Unknown values are treated as "auto".
   */
  getFillMode(): Promise<FillMode>;

  /**
   * Updates the fill mode. Resolves to the value read back after the update.
   */
  setFillMode(mode: FillMode): Promise<FillMode>;
}

export type FillMode = "smart" | "auto" | "dumb";
