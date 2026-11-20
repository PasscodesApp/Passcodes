import type { ReactNode } from "react";

/**
 * Expand / collapse without animation: the content is simply not rendered
 * while collapsed, so a collapsed section costs nothing to render.
 *
 * (Reanimated layout animations were crashing the app, so there is no
 * fade / height animation any more.)
 */
export default function Collapsible({
  expanded,
  children,
}: {
  expanded: boolean;
  children: ReactNode;
}) {
  return expanded ? <>{children}</> : null;
}
