export type ToastType = "success" | "error";

export type ToastOptions = {
  /** How long the toast stays, in ms. Default: 2200 (4000 with an Undo). */
  duration?: number;

  /**
   * Shows an "Undo" button on the toast. Pressing it runs this handler and
   * closes the toast.
   */
  onUndo?: () => void;
};

export type ShowToast = (
  message: string,
  type?: ToastType,
  options?: ToastOptions,
) => void;

/** What the toast view renders (one toast at a time). */
export type ToastState = {
  message: string;
  type: ToastType;
  duration: number;
  onUndo: (() => void) | undefined;
};
