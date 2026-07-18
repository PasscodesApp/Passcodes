import LinkButton from "@/components/LinkButton";
import ScreenHeading from "@/components/ScreenHeading";
import Text from "@/components/Text";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { Stack } from "expo-router";
import { useColorScheme, View } from "react-native";
import { Card } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomeScreen() {
  const scheme = useColorScheme();

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
          <ScreenHeading
            title="Passcodes"
            style={{
              color: scheme === "dark" ? "#289ede" : "#34597f",
              fontSize: 32,
            }}
          />
          <Text style={{ fontSize: 12 }}>v3.1.0.rc2 - Stable</Text>
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
              icon={({ size, color }) => (
                <FontAwesome6
                  name="key"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
            />
            <LinkButton
              href={"/settings"}
              text="Settings"
              icon={({ size, color }) => (
                <FontAwesome6
                  name="gear"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
            />
          </Card.Actions>
        </Card>
      </SafeAreaView>
    </>
  );
}
