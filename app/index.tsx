import { Link } from "expo-router";
import { Text, View } from "react-native";

export default function Home() {
  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Text>Passcodes</Text>
      <Text>vX.X.X - Alpha</Text>
      <Link href={"/savepassword"}>Save Password</Link>
      <Link href={"/loadpassword"}>Load Password</Link>
    </View>
  );
}
