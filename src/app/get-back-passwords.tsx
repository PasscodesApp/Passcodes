import ScreenHeading from "@/components/ScreenHeading";
import { Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function GetBackPasswordsScreen() {
  return (
    <SafeAreaView
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        padding: 20,
        gap: 10,
      }}
    >
      <ScreenHeading title="GetBack Data" />
      <Text
        style={{
          color: "#06a3eb",
          fontSize: 16,
        }}
      >
        Migration is not need for your device!!
      </Text>
    </SafeAreaView>
  );
}
