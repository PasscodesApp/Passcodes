import Text from "@/components/Text";
import { parseApplicationIds } from "@/libs/application_ids";
import { useEffect, useMemo, useState } from "react";
import { Image, Modal, Pressable, View } from "react-native";
import {
  ActivityIndicator,
  Button,
  Checkbox,
  Searchbar,
  useTheme,
} from "react-native-paper";
import { FlashList } from "@shopify/flash-list";

import {
  getApplicationIcon,
  getInstalledApplications,
  type InstalledApplication,
} from "./installedApps";

type Props = {
  /** Text currently in the Application IDs field. */
  currentText: string;
  /** Called with the new IDs (already de-duplicated). */
  onConfirm: (ids: string[]) => void;
  onClose: () => void;
};

function AppIcon({ packageName }: { packageName: string }) {
  const theme = useTheme();
  const [uri, setUri] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    getApplicationIcon(packageName, 96)
      .then((icon) => !cancelled && setUri(icon))
      .catch(() => undefined);

    return () => {
      cancelled = true;
    };
  }, [packageName]);

  return (
    <View
      style={{
        width: 40,
        height: 40,
        borderRadius: 10,
        overflow: "hidden",
        backgroundColor: theme.colors.surfaceVariant,
      }}
    >
      {uri ? (
        <Image source={{ uri }} style={{ width: 40, height: 40 }} />
      ) : null}
    </View>
  );
}

/** Bottom sheet to pick installed apps (Android). Multi-select with search. */
export default function ApplicationPickerModal({
  currentText,
  onConfirm,
  onClose,
}: Props) {
  const theme = useTheme();
  const [apps, setApps] = useState<InstalledApplication[] | null>(null);
  const [failed, setFailed] = useState(false);
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState<string[]>(
    () => parseApplicationIds(currentText).ids,
  );

  useEffect(() => {
    let cancelled = false;

    getInstalledApplications()
      .then((list) => !cancelled && setApps(list))
      .catch(() => !cancelled && setFailed(true));

    return () => {
      cancelled = true;
    };
  }, []);

  const visible = useMemo(() => {
    const needle = query.trim().toLowerCase();

    if (!apps) return [];
    if (!needle) return apps;

    return apps.filter(
      (app) =>
        app.label.toLowerCase().includes(needle) ||
        app.packageName.toLowerCase().includes(needle),
    );
  }, [apps, query]);

  const toggle = (id: string) =>
    setSelected((current) =>
      current.includes(id) ? current.filter((x) => x !== id) : [...current, id],
    );

  return (
    <Modal
      visible
      transparent
      animationType="fade"
      statusBarTranslucent
      onRequestClose={onClose}
    >
      <Pressable
        style={{
          flex: 1,
          backgroundColor: "rgba(0, 0, 0, 0.5)",
          justifyContent: "flex-end",
        }}
        onPress={onClose}
      >
        <Pressable
          onPress={() => {}}
          style={{
            backgroundColor: theme.colors.surface,
            padding: 20,
            gap: 12,
            height: "80%",
            borderTopLeftRadius: 20,
            borderTopRightRadius: 20,
          }}
        >
          <Text variant="titleLarge">Choose apps</Text>

          <Searchbar
            placeholder="Search apps"
            value={query}
            onChangeText={setQuery}
            autoCapitalize="none"
            autoCorrect={false}
          />

          <View style={{ flex: 1 }}>
            {failed ? (
              <Text
                style={{
                  textAlign: "center",
                  fontSize: 12,
                  color: theme.colors.error,
                  marginVertical: 12,
                }}
              >
                Couldn&apos;t read the installed apps. Type the application ID
                instead.
              </Text>
            ) : apps === null ? (
              <ActivityIndicator style={{ margin: 20 }} />
            ) : visible.length === 0 ? (
              <Text
                style={{
                  textAlign: "center",
                  fontSize: 12,
                  marginVertical: 12,
                  color: theme.colors.onSurfaceVariant,
                }}
              >
                No apps found.
              </Text>
            ) : (
              <FlashList
                data={visible}
                extraData={selected}
                keyExtractor={(app) => app.packageName}
                keyboardShouldPersistTaps="handled"
                renderItem={({ item }) => (
                  <Pressable
                    onPress={() => toggle(item.packageName)}
                    style={{
                      flexDirection: "row",
                      alignItems: "center",
                      gap: 12,
                      paddingVertical: 8,
                    }}
                  >
                    <AppIcon packageName={item.packageName} />
                    <View style={{ flex: 1 }}>
                      <Text variant="titleSmall" numberOfLines={1}>
                        {item.label}
                      </Text>
                      <Text
                        variant="bodySmall"
                        numberOfLines={1}
                        style={{ color: theme.colors.onSurfaceVariant }}
                      >
                        {item.packageName}
                      </Text>
                    </View>
                    <Checkbox
                      status={
                        selected.includes(item.packageName)
                          ? "checked"
                          : "unchecked"
                      }
                      onPress={() => toggle(item.packageName)}
                    />
                  </Pressable>
                )}
              />
            )}
          </View>

          <View
            style={{
              flexDirection: "row",
              justifyContent: "flex-end",
              gap: 8,
            }}
          >
            <Button mode="text" onPress={onClose}>
              Cancel
            </Button>
            <Button
              mode="text"
              onPress={() => {
                onConfirm(selected);
                onClose();
              }}
            >
              {selected.length > 0 ? `Use ${selected.length} selected` : "Use none"}
            </Button>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}
