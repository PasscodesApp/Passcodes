export interface PasscodesNativeDatabaseModuleInterface {
  /**
   * Number of non-deleted saved passwords, read natively from the same
   * SQLite file the app uses. Resolves to 0 if the database doesn't
   * exist yet. Mainly useful to confirm the native module is linked.
   */
  getPasswordCount(): Promise<number>;
}
