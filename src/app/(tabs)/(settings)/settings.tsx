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
import { FontAwesome6 } from "@react-native-vector-icons/fontawesome6";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import { Alert, ScrollView } from "react-native";
import { Button, Card, Switch } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SettingsScreen() {
  const [isEnabled, setIsEnabled] = useState(isBiometricsAuthEnabled());
  const toggleSwitch = () => setIsEnabled(toggleBiometricsFeature());

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
    <SafeAreaView style={{ flex: 1, paddingHorizontal: 12 }}>
      <ScrollView contentContainerStyle={{ gap: 16 }}>
        <Card>
          <Card.Content>
            <Text style={{ fontSize: 16, fontWeight: "bold" }}>
              In App Lock (Biometrics):
            </Text>
          </Card.Content>
          <Card.Actions>
            <Switch value={isEnabled} onValueChange={toggleSwitch} />
          </Card.Actions>
        </Card>

        <Card>
          <Card.Actions style={{ flexDirection: "column", gap: 12 }}>
            <Button
              icon={({ size, color }) => (
                <FontAwesome6
                  name="upload"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={() => handleImportPasswords()}
            >
              <Text>Import With Google Passwords Format</Text>
            </Button>

            <Button
              mode="outlined"
              icon={({ size, color }) => (
                <FontAwesome6
                  name="share-nodes"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={async () => {
                let result = await unlockWithBiometricsApp();
                if (result) {
                  handleExportPasswords();
                } else {
                  Alert.alert(
                    "Authentication Failed!!!",
                    "exporting passwords is security activity and is protected by app lock.",
                  );
                }
              }}
            >
              <Text>Share With Google Passwords Format</Text>
            </Button>
          </Card.Actions>
        </Card>

        <Card>
          <Card.Content>
            <Text
              style={{ fontSize: 28, marginBottom: 12, textAlign: "center" }}
            >
              TroubleShooting
            </Text>

            <Card.Actions style={{ flexDirection: "column", gap: 12 }}>
              <LinkButton
                href="/get-back-passwords"
                text="GetBack Passwords From v2"
              />
              <LinkButton href="/data-recovery" text="Data Recovery" />
            </Card.Actions>
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
    </SafeAreaView>
  );
}
