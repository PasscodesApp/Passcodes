import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from "react";

import { ToastContext } from "./ToastContext";
import ToastView from "./ToastView";
import type { ShowToast, ToastState } from "./types";

const DEFAULT_DURATION = 2200;

/** A toast with an Undo button stays a bit longer so there is time to tap it. */
const UNDO_DURATION = 4000;

/**
 * Owns the one visible toast. A new `showToast` replaces the current toast
 * and restarts the timer.
 *
 * No animation on purpose: the toast simply appears and disappears (animations
 * were crashing the app).
 */
export function ToastProvider({ children }: PropsWithChildren) {
  const [toast, setToast] = useState<ToastState | null>(null);

  const showToast = useCallback<ShowToast>(
    (message, type = "success", options = {}) => {
      const { duration, onUndo } = options;

      setToast({
        message,
        type,
        onUndo,
        duration: duration ?? (onUndo ? UNDO_DURATION : DEFAULT_DURATION),
      });
    },
    [],
  );

  // `toast` is a new object per showToast call, so every toast gets its own
  // timer (and replacing a toast restarts it).
  useEffect(() => {
    if (toast === null) {
      return;
    }

    const timeout = setTimeout(() => setToast(null), toast.duration);

    return () => clearTimeout(timeout);
  }, [toast]);

  const handleUndo = useCallback(() => {
    toast?.onUndo?.();
    setToast(null);
  }, [toast]);

  const contextValue = useMemo(() => ({ showToast }), [showToast]);

  return (
    <ToastContext.Provider value={contextValue}>
      {children}

      {toast !== null && <ToastView toast={toast} onUndo={handleUndo} />}
    </ToastContext.Provider>
  );
}
