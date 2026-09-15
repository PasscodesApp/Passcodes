import { StyleSheet, View } from "react-native";
import Text from "./Text";

type StrengthIndicatorProps = {
  /**
   * Normalized password strength score.
   *
   * Must be a value between 0 and 1:
   * - 0 = no strength
   * - 1 = maximum strength
   */
  score: number;

  /**
   * Number of bars used to visually represent the strength.
   *
   * Defaults to 5.
   */
  bars?: number;
};

/**
 * Displays a password strength score as a series of bars.
 *
 * The `score` is expected to be normalized to the range 0..1.
 * The score is mapped proportionally to the configured number
 * of bars. For example, with 5 bars:
 *
 *   0   → 0 active bars
 *   0.2 → 1 active bar
 *   0.5 → 3 active bars
 *   1   → 5 active bars
 *
 * A score between 0 and 1 is rounded up so that any non-zero
 * strength is represented by at least one active bar.
 */
export default function StrengthIndicator({
  score,
  bars = 5,
}: StrengthIndicatorProps) {
  const activeBars = Math.ceil(score * bars);

  return (
    <View style={styles.strengthContainer}>
      <View style={styles.strengthBars}>
        {Array.from({ length: bars }).map((_, index) => (
          <View
            key={index}
            style={[
              styles.strengthBar,
              index < activeBars && styles.strengthBarActive,
            ]}
          />
        ))}
      </View>

      <View style={styles.strengthLabels}>
        <Text variant="labelSmall">Weak </Text>
        <Text variant="labelSmall">Strong </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  strengthContainer: {
    gap: 8,
  },

  strengthBars: {
    flexDirection: "row",
    gap: 5,
  },

  strengthBar: {
    flex: 1,
    height: 7,
    borderRadius: 4,
    backgroundColor: "#ddd",
  },

  strengthBarActive: {
    backgroundColor: "#236636",
  },

  strengthLabels: {
    flexDirection: "row",
    justifyContent: "space-between",
    opacity: 0.6,
  },
});
