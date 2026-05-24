import { Link } from "expo-router";
import { Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function Settings() {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        gap: 15,
      }}
    >
      <Text style={{ fontSize: 32 }}>Settings</Text>

      <View>
        <Text style={{ fontSize: 28 }}>TroubleShooting</Text>
        <Link style={{ color: "#5a5ada" }} href={"/getbackpasswords"}>
          GetBack Passwords From Prior To v3.
        </Link>
      </View>

      <View>
        <Text style={{ fontSize: 28 }}>Contact Us</Text>
        <Link
          style={{ color: "#5a5ada" }}
          href={"https://passcodesapp.github.io/Passcodes-Website/"}
        >
          Goto Website
        </Link>
        <Link
          style={{ color: "#5a5ada" }}
          href={"mailto:jeeldobariya38@gmail.com"}
        >
          Email Me
        </Link>
      </View>
    </SafeAreaView>
  );
}
