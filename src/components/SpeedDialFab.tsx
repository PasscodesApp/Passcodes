import Text from "@/components/Text";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6/static";
import {
  memo,
  useCallback,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { BackHandler, Pressable, StyleSheet, View } from "react-native";
import { FAB, Surface, useTheme } from "react-native-paper";

export type SpeedDialAction = {
  key: string;
  label: string;
  renderIcon: (size: number, color: string) => ReactNode;
  onPress: () => void;
};

type SpeedDialFabProps = {
  actions: SpeedDialAction[];
  /** Icon of the main button while the menu is closed. */
  renderIcon: (size: number, color: string) => ReactNode;
  accessibilityLabel?: string;
};

/**
 * Reusable expanding FAB ("speed dial").
 *
 * Built on plain views, NOT Paper's `FAB.Group` (that one renders in a
 * Portal, i.e. above the tab bar). Memoized: as long as `actions` is stable
 * it never re-renders because of its parent.
 *
 * No animation on purpose (Reanimated was crashing the app): opening shows
 * the actions at once and the main icon is swapped for an "x".
 *
 * The small action buttons are a hand-built `MiniFabButton`, NOT Paper's
 * `<FAB size="small">`: on react-native-paper 6.0.0-alpha.2 that crashes
 * every time with "Cannot read property 'shape' of undefined" inside
 * Paper's internal `Shell` component (a Material 3 shape-morph wrapper).
 * The main (default size) FAB below does not use that code path and is
 * unaffected.
 */
function SpeedDialFab({
  actions,
  renderIcon,
  accessibilityLabel = "Open actions",
}: SpeedDialFabProps) {
  const [open, setOpen] = useState(false);

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

  const close = useCallback(() => setOpen(false), []);
  const toggle = useCallback(() => setOpen((value) => !value), []);

  const mainIcon = useCallback(
    ({ size, color }: { size: number; color: string }) =>
      open ? (
        <FontAwesome6 name="xmark" size={size} color={color} iconStyle="solid" />
      ) : (
        renderIcon(size, color)
      ),
    [open, renderIcon],
  );

  return (
    <>
      {open && (
        <View style={[StyleSheet.absoluteFill, styles.scrim]}>
          <Pressable
            style={StyleSheet.absoluteFill}
            onPress={close}
            accessibilityLabel="Close actions"
          />
        </View>
      )}

      <View pointerEvents="box-none" style={styles.container}>
        {open &&
          actions.map((action) => (
            <SpeedDialItem key={action.key} action={action} onClose={close} />
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
  onClose: () => void;
};

const SpeedDialItem = memo(function SpeedDialItem({
  action,
  onClose,
}: SpeedDialItemProps) {
  const theme = useTheme();

  const handlePress = useCallback(() => {
    onClose();
    action.onPress();
  }, [action, onClose]);

  return (
    <View style={styles.item}>
      <Surface elevation={2} borderRadius={8} style={styles.label}>
        <Text>{action.label}</Text>
      </Surface>

      <Pressable
        onPress={handlePress}
        accessibilityRole="button"
        aria-label={action.label}
        hitSlop={8}
        style={({ pressed }) => [
          styles.miniFab,
          {
            backgroundColor: theme.colors.secondaryContainer,
            elevation: 3,
            opacity: pressed ? 0.85 : 1,
          },
        ]}
      >
        {action.renderIcon(20, theme.colors.onSecondaryContainer)}
      </Pressable>
    </View>
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
  // Material 3 "small FAB" size (40x40, 12dp corner radius), built by hand to
  // avoid Paper's broken `<FAB size="small">` on this alpha version.
  miniFab: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.3,
    shadowRadius: 2,
  },
});

export default memo(SpeedDialFab);
