// contexts/ToastContext.tsx

import ToastMessage from "@/components/ToastMessage";

import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";

type ToastContextValue = {
  showToast: (message: string, duration?: number) => void;
};

const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: PropsWithChildren) {
  const [message, setMessage] = useState("");
  const [duration, setDuration] = useState(2200);

  const showToast = useCallback((message: string, duration = 2200) => {
    setMessage(message);
    setDuration(duration);
  }, []);

  const hideToast = useCallback(() => {
    setMessage("");
  }, []);

  const contextValue = useMemo(
    () => ({
      showToast,
    }),
    [showToast],
  );

  return (
    <ToastContext.Provider value={contextValue}>
      {children}

      <ToastMessage
        message={message}
        visible={message !== ""}
        duration={duration}
        onHide={hideToast}
      />
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);

  if (context === null) {
    throw new Error("useToast must be used inside ToastProvider");
  }

  return context;
}
