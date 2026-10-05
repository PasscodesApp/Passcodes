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
  FadeIn,
  FadeInDown,
  FadeOut,
  FadeOutDown,
  useAnimatedStyle,
  useSharedValue,
  withTiming,
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

/**
 * Reusable expanding FAB ("speed dial").
 *
 * Built on plain views, NOT Paper's `FAB.Group` (that one renders in a
 * Portal, i.e. above the tab bar). Memoized: as long as `actions` is stable
 * it never re-renders because of its parent.
 */
function SpeedDialFab({
  actions,
  renderIcon,
  accessibilityLabel = "Open actions",
}: SpeedDialFabProps) {
  const [open, setOpen] = useState(false);

  const rotation = useSharedValue(0);

  useEffect(() => {
    rotation.value = withTiming(open ? 1 : 0, { duration: 200 });
  }, [open, rotation]);

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

  const iconStyle = useAnimatedStyle(() => ({
    transform: [{ rotate: `${rotation.value * 45}deg` }],
  }));

  const close = useCallback(() => setOpen(false), []);

  const mainIcon = useCallback(
    ({ size, color }: { size: number; color: string }) => (
      <Animated.View style={iconStyle}>{renderIcon(size, color)}</Animated.View>
    ),
    [iconStyle, renderIcon],
  );

  return (
    <>
      {open && (
        <Animated.View
          entering={FadeIn.duration(150)}
          exiting={FadeOut.duration(150)}
          style={[StyleSheet.absoluteFill, { backgroundColor: "rgba(0,0,0,0.45)" }]}
        >
          <Pressable
            style={StyleSheet.absoluteFill}
            onPress={close}
            accessibilityLabel="Close actions"
          />
        </Animated.View>
      )}

      <View
        pointerEvents="box-none"
        style={{
          position: "absolute",
          bottom: 35,
          right: 35,
          alignItems: "flex-end",
          gap: 16,
        }}
      >
        {open &&
          actions.map((action, index) => (
            <Animated.View
              key={action.key}
              entering={FadeInDown.delay(
                (actions.length - 1 - index) * 40,
              ).duration(160)}
              exiting={FadeOutDown.duration(120)}
              style={{ flexDirection: "row", alignItems: "center", gap: 12 }}
            >
              <Surface
                elevation={2}
                style={{
                  borderRadius: 8,
                  paddingHorizontal: 12,
                  paddingVertical: 6,
                }}
              >
                <Text>{action.label}</Text>
              </Surface>

              <FAB
                size="small"
                accessibilityLabel={action.label}
                icon={({ size, color }) => action.renderIcon(size, color)}
                onPress={() => {
                  close();
                  action.onPress();
                }}
              />
            </Animated.View>
          ))}

        <FAB
          accessibilityLabel={accessibilityLabel}
          accessibilityState={{ expanded: open }}
          icon={mainIcon}
          onPress={() => setOpen((value) => !value)}
        />
      </View>
    </>
  );
}

export default memo(SpeedDialFab);
