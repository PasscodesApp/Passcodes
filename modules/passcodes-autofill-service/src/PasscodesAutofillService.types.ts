// Define your exported module types here.
export interface PasscodesAutofillServiceModuleInterface {
  /**
   * Checks whether the user has enabled this app as their primary Autofill Provider in Android system settings.
   */
  isAutofillServiceEnabled(): boolean;

  /**
   * Opens Android Settings directly to the Autofill service selection screen.
   */
  openAutofillSettings(): void;
}
