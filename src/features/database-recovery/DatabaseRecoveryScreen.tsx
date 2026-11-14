import Config from "@/config";
import { exportDatabase } from "@/db/exportDatabase";
import {
  confirmIdentity,
  describeAuthFailure,
  isAuthenticated,
} from "@/features/biometrics";
import { useCallback, useState } from "react";
import {
  Linking,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

/**
 * Shown instead of the app whenever Passcodes can't get its saved data ready.
 *
 * Everything here is deliberately plain-language and read-only: nothing on
 * this screen deletes, resets or changes the saved data.
 *
 * It uses no app-wide providers (theme, toasts, repositories), because it is
 * rendered before any of them exist.
 */

type Props = {
  databaseName: string;
  error: unknown;
  onRetry: () => void;
};

type ExportState =
  | { phase: "idle" }
  | { phase: "working" }
  | { phase: "done"; message: string }
  | { phase: "error"; message: string };

const palettes = {
  dark: {
    background: "#020221",
    card: "#0C0C33",
    border: "#23234F",
    text: "#EAF1FF",
    muted: "#A9B7D6",
    primary: "#7EABEE",
    onPrimary: "#020221",
    dangerBg: "#351B25",
    danger: "#EF9F9F",
    warnBg: "#3A2F12",
    warn: "#F2D184",
    success: "#9BE3B0",
    scrim: "rgba(0,0,0,0.65)",
  },
  light: {
    background: "#F2F6FD",
    card: "#FFFFFF",
    border: "#D5DEEE",
    text: "#0B1B33",
    muted: "#4D5F80",
    primary: "#34597F",
    onPrimary: "#FFFFFF",
    dangerBg: "#FBE7EA",
    danger: "#A62B3F",
    warnBg: "#FFF3D6",
    warn: "#7A5600",
    success: "#1C7A3A",
    scrim: "rgba(10,20,40,0.5)",
  },
} as const;

type Palette = (typeof palettes)[keyof typeof palettes];

const TELEGRAM_BLUE = "#229ED9";
const DISCORD_BLURPLE = "#5865F2";

function describeError(error: unknown): string {
  const text = error instanceof Error ? error.message : String(error);

  return text.length > 400 ? `${text.slice(0, 400)}…` : text;
}

export default function DatabaseRecoveryScreen({
  databaseName,
  error,
  onRetry,
}: Props) {
  const colors = palettes[useColorScheme() === "light" ? "light" : "dark"];

  const [warningOpen, setWarningOpen] = useState(false);
  const [showDetails, setShowDetails] = useState(false);
  const [exportState, setExportState] = useState<ExportState>({
    phase: "idle",
  });
  const [linkProblem, setLinkProblem] = useState(false);

  const busy = exportState.phase === "working";

  const runExport = useCallback(async () => {
    setWarningOpen(false);
    setExportState({ phase: "working" });

    try {
      // The app lock must not be bypassable through this screen.
      const outcome = await confirmIdentity(
        "Confirm it's you to export your Passcodes data",
      );

      if (!isAuthenticated(outcome)) {
        const message = describeAuthFailure(outcome);

        setExportState(
          message ? { phase: "error", message } : { phase: "idle" },
        );

        return;
      }

      const result = await exportDatabase(databaseName);

      if (result.status === "cancelled") {
        setExportState({ phase: "idle" });
        return;
      }

      setExportState({
        phase: "done",
        message: result.includesWalFile
          ? "Your data was saved as two files. Please send both files to support, and keep them private."
          : "Your data was saved. Keep the file somewhere safe and private.",
      });
    } catch (caught) {
      console.error("Database export failed:", caught);

      setExportState({
        phase: "error",
        message:
          "We couldn't export your data this time. Nothing was changed. Please try again, or ask for help below.",
      });
    }
  }, [databaseName]);

  const openLink = useCallback(async (url: string) => {
    setLinkProblem(false);

    try {
      await Linking.openURL(url);
    } catch {
      setLinkProblem(true);
    }
  }, []);

  return (
    <SafeAreaView style={[styles.screen, { backgroundColor: colors.background }]}>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        <View
          style={[
            styles.card,
            { backgroundColor: colors.card, borderColor: colors.border },
          ]}
        >
          <View style={[styles.badge, { backgroundColor: colors.dangerBg }]}>
            <Text style={[styles.badgeText, { color: colors.danger }]}>!</Text>
          </View>

          <Text
            accessibilityRole="header"
            style={[styles.title, { color: colors.text }]}
          >
            We couldn&apos;t open your passwords
          </Text>

          <Text style={[styles.body, { color: colors.muted }]}>
            Passcodes ran into a problem while getting your saved passwords
            ready. Your data has not been deleted or changed. You can try
            again, or send a copy of your data to our support team so they can
            help you get everything back.
          </Text>

          <ActionButton
            colors={colors}
            label="Try again"
            variant="primary"
            disabled={busy}
            onPress={onRetry}
          />

          <ActionButton
            colors={colors}
            label={busy ? "Preparing your data…" : "Export my data"}
            variant="outline"
            disabled={busy}
            onPress={() => setWarningOpen(true)}
          />

          {exportState.phase === "done" ? (
            <Text
              accessibilityLiveRegion="polite"
              style={[styles.status, { color: colors.success }]}
            >
              {exportState.message}
            </Text>
          ) : null}

          {exportState.phase === "error" ? (
            <Text
              accessibilityLiveRegion="polite"
              style={[styles.status, { color: colors.danger }]}
            >
              {exportState.message}
            </Text>
          ) : null}

          <View style={[styles.divider, { backgroundColor: colors.border }]} />

          <Text style={[styles.sectionTitle, { color: colors.text }]}>
            Need help?
          </Text>

          <Text style={[styles.small, { color: colors.muted }]}>
            Our community and support team can help. Please don&apos;t post
            your exported file in public chats.
          </Text>

          <View style={styles.row}>
            <ActionButton
              colors={colors}
              label="Telegram"
              variant="brand"
              brandColor={TELEGRAM_BLUE}
              onPress={() => openLink(Config.SOCIAL_TELEGRAM)}
            />
            <ActionButton
              colors={colors}
              label="Discord"
              variant="brand"
              brandColor={DISCORD_BLURPLE}
              onPress={() => openLink(Config.SOCIAL_DISCORD)}
            />
          </View>

          {linkProblem ? (
            <Text style={[styles.small, { color: colors.danger }]}>
              We couldn&apos;t open the link. You can find us at{" "}
              {Config.SOCIAL_TELEGRAM} or {Config.SOCIAL_DISCORD}.
            </Text>
          ) : null}

          <Pressable
            accessibilityRole="button"
            onPress={() => setShowDetails((open) => !open)}
            style={styles.detailsToggle}
          >
            <Text style={[styles.small, { color: colors.primary }]}>
              {showDetails ? "Hide details for support" : "Show details for support"}
            </Text>
          </Pressable>

          {showDetails ? (
            <Text
              selectable
              style={[
                styles.details,
                { color: colors.muted, borderColor: colors.border },
              ]}
            >
              {describeError(error)}
            </Text>
          ) : null}
        </View>

        <Text style={[styles.footer, { color: colors.muted }]}>
          Nothing on this screen deletes or changes your saved data.
        </Text>
      </ScrollView>

      <Modal
        visible={warningOpen}
        transparent
        animationType="fade"
        statusBarTranslucent
        onRequestClose={() => setWarningOpen(false)}
      >
        <View style={[styles.scrim, { backgroundColor: colors.scrim }]}>
          <View
            accessibilityViewIsModal
            style={[
              styles.dialog,
              { backgroundColor: colors.card, borderColor: colors.border },
            ]}
          >
            <Text style={[styles.dialogTitle, { color: colors.text }]}>
              Before you export
            </Text>

            <View style={[styles.warning, { backgroundColor: colors.warnBg }]}>
              <Text style={[styles.warningText, { color: colors.warn }]}>
                The exported file contains ALL of your saved passwords and other
                private information, and anyone who gets it can read them.
              </Text>
            </View>

            <Text style={[styles.body, { color: colors.muted }]}>
              Only share it with the official Passcodes support team, in a
              private message. Never post it in a public group, and delete your
              copy when you no longer need it.
            </Text>

            <ActionButton
              colors={colors}
              label="I understand, export"
              variant="primary"
              onPress={runExport}
            />
            <ActionButton
              colors={colors}
              label="Cancel"
              variant="outline"
              onPress={() => setWarningOpen(false)}
            />
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

type ActionButtonProps = {
  colors: Palette;
  label: string;
  onPress: () => void;
  variant: "primary" | "outline" | "brand";
  brandColor?: string;
  disabled?: boolean;
};

function ActionButton({
  colors,
  label,
  onPress,
  variant,
  brandColor,
  disabled = false,
}: ActionButtonProps) {
  const background =
    variant === "primary"
      ? colors.primary
      : variant === "brand"
        ? (brandColor ?? colors.primary)
        : "transparent";

  const textColor =
    variant === "primary"
      ? colors.onPrimary
      : variant === "brand"
        ? "#FFFFFF"
        : colors.primary;

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={label}
      accessibilityState={{ disabled }}
      disabled={disabled}
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        {
          backgroundColor: background,
          borderColor: variant === "outline" ? colors.primary : background,
          opacity: disabled ? 0.5 : pressed ? 0.85 : 1,
        },
        variant === "brand" && styles.brandButton,
      ]}
    >
      <Text style={[styles.buttonText, { color: textColor }]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  scrollContent: {
    flexGrow: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 20,
    gap: 16,
  },
  card: {
    width: "100%",
    maxWidth: 460,
    borderRadius: 24,
    borderWidth: 1,
    padding: 24,
    gap: 14,
  },
  badge: {
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: "center",
    justifyContent: "center",
    alignSelf: "center",
  },
  badgeText: { fontSize: 26, fontWeight: "800" },
  title: { fontSize: 22, fontWeight: "800", textAlign: "center" },
  body: { fontSize: 15, lineHeight: 22, textAlign: "center" },
  small: { fontSize: 13, lineHeight: 19, textAlign: "center" },
  status: { fontSize: 14, lineHeight: 20, textAlign: "center" },
  sectionTitle: { fontSize: 16, fontWeight: "700", textAlign: "center" },
  divider: { height: StyleSheet.hairlineWidth, marginVertical: 4 },
  row: { flexDirection: "row", gap: 12 },
  button: {
    minHeight: 48,
    borderRadius: 14,
    borderWidth: 1.5,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
  },
  brandButton: { flex: 1 },
  buttonText: { fontSize: 16, fontWeight: "700" },
  detailsToggle: { alignSelf: "center", paddingVertical: 6 },
  details: {
    fontSize: 12,
    lineHeight: 18,
    borderWidth: 1,
    borderRadius: 12,
    padding: 12,
  },
  footer: { fontSize: 12, textAlign: "center", maxWidth: 460 },
  scrim: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 20,
  },
  dialog: {
    width: "100%",
    maxWidth: 440,
    borderRadius: 24,
    borderWidth: 1,
    padding: 22,
    gap: 14,
  },
  dialogTitle: { fontSize: 20, fontWeight: "800", textAlign: "center" },
  warning: { borderRadius: 14, padding: 14 },
  warningText: { fontSize: 15, lineHeight: 22, fontWeight: "600" },
});
