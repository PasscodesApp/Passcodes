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
          <ScreenHeading title="Passcodes" style={{ color: "#34597f" }} />
          <Text style={{ fontSize: 8 }}>v3.1.0.rc1 - Stable</Text>
        </View>

        <Card
          style={{
            padding: 12,
          }}
        >
          <Card.Actions
            style={{ flexDirection: "column", alignItems: "stretch", gap: 12 }}
          >
            <LinkButton
              variant="primary"
              href={"/password-manager"}
              text="Password Manager"
            />
            <LinkButton href={"/settings"} text="Settings" />
          </Card.Actions>
        </Card>
      </SafeAreaView>
    </>
  );
}
