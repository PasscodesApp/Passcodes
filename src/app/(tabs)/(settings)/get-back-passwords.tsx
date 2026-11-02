import Screen from "@/components/Screen";
import ScreenHeading from "@/components/ScreenHeading";
import Text from "@/components/Text";

export default function GetBackPasswordsScreen() {
  return (
    <Screen
      style={{
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
    </Screen>
  );
}
