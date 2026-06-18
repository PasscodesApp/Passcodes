import LinkButton from "@/components/LinkButton";
import ScreenHeading from "@/components/ScreenHeading";
import {
  isBiometricsAuthEnabled,
  toggleBiometricsFeature,
} from "@/libs/biometric";
import { useState } from "react";
import { ScrollView, Switch, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SettingsScreen() {
  const [isEnabled, setIsEnabled] = useState(isBiometricsAuthEnabled());
  const toggleSwitch = () => setIsEnabled(toggleBiometricsFeature());

  return (
    <SafeAreaView style={{ padding: 20 }}>
      <ScrollView contentContainerStyle={{ gap: 15 }}>
        <ScreenHeading title="Settings" />

        <View
          style={{
            backgroundColor: "#efe9e3",
            borderWidth: 2,
            padding: 16,
            borderRadius: 20,
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <Text style={{ fontSize: 16, fontWeight: "bold" }}>
            In App Lock (Biometrics):
          </Text>
          <Switch
            value={isEnabled}
            onValueChange={toggleSwitch}
            trackColor={{ false: "#767577", true: "#81b0ff" }}
            thumbColor={isEnabled ? "#25f068" : "#4d2b05"}
            ios_backgroundColor="#3e3e3e"
          />
        </View>

        <View
          style={{
            backgroundColor: "#efe9e3",
            borderWidth: 2,
            padding: 16,
            borderRadius: 20,
            gap: 12,
          }}
        >
          <Text style={{ fontSize: 32, marginBottom: 12 }}>
            TroubleShooting
          </Text>
          <LinkButton
            style={{ color: "#0257c1", textAlign: "center" }}
            href={"/get-back-passwords"}
            text="GetBack Passwords From v2"
          />
          <LinkButton
            style={{ color: "#0257c1", textAlign: "center" }}
            href={"/data-recovery"}
            text="Data Recovery"
          />
        </View>

        <View
          style={{
            backgroundColor: "#efe9e3",
            borderWidth: 2,
            padding: 16,
            borderRadius: 20,
            gap: 12,
          }}
        >
          <Text style={{ fontSize: 32 }}>Contact Us</Text>
          <LinkButton
            style={{ color: "#0257c1", textAlign: "center" }}
            href={"https://passcodesapp.github.io/Passcodes-Website/"}
            text="Goto Website"
          />
          <LinkButton
            style={{ color: "#0257c1", textAlign: "center" }}
            href={"mailto:jeeldobariya38@gmail.com"}
            text="Email Me"
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
