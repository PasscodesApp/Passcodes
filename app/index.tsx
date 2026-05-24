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
        gap: 10,
      }}
    >
      <Text style={{ fontSize: 36 }}>Passcodes</Text>
      <Text style={{ fontSize: 16 }}>v3.0.0 - Alpha</Text>

      <Link style={{ fontSize: 20 }} href={"/savepassword"}>
        Save Password
      </Link>
      <Link style={{ fontSize: 20 }} href={"/loadpassword"}>
        Load Password
      </Link>
      <Link style={{ fontSize: 20 }} href={"/getbackpasswords"}>
        GetBack Password
      </Link>
    </SafeAreaView>
  );
}
