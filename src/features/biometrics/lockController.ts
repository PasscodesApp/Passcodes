/**
 * The App Lock state machine: when the app is locked, when it re-locks after
 * being in the background, and how it unlocks.
 *
 * Pure logic, no React Native / Expo imports: the clock, the setting, the
 * authenticator and the app-state events are passed in, so it runs in plain
 * Node tests. State lives OUTSIDE React (read it with `useSyncExternalStore`),
 * so it can be driven from anywhere without effects that set state.
 */

import { isAuthenticated, type AuthOutcome, type Authenticator } from "./authentication";

export interface LockState {
  locked: boolean;
  /** A prompt is currently showing. */
  authenticating: boolean;
  /** Result of the latest unlock attempt, `null` until there is one. */
  lastOutcome: AuthOutcome | null;
}

export type AppStateName =
  | "active"
  | "inactive"
  | "background"
  | "unknown"
  | "extension";

export interface AppLockDeps {
  authenticator: Authenticator;
  /** Is the App Lock switch on? Read every time, so changes apply at once. */
  isEnabled(): boolean;
  now(): number;
  /** Time spent away from the app after which it locks again. */
  timeoutMs: number;
  unlockReason: string;
  initialAppState?: AppStateName;
}

export interface AppLock {
  getState(): LockState;
  subscribe(listener: () => void): () => void;
  /** Shows the unlock prompt. Concurrent calls share one prompt. */
  unlock(): Promise<AuthOutcome>;
  /** Feed every `AppState` change into this. */
  handleAppStateChange(next: AppStateName): void;
}

/** Two minutes, the value the app has always used. */
export const AUTO_LOCK_TIMEOUT_MS = 120_000;

export function createAppLock(deps: AppLockDeps): AppLock {
  let state: LockState = {
    locked: deps.isEnabled(),
    authenticating: false,
    lastOutcome: null,
  };

  const listeners = new Set<() => void>();
  let inFlight: Promise<AuthOutcome> | null = null;
  let previousAppState: AppStateName = deps.initialAppState ?? "active";
  let leftAt: number | null = null;

  function setState(patch: Partial<LockState>) {
    state = { ...state, ...patch };
    listeners.forEach((listener) => listener());
  }

  function unlock(): Promise<AuthOutcome> {
    if (!state.locked) {
      return Promise.resolve({ status: "not-required" });
    }

    if (inFlight !== null) {
      return inFlight;
    }

    setState({ authenticating: true });

    inFlight = deps.authenticator
      .authenticate(deps.unlockReason)
      .catch((): AuthOutcome => ({ status: "error" }))
      .then((outcome) => {
        inFlight = null;

        setState(
          isAuthenticated(outcome)
            ? { locked: false, authenticating: false, lastOutcome: outcome }
            : { authenticating: false, lastOutcome: outcome },
        );

        return outcome;
      });

    return inFlight;
  }

  function handleAppStateChange(next: AppStateName) {
    const previous = previousAppState;
    previousAppState = next;

    if (previous === "active" && next !== "active") {
      leftAt = deps.now();
      return;
    }

    if (previous !== "active" && next === "active") {
      const awayMs = leftAt === null ? 0 : deps.now() - leftAt;
      leftAt = null;

      // Only an enabled lock re-locks. (Short trips away, like the system
      // biometric prompt itself, never do.)
      if (!state.locked && deps.isEnabled() && awayMs > deps.timeoutMs) {
        setState({ locked: true, lastOutcome: null });
        void unlock();
      }
    }
  }

  return {
    getState: () => state,
    subscribe(listener) {
      listeners.add(listener);

      return () => {
        listeners.delete(listener);
      };
    },
    unlock,
    handleAppStateChange,
  };
}
