import LinkButton from "@/components/LinkButton";
import ScreenHeading from "@/components/ScreenHeading";
import Text from "@/components/Text";
import { Stack } from "expo-router";
import { View } from "react-native";
import { Card } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomeScreen() {
  return (
    <>
      <Stack.Header hidden></Stack.Header>
      <SafeAreaView
        style={{
          flex: 1,
          justifyContent: "center",
          alignItems: "center",
          gap: 48,
        }}
      >
        <View style={{ alignItems: "center" }}>
          <ScreenHeading title="Passcodes" />
          <Text style={{ fontSize: 8 }}>v3.1.0.rc1 - Stable</Text>
        </View>

        <Card>
          <Card.Actions
            style={{ flexDirection: "column", alignItems: "stretch", gap: 12 }}
          >
            <LinkButton href={"/save-password"} text="Save Password" />
            <LinkButton href={"/load-password"} text="Password List" />
            <LinkButton href={"/settings"} text="Settings" />
          </Card.Actions>
        </Card>
      </SafeAreaView>
    </>
  );
}
