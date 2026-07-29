import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { Tabs } from "expo-router";

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: true,
        tabBarActiveTintColor: "#34597f",
        headerTitleAlign: "center",
      }}
    >
      <Tabs.Screen
        name="(password-manager)"
        options={{
          title: "Passcodes",
          tabBarIcon: ({ size, color }) => (
            <FontAwesome6
              name="key"
              size={size}
              color={color}
              iconStyle="solid"
            />
          ),
        }}
      />
      <Tabs.Screen
        name="(settings)"
        options={{
          title: "Settings",
          tabBarIcon: ({ size, color }) => (
            <FontAwesome6
              name="gear"
              size={size}
              color={color}
              iconStyle="solid"
            />
          ),
        }}
      />
    </Tabs>
  );
}
