// components/ToastMessage.tsx

import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import { useEffect, useRef } from "react";
import { Animated, View } from "react-native";
import { Text } from "react-native-paper";

type ToastMessageProps = {
  message: string;
  visible: boolean;
  duration?: number;
  onHide: () => void;
};

export default function ToastMessage({
  message,
  visible,
  duration = 2200,
  onHide,
}: ToastMessageProps) {
  const opacity = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (!visible) {
      Animated.timing(opacity, {
        toValue: 0,
        duration: 150,
        useNativeDriver: true,
      }).start();

      return;
    }

    Animated.timing(opacity, {
      toValue: 1,
      duration: 150,
      useNativeDriver: true,
    }).start();

    const timeout = setTimeout(() => {
      Animated.timing(opacity, {
        toValue: 0,
        duration: 200,
        useNativeDriver: true,
      }).start(() => {
        onHide();
      });
    }, duration);

    return () => {
      clearTimeout(timeout);
    };
  }, [visible, duration, onHide, opacity]);

  if (!visible) {
    return null;
  }

  return (
    <Animated.View
      pointerEvents="none"
      style={{
        position: "absolute",
        left: 20,
        right: 20,
        bottom: 100,
        alignItems: "center",
        opacity,
        transform: [
          {
            translateY: opacity.interpolate({
              inputRange: [0, 1],
              outputRange: [10, 0],
            }),
          },
        ],
      }}
    >
      <View
        style={{
          flexDirection: "row",
          alignItems: "center",
          gap: 8,
          paddingHorizontal: 16,
          paddingVertical: 10,
          borderRadius: 24,
          elevation: 4,
          backgroundColor: "#323232",
        }}
      >
        <FontAwesome6
          name="check"
          size={16}
          color="#ffffff"
          iconStyle="solid"
        />

        <Text
          variant="bodyMedium"
          style={{
            color: "#ffffff",
          }}
        >
          {message}
        </Text>
      </View>
    </Animated.View>
  );
}
