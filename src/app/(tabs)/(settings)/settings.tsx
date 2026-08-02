import LinkButton from "@/components/LinkButton";
import { LinkIconButton } from "@/components/LinkIconButton";
import Text from "@/components/Text";
import { passwords } from "@/db/schema";
import {
  isBiometricsAuthEnabled,
  toggleBiometricsFeature,
  unlockWithBiometricsApp,
} from "@/libs/biometric";
import constants from "@/libs/constants";
import {
  getGooglePasswordsCSVContent,
  type PasswordCSVFormat,
  sharePasswordAsCSV,
} from "@/libs/exporting";
import {
  convertRawCSVToPasswords,
  getCSVPasswordString,
} from "@/libs/importing";
import {
  isScreenshotPreventionEnabled,
  toggleScreenshotPreventionFeature,
} from "@/libs/screenshot_prevention";
import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { LinearGradient } from "expo-linear-gradient";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import { Alert, ScrollView } from "react-native";
import { Card, Divider, List, Switch, useTheme } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SettingsScreen() {
  const [isAppLockEnabled, setIsAppLockEnabled] = useState(
    isBiometricsAuthEnabled(),
  );
  const toggleInAppLockSwitch = () =>
    setIsAppLockEnabled(toggleBiometricsFeature());

  const [isScreenshotPreventEnabled, setIsScreenshotPreventEnabled] = useState(
    isScreenshotPreventionEnabled(),
  );
  const toggleScreenshotPreventSwitch = () =>
    setIsScreenshotPreventEnabled(toggleScreenshotPreventionFeature());

  const theme = useTheme();
  let db = useSQLiteContext();
  const drizzleDb = drizzle(db);

  async function handleImportPasswords() {
    let content = await getCSVPasswordString();
    let importPasswordList: PasswordCSVFormat[] =
      convertRawCSVToPasswords(content);

    drizzleDb.transaction((tx) => {
      importPasswordList.forEach((importablePassword) => {
        tx.insert(passwords)
          .values({
            domain: importablePassword.domain,
            username: importablePassword.username,
            password: importablePassword.password,
            notes: importablePassword.notes,
            url: importablePassword.url,
          })
          .execute();
      });
    });
  }

  async function handleExportPasswords() {
    const result: PasswordCSVFormat[] = await drizzleDb
      .select()
      .from(passwords);

    let content = await getGooglePasswordsCSVContent(result);
    sharePasswordAsCSV(content);
  }

  return (
    <SafeAreaView style={{ flex: 1, paddingVertical: 12 }}>
      <ScrollView
        contentContainerStyle={{
          paddingHorizontal: 12,
          paddingBottom: 96,
          gap: 24,
        }}
        showsVerticalScrollIndicator
      >
        <Text
          variant="headlineMedium"
          style={{
            marginTop: 8,
            marginBottom: 20,
            marginHorizontal: 4,
            textAlign: "center",
          }}
        >
          Settings
        </Text>

        <Card>
          <List.Section style={{ margin: 15 }}>
            <List.Item
              style={{ paddingRight: 0 }}
              title="In App Lock"
              description="Require biometrics to unlock the app"
              left={(props) => (
                <FontAwesome6
                  {...props}
                  name="fingerprint"
                  iconStyle="solid"
                  size={20}
                />
              )}
              right={() => (
                <Switch
                  style={{ alignSelf: "flex-end" }}
                  value={isAppLockEnabled}
                  onValueChange={toggleInAppLockSwitch}
                />
              )}
            />
          </List.Section>

          <Divider />

          <List.Section style={{ margin: 15 }}>
            <List.Item
              style={{ paddingRight: 0 }}
              title="Screenshot Prevention"
              description="Doesn't allow screenshot, changes require app restart.."
              left={(props) => (
                <FontAwesome6
                  {...props}
                  style={[props.style, { marginVertical: "auto" }]}
                  name="mobile-screen"
                  iconStyle="solid"
                  size={20}
                />
              )}
              right={() => (
                <Switch
                  style={{ alignSelf: "flex-end" }}
                  value={isScreenshotPreventEnabled}
                  onValueChange={toggleScreenshotPreventSwitch}
                />
              )}
            />
          </List.Section>
        </Card>

        <Card>
          <List.Section style={{ margin: 15 }}>
            <List.Item
              title="Import passwords"
              description="Import a Google Password CSV"
              left={(props) => (
                <FontAwesome6
                  {...props}
                  name="download"
                  iconStyle="solid"
                  size={20}
                />
              )}
              right={(props) => (
                <FontAwesome6
                  {...props}
                  name="chevron-right"
                  iconStyle="solid"
                  size={20}
                />
              )}
              onPress={handleImportPasswords}
            />

            <Divider />

            <List.Item
              title="Export passwords"
              description="Share as Google Password CSV"
              left={(props) => (
                <FontAwesome6
                  {...props}
                  name="share-nodes"
                  iconStyle="solid"
                  size={20}
                />
              )}
              right={(props) => (
                <FontAwesome6
                  {...props}
                  name="chevron-right"
                  iconStyle="solid"
                  size={20}
                />
              )}
              onPress={async () => {
                const result = await unlockWithBiometricsApp();

                if (result) {
                  handleExportPasswords();
                } else {
                  Alert.alert(
                    "Authentication Failed",
                    "Exporting passwords is protected by App Lock.",
                  );
                }
              }}
            />
          </List.Section>
        </Card>

        <Card>
          <Card.Content>
            <Text
              style={{ fontSize: 28, marginBottom: 12, textAlign: "center" }}
            >
              Recovery
            </Text>

            <Card.Content
              style={{
                padding: 20,
                flexDirection: "column",
                alignItems: "stretch",
                gap: 16,
              }}
            >
              <LinkButton
                href="/get-back-passwords"
                text="GetBack Passwords From v2"
              />
              <LinkButton href="/data-recovery" text="Data Recovery" />
            </Card.Content>
          </Card.Content>
        </Card>

        <Card>
          <Card.Content style={{ gap: 12 }}>
            <Card.Title
              title={constants.appname}
              titleVariant="headlineLarge"
              titleStyle={{ textAlign: "center" }}
              subtitle={constants.version}
              subtitleVariant="labelMedium"
              subtitleStyle={{ textAlign: "center" }}
              style={{ marginBlock: 24 }}
            />

            <Card.Actions style={{ justifyContent: "center", gap: 12 }}>
              <LinkIconButton
                href="mailto:jeeldobariya38@gmail.com"
                icon={({ size, color }) => (
                  <FontAwesome6
                    name="envelope"
                    size={size}
                    color={color}
                    iconStyle="regular"
                  />
                )}
                darkModeColor="#2a4759"
              />
              <LinkIconButton
                href="https://github.com/PasscodesApp/Passcodes"
                icon={({ size, color }) => (
                  <FontAwesome6
                    name="github"
                    size={size}
                    color={color}
                    iconStyle="brand"
                  />
                )}
                darkModeColor="#3a424b"
              />
              <LinkIconButton
                href="mailto:jeeldobariya38@gmail.com"
                icon={({ size, color }) => (
                  <FontAwesome6
                    name="telegram"
                    size={size}
                    color={color}
                    iconStyle="brand"
                  />
                )}
                darkModeColor="#0088cc"
              />
              <LinkIconButton
                href="https://discord.gg/kSSkYq7KAQ"
                icon={({ size, color }) => (
                  <FontAwesome6
                    name="discord"
                    size={size}
                    color={color}
                    iconStyle="brand"
                  />
                )}
                darkModeColor="#5865F2"
              />
            </Card.Actions>
          </Card.Content>
        </Card>
      </ScrollView>

      <LinearGradient
        pointerEvents="none"
        colors={["transparent", theme.colors.background]}
        style={{
          position: "absolute",
          left: 0,
          right: 0,
          bottom: 0,
          height: 150,
        }}
      />
    </SafeAreaView>
  );
}
