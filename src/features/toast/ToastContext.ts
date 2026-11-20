import { createContext, useContext } from "react";

import type { ShowToast } from "./types";

export type ToastContextValue = {
  showToast: ShowToast;
};

export const ToastContext = createContext<ToastContextValue | null>(null);

export function useToast(): ToastContextValue {
  const context = useContext(ToastContext);

  if (context === null) {
    throw new Error("useToast must be used inside ToastProvider");
  }

  return context;
}
