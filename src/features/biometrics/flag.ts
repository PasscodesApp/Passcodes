/**
 * A persisted on/off switch.
 *
 * Pure logic: storage is passed in (the app uses `expo-sqlite/kv-store`), so
 * it runs in plain Node tests.
 *
 * - A missing, unreadable or unrecognised stored value means "use the
 *   default". For security features the default is ON.
 * - `set()` never throws and returns the value that is REALLY stored
 *   afterwards, so the UI can't show a switch position that wasn't saved.
 */

export interface FlagStorage {
  getItemSync(key: string): string | null;
  setItemSync(key: string, value: string): void;
}

export interface Flag {
  get(): boolean;
  set(value: boolean): boolean;
  toggle(): boolean;
}

export function createFlag(
  storage: FlagStorage,
  key: string,
  defaultValue: boolean,
): Flag {
  function get(): boolean {
    try {
      const stored = storage.getItemSync(key);

      if (stored === "true") {
        return true;
      }

      if (stored === "false") {
        return false;
      }

      return defaultValue;
    } catch {
      return defaultValue;
    }
  }

  function set(value: boolean): boolean {
    try {
      storage.setItemSync(key, value ? "true" : "false");
    } catch {
      // Not saved: the read below reports the value that is still in effect.
    }

    return get();
  }

  return {
    get,
    set,
    toggle: () => set(!get()),
  };
}
