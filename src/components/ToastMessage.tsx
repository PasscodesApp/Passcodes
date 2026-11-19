import FontAwesome6 from "@react-native-vector-icons/fontawesome6/static";
import { useEffect, useState } from "react";
import { Animated, View } from "react-native";
import { Text } from "react-native-paper";

export type ToastType = "success" | "error";

type ToastMessageProps = {
  message: string;
  visible: boolean;
  type?: ToastType;
  duration?: number;
  onHide: () => void;
};

export default function ToastMessage({
  message,
  visible,
  type = "success",
  duration = 2200,
  onHide,
}: ToastMessageProps) {
  // State (not a ref) holds the animated value: created once, safe to read
  // during render.
  const [opacity] = useState(() => new Animated.Value(0));

  useEffect(() => {
    // Nothing to animate while hidden. (The view is not even mounted then,
    // so starting a native animation here would run on a detached node.)
    if (!visible) {
      return;
    }

    const fadeIn = Animated.timing(opacity, {
      toValue: 1,
      duration: 150,
      useNativeDriver: true,
    });

    const fadeOut = Animated.timing(opacity, {
      toValue: 0,
      duration: 200,
      useNativeDriver: true,
    });

    fadeIn.start();

    const timeout = setTimeout(() => {
      fadeOut.start(({ finished }) => {
        // An interrupted fade-out (new toast, unmount) must not hide the
        // toast that replaced it.
        if (finished) {
          onHide();
        }
      });
    }, duration);

    return () => {
      clearTimeout(timeout);

      // Stop running native animations before the view goes away.
      fadeIn.stop();
      fadeOut.stop();
    };
    // `message`: a new toast while one is showing restarts the timer.
  }, [visible, message, duration, onHide, opacity]);

  if (!visible) {
    return null;
  }

  const isError = type === "error";

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
          backgroundColor: isError ? "#7f1e19" : "#323232",
        }}
      >
        <FontAwesome6
          name={isError ? "xmark" : "check"}
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
