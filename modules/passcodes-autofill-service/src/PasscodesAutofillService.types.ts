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

  /**
   * The personal details used for form filling, or an empty record.
   * Stored in a native DataStore — NOT in the Drizzle database — and
   * therefore not part of any backup, import or export.
   *
   * These are entered and edited only on the native Autofill settings
   * screen (`openAutofillSettings()`) — there is no write method here.
   * This getter exists only if the app wants to read them back, e.g. to
   * show a "details configured" badge.
   */
  getAutofillInformation(): Promise<AutofillInformation>;

  /** Deletes all stored personal details. */
  clearAutofillInformation(): Promise<AutofillInformation>;
}

export type AutofillEmail = {
  label?: string;
  address: string;
  isDefault?: boolean;
};

export type AutofillPhone = {
  label?: string;
  /** Dialing code digits only, without "+": "91". */
  countryCode?: string;
  /** National number, digits only. */
  number: string;
  isDefault?: boolean;
};

export type AutofillAddress = {
  street?: string;
  street2?: string;
  city?: string;
  region?: string;
  postalCode?: string;
  country?: string;
  /** Optional one-line version; composed from the parts when empty. */
  full?: string;
};

export type AutofillInformation = {
  firstName?: string;
  lastName?: string;
  fullName?: string;
  /** ISO date "yyyy-MM-dd". */
  birthDate?: string;
  emails?: AutofillEmail[];
  phones?: AutofillPhone[];
  address?: AutofillAddress;
};

export type FillMode = "smart" | "auto" | "dumb";
