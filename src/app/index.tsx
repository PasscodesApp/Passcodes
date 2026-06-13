import LinkButton from "@/components/LinkButton";
import ScreenHeading from "@/components/ScreenHeading";
import { Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomeScreen() {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        gap: 24,
      }}
    >
      <View style={{ alignItems: "center" }}>
        <ScreenHeading title="Passcodes" />
        <Text style={{ fontSize: 12 }}>v3.0.0 - Alpha</Text>
      </View>

      <View style={{ alignItems: "stretch", gap: 12 }}>
        <LinkButton href={"/save-password"} text="Save Password" />
        <LinkButton href={"/load-password"} text="Password List" />
        <LinkButton href={"/settings"} text="Settings" />
      </View>
    </SafeAreaView>
  );
}
