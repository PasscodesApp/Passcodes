export interface PasscodesAutofillServiceModuleInterface {
  isAutofillServiceEnabled(): boolean;
  openAutofillSettings(): void;

  /** Whether keyboard-strip inline suggestions are offered during fill (default: true). */
  isInlineSuggestionsEnabled(): Promise<boolean>;
  setInlineSuggestionsEnabled(enabled: boolean): Promise<void>;
}
