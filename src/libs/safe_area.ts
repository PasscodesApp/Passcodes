import type { Edge } from "react-native-safe-area-context";

/**
 * THE one place to tune safe-area behaviour for the whole app.
 *
 * Every screen renders inside `<Screen>` (src/components/Screen.tsx), which
 * reads these values. Change a number here and every screen follows.
 *
 * How it works: `Screen` uses the native `SafeAreaView`, which measures how
 * much of ITSELF actually overlaps the status bar / notch / gesture bar. So
 * a screen under a navigation header gets no extra top padding (the header
 * already covers the status bar) and a screen above the tab bar gets no extra
 * bottom padding. You never have to say "this screen has a header".
 */
export const SAFE_AREA = {
  /** Which system edges `Screen` keeps clear by default. */
  edges: ["top", "right", "bottom", "left"] as Edge[],

  /**
   * Extra space (dp) added on top of the system inset, on every screen.
   * Increase `top` if content feels too close to the status bar / header,
   * `bottom` if it feels too close to the gesture bar / tab bar.
   */
  extra: { top: 0, right: 0, bottom: 0, left: 0 },

  /**
   * Extra top/bottom space for screens that have NO navigation header
   * (tab roots like Generate and Settings), so their title doesn't sit
   * tight under the status bar.
   */
  headerlessGap: 12,
};
