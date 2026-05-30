import ScreenHeading from "@/components/ScreenHeading";
import { Link } from "expo-router";
import { Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function Home() {
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

      <Link style={{ fontSize: 16 }} href={"/savepassword"}>
        Save Password
      </Link>
      <Link style={{ fontSize: 16 }} href={"/loadpassword"}>
        Load Password
      </Link>
      <Link style={{ fontSize: 16 }} href={"/settings"}>
        Settings
      </Link>
    </SafeAreaView>
  );
}
