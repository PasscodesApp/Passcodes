export type ProtectUserDataProps = {
  /** Whether to allow Android to back up the app's data. */
  allowBackup: boolean;
};

export default function withProtectUserDataPlugin(
  props: ProtectUserDataProps = { allowBackup: false },
): [string, ProtectUserDataProps] {
  return ["@plugins/withProtectUserData", props];
}
