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
        alignItems: "center",
        gap: 15,
      }}
    >
      <ScreenHeading title="Settings" />

      <View>
        <Text style={{ fontSize: 32 }}>TroubleShooting</Text>
        <LinkButton
          style={{ color: "#5a5ada" }}
          href={"/get-back-passwords"}
          text="GetBack Passwords From Prior To v3."
        />
      </View>

      <View>
        <Text style={{ fontSize: 32 }}>Contact Us</Text>
        <LinkButton
          style={{ color: "#5a5ada", textAlign: "center" }}
          href={"https://passcodesapp.github.io/Passcodes-Website/"}
          text="Goto Website"
        />
        <LinkButton
          style={{ color: "#5a5ada", textAlign: "center" }}
          href={"mailto:jeeldobariya38@gmail.com"}
          text="Email Me"
        />
      </View>
    </SafeAreaView>
  );
}
