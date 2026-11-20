import type { ReactNode } from "react";
import Animated, {
  FadeIn,
  FadeOut,
  LinearTransition,
} from "react-native-reanimated";

const DURATION = 220;

/**
 * Animated expand / collapse built on Reanimated layout animations:
 *
 * - the content fades in / out and is really unmounted when collapsed, so a
 *   collapsed section costs nothing to render
 * - the wrapper animates its own height (`LinearTransition`)
 *
 * No manual measuring / timers. Put `layout={LinearTransition}` on the
 * siblings too (see `CategorySection`) so they slide along smoothly.
 *
 * Wrap the list in `<LayoutAnimationConfig skipEntering>` to avoid
 * animating the very first render.
 */
export default function Collapsible({
  expanded,
  children,
}: {
  expanded: boolean;
  children: ReactNode;
}) {
  return (
    <Animated.View
      layout={LinearTransition.duration(DURATION)}
      style={{ overflow: "hidden" }}
    >
      {expanded && (
        <Animated.View
          entering={FadeIn.duration(DURATION)}
          exiting={FadeOut.duration(150)}
        >
          {children}
        </Animated.View>
      )}
    </Animated.View>
  );
}
