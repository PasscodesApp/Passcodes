export {
  describeAuthFailure,
  isAuthenticated,
  type AuthOutcome,
} from "./authentication";
export { default as LockScreen } from "./components/LockScreen";
export { confirmIdentity, isAppLockEnabled, toggleAppLock } from "./runtime";
export { useAppLock } from "./useAppLock";
