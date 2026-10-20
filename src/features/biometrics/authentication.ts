/**
 * Device authentication (biometrics, falling back to the device PIN, pattern
 * or password).
 *
 * Pure logic, no React Native / Expo imports: the platform is reached through
 * an `AuthBackend`, so everything here runs in plain Node tests.
 *
 * Design rules:
 * - `authenticate()` NEVER throws. Every problem is an `AuthOutcome`.
 * - It fails closed: anything unexpected is a refusal, never a success.
 * - The one deliberate exception is a device with no screen lock at all
 *   (`not-required`): there is nothing to ask, and refusing would lock the
 *   user out of their own passwords with no way back in.
 * - Overlapping calls share one prompt instead of stacking prompts.
 */

export type AuthOutcome =
  /** The user proved who they are. */
  | { status: "success" }
  /** The device has no screen lock / biometrics, so there is nothing to ask. */
  | { status: "not-required" }
  /** The user closed the prompt. Not an error. */
  | { status: "cancelled" }
  /** Too many failed attempts; the system blocked the prompt for a while. */
  | { status: "lockout" }
  /** The check ran and the user did not pass it. */
  | { status: "failed" }
  /** The check could not run (platform error). Treated as a refusal. */
  | { status: "error" };

export type PromptResult =
  | { success: true }
  | { success: false; error: string };

/** What the platform has to provide. */
export interface AuthBackend {
  /** `true` if the device has a PIN, pattern, password or biometrics set up. */
  hasDeviceSecurity(): Promise<boolean>;
  /** Shows the system prompt. May throw; the authenticator handles that. */
  prompt(message: string): Promise<PromptResult>;
}

export interface Authenticator {
  authenticate(reason: string): Promise<AuthOutcome>;
}

/** Platform error codes that just mean "the user backed out". */
const CANCELLATION_ERRORS = new Set([
  "user_cancel",
  "system_cancel",
  "app_cancel",
  "user_fallback",
]);

const LOCKOUT_ERRORS = new Set(["lockout"]);

/** `true` when the outcome allows the protected action to continue. */
export function isAuthenticated(outcome: AuthOutcome): boolean {
  return outcome.status === "success" || outcome.status === "not-required";
}

/**
 * Text for the user when `outcome` blocked something, or `null` when there is
 * nothing to say (success, or the user cancelled on purpose).
 */
export function describeAuthFailure(
  outcome: AuthOutcome | null | undefined,
): string | null {
  switch (outcome?.status) {
    case "lockout":
      return "Too many failed attempts. Wait a moment and try again.";
    case "failed":
      return "Authentication failed. Please try again.";
    case "error":
      return "Authentication isn't available right now. Please try again.";
    default:
      return null;
  }
}

export function createAuthenticator(backend: AuthBackend): Authenticator {
  let inFlight: Promise<AuthOutcome> | null = null;

  async function run(reason: string): Promise<AuthOutcome> {
    try {
      let secured = true;

      try {
        secured = await backend.hasDeviceSecurity();
      } catch {
        // Unknown: ask anyway. A missing answer must not turn into "open".
        secured = true;
      }

      if (!secured) {
        return { status: "not-required" };
      }

      const result = await backend.prompt(reason);

      if (result.success) {
        return { status: "success" };
      }

      if (CANCELLATION_ERRORS.has(result.error)) {
        return { status: "cancelled" };
      }

      if (LOCKOUT_ERRORS.has(result.error)) {
        return { status: "lockout" };
      }

      return { status: "failed" };
    } catch {
      return { status: "error" };
    }
  }

  return {
    authenticate(reason: string): Promise<AuthOutcome> {
      if (inFlight === null) {
        inFlight = run(reason).finally(() => {
          inFlight = null;
        });
      }

      return inFlight;
    },
  };
}
