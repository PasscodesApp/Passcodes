import { SAFE_AREA } from "@/libs/safe_area";
import { StyleSheet, View, type ViewProps } from "react-native";
import { SafeAreaView, type Edge } from "react-native-safe-area-context";

interface ScreenProps extends ViewProps {
  /** Override which system edges are kept clear. Defaults to SAFE_AREA.edges. */
  edges?: Edge[];
  /**
   * The screen has no navigation header (e.g. a tab root), so add the
   * SAFE_AREA.headerlessGap breathing room above and below its content.
   */
  headerless?: boolean;
}

/**
 * The single wrapper every screen uses for safe-area handling. Tune the
 * behaviour in src/libs/safe_area.ts, not in individual screens.
 *
 * It only depends on react-native + safe-area-context, so it is safe to use
 * outside the theme providers (lock screen, database loading/error screens).
 *
 * - Outer native `SafeAreaView`: keeps the content clear of the system bars.
 *   It carries the background colour, so the inset areas are painted too.
 * - Inner `View`: takes the caller's layout `style` plus the adjustable extra
 *   spacing, applied as margin so it never fights the screen's own padding.
 */
export default function Screen({
  edges = SAFE_AREA.edges,
  headerless = false,
  style,
  children,
  ...rest
}: ScreenProps) {
  const { backgroundColor, ...contentStyle } = StyleSheet.flatten(style) ?? {};

  const { extra, headerlessGap } = SAFE_AREA;
  const gap = headerless ? headerlessGap : 0;

  return (
    <SafeAreaView edges={edges} style={[styles.fill, { backgroundColor }]}>
      <View
        {...rest}
        style={[
          styles.fill,
          {
            marginTop: extra.top + gap,
            marginBottom: extra.bottom + gap,
            marginLeft: extra.left,
            marginRight: extra.right,
          },
          contentStyle,
        ]}
      >
        {children}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  fill: { flex: 1 },
});
