import LinkButton from "@/components/LinkButton";
import Text from "@/components/Text";
import { passwords } from "@/db/schema";
import {
  isBiometricsAuthEnabled,
  toggleBiometricsFeature,
  unlockWithBiometricsApp,
} from "@/libs/biometric";
import {
  getGooglePasswordsCSVContent,
  type PasswordCSVFormat,
  sharePasswordAsCSV,
} from "@/libs/exporting";
import {
  convertRawCSVToPasswords,
  getCSVPasswordString,
} from "@/libs/importing";
import { drizzle } from "drizzle-orm/expo-sqlite";
import { Stack } from "expo-router";
import { useSQLiteContext } from "expo-sqlite";
import { useState } from "react";
import { Alert, ScrollView } from "react-native";
import { Button, Card, Switch, useTheme } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SettingsScreen() {
  const theme = useTheme();
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
    <>
      <Stack.Title>Settings</Stack.Title>
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
              <Button onPress={() => handleImportPasswords()}>
                <Text>Import With Google Passwords Format</Text>
              </Button>

              <Button
                mode="outlined"
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
                  href={"/get-back-passwords"}
                  text="GetBack Passwords From v2"
                />
                <LinkButton href={"/data-recovery"} text="Data Recovery" />
              </Card.Actions>
            </Card.Content>
          </Card>

          <Card>
            <Card.Content>
              <Text
                style={{ fontSize: 28, marginBottom: 12, textAlign: "center" }}
              >
                Contact Us
              </Text>

              <Card.Actions style={{ flexDirection: "column", gap: 12 }}>
                <LinkButton
                  href={"https://discord.gg/kSSkYq7KAQ"}
                  text="Join Discord"
                />
                <LinkButton
                  href={"https://passcodesapp.github.io/Passcodes-Website/"}
                  text="Goto Website"
                />
                <LinkButton
                  href={"mailto:jeeldobariya38@gmail.com"}
                  text="Email Me"
                />
              </Card.Actions>
            </Card.Content>
          </Card>
        </ScrollView>
      </SafeAreaView>
    </>
  );
}
