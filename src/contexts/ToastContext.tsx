import ToastMessage, { ToastType } from "@/components/ToastMessage";

import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";

type ToastContextValue = {
  showToast: (message: string, type?: ToastType, duration?: number) => void;
};

const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: PropsWithChildren) {
  const [message, setMessage] = useState("");
  const [type, setType] = useState<ToastType>("success");
  const [duration, setDuration] = useState(2200);

  const showToast = useCallback(
    (message: string, type: ToastType = "success", duration = 2200) => {
      setMessage(message);
      setType(type);
      setDuration(duration);
    },
    [],
  );

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
        type={type}
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
