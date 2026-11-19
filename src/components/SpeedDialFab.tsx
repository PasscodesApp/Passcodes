import Text from "@/components/Text";
import {
  memo,
  useCallback,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { BackHandler, Pressable, StyleSheet, View } from "react-native";
import { FAB, Surface } from "react-native-paper";
import Animated, {
  Extrapolation,
  interpolate,
  useAnimatedStyle,
  useSharedValue,
  withTiming,
  type SharedValue,
} from "react-native-reanimated";

export type SpeedDialAction = {
  key: string;
  label: string;
  renderIcon: (size: number, color: string) => ReactNode;
  onPress: () => void;
};

type SpeedDialFabProps = {
  actions: SpeedDialAction[];
  /** Icon of the main button (rotates to an "x" while open). */
  renderIcon: (size: number, color: string) => ReactNode;
  accessibilityLabel?: string;
};

const DURATION = 200;

/** Fraction of the animation between two actions appearing. */
const STAGGER = 0.15;

/**
 * Reusable expanding FAB ("speed dial").
 *
 * Built on plain views, NOT Paper's `FAB.Group` (that one renders in a
 * Portal, i.e. above the tab bar). Memoized: as long as `actions` is stable
 * it never re-renders because of its parent.
 *
 * Open / close is driven by ONE shared value (`progress`). Nothing is mounted
 * or unmounted while animating, so there are no Reanimated entering / exiting
 * layout animations. Those were the crash source: pressing an action closed
 * the menu (exit animations started) and navigated away in the same tick, and
 * coming back the animated views no longer existed.
 */
function SpeedDialFab({
  actions,
  renderIcon,
  accessibilityLabel = "Open actions",
}: SpeedDialFabProps) {
  const [open, setOpen] = useState(false);

  // 0 = closed, 1 = open.
  const progress = useSharedValue(0);

  useEffect(() => {
    progress.set(withTiming(open ? 1 : 0, { duration: DURATION }));
  }, [open, progress]);

  // Android back button closes the menu first.
  useEffect(() => {
    if (!open) {
      return;
    }

    const subscription = BackHandler.addEventListener(
      "hardwareBackPress",
      () => {
        setOpen(false);
        return true;
      },
    );

    return () => subscription.remove();
  }, [open]);

  const scrimStyle = useAnimatedStyle(() => ({
    opacity: progress.get(),
  }));

  const iconStyle = useAnimatedStyle(() => ({
    transform: [{ rotate: `${progress.get() * 45}deg` }],
  }));

  const close = useCallback(() => setOpen(false), []);
  const toggle = useCallback(() => setOpen((value) => !value), []);

  const mainIcon = useCallback(
    ({ size, color }: { size: number; color: string }) => (
      <Animated.View style={iconStyle}>{renderIcon(size, color)}</Animated.View>
    ),
    [iconStyle, renderIcon],
  );

  return (
    <>
      <Animated.View
        pointerEvents={open ? "auto" : "none"}
        style={[StyleSheet.absoluteFill, styles.scrim, scrimStyle]}
      >
        <Pressable
          style={StyleSheet.absoluteFill}
          onPress={close}
          disabled={!open}
          accessible={open}
          accessibilityLabel="Close actions"
        />
      </Animated.View>

      <View pointerEvents="box-none" style={styles.container}>
        {actions.map((action, index) => (
          <SpeedDialItem
            key={action.key}
            action={action}
            // The action closest to the main button appears first.
            start={Math.min((actions.length - 1 - index) * STAGGER, 0.6)}
            progress={progress}
            open={open}
            onClose={close}
          />
        ))}

        <FAB
          aria-label={accessibilityLabel}
          aria-expanded={open}
          icon={mainIcon}
          onPress={toggle}
        />
      </View>
    </>
  );
}

type SpeedDialItemProps = {
  action: SpeedDialAction;
  /** Point of `progress` (0..1) where this item starts to appear. */
  start: number;
  progress: SharedValue<number>;
  open: boolean;
  onClose: () => void;
};

const SpeedDialItem = memo(function SpeedDialItem({
  action,
  start,
  progress,
  open,
  onClose,
}: SpeedDialItemProps) {
  const style = useAnimatedStyle(() => {
    const visible = interpolate(
      progress.get(),
      [start, 1],
      [0, 1],
      Extrapolation.CLAMP,
    );

    return {
      opacity: visible,
      transform: [{ translateY: (1 - visible) * 16 }],
    };
  });

  const renderActionIcon = useCallback(
    ({ size, color }: { size: number; color: string }) =>
      action.renderIcon(size, color),
    [action],
  );

  const handlePress = useCallback(() => {
    onClose();
    action.onPress();
  }, [action, onClose]);

  return (
    <Animated.View
      pointerEvents={open ? "auto" : "none"}
      accessibilityElementsHidden={!open}
      importantForAccessibility={open ? "auto" : "no-hide-descendants"}
      style={[styles.item, style]}
    >
      <Surface elevation={2} borderRadius={8} style={styles.label}>
        <Text>{action.label}</Text>
      </Surface>

      <FAB
        size="small"
        aria-label={action.label}
        icon={renderActionIcon}
        onPress={handlePress}
      />
    </Animated.View>
  );
});

const styles = StyleSheet.create({
  scrim: {
    backgroundColor: "rgba(0,0,0,0.45)",
  },
  container: {
    position: "absolute",
    bottom: 35,
    right: 35,
    alignItems: "flex-end",
    gap: 16,
  },
  item: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  label: {
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
});

export default memo(SpeedDialFab);
