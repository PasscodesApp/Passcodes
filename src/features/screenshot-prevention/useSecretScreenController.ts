import { useEffect } from "react";
import { allowScreenShots, preventScreenShots } from "./runtime";

export default function useSecretScreenController(key: string) {
  // This for clean up, whenever screen goes it reenables screen shots.
  useEffect(() => {
    return () => {
      allowScreenShots(key);
    };
  }, []);

  return {
    preventScreenShots: () => preventScreenShots(key),
    allowScreenShots: () => allowScreenShots(key),
  };
}
