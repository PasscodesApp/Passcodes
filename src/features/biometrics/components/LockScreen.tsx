import ScreenHeading from "@/components/ScreenHeading";
import Text from "@/components/Text";
import { Button } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

interface LockScreenProps {
  onUnlock: () => void;
  /** A prompt is showing; the button is disabled so taps can't stack prompts. */
  busy: boolean;
  /** Why the last attempt failed, if it did. */
  message: string | null;
}

/**
 * Shown instead of the app while it is locked. It renders before the theme
 * providers, so it only uses components that work without them.
 */
export default function LockScreen({ onUnlock, busy, message }: LockScreenProps) {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        gap: 10,
        paddingHorizontal: 24,
      }}
    >
      <ScreenHeading title="App Locked!!" style={{ marginBlock: 12 }} />

      {message ? (
        <Text
          accessibilityLiveRegion="polite"
          style={{ textAlign: "center" }}
        >
          {message}
        </Text>
      ) : null}

      <Button title="Unlock" onPress={onUnlock} disabled={busy} />
    </SafeAreaView>
  );
}
