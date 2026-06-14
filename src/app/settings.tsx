import LinkButton from "@/components/LinkButton";
import ScreenHeading from "@/components/ScreenHeading";
import { Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SettingsScreen() {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "stretch",
        padding: 20,
        gap: 15,
      }}
    >
      <ScreenHeading title="Settings" />

      <View
        style={{
          backgroundColor: "#efe9e3",
          borderWidth: 2,
          padding: 16,
          borderRadius: 20,
          gap: 12,
        }}
      >
        <Text style={{ fontSize: 32, marginBottom: 12 }}>TroubleShooting</Text>
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
    </SafeAreaView>
  );
}
