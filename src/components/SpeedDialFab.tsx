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
import { FAB, Surface } from "react-native-paper";

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
    <View style={styles.item}>
      <Surface elevation={2} borderRadius={8} style={styles.label}>
        <Text>{action.label}</Text>
      </Surface>

      <FAB
        size="small"
        aria-label={action.label}
        icon={renderActionIcon}
        onPress={handlePress}
      />
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
});

export default memo(SpeedDialFab);
