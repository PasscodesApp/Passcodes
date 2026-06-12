import LinkButton from "@/components/LinkButton";
import ScreenHeading from "@/components/ScreenHeading";
import { Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomeScreen() {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        gap: 15,
      }}
    >
      <ScreenHeading title="Passcodes" />
      <Text style={{ fontSize: 12 }}>v3.0.0 - Alpha</Text>

      <LinkButton href={"/save-password"} text="Save Password" />
      <LinkButton href={"/load-password"} text="Load Password" />
      <LinkButton href={"/settings"} text="Settings" />
    </SafeAreaView>
  );
}
