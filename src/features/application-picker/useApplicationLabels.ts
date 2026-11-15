import { parseApplicationIds } from "@/libs/application_ids";
import { useEffect, useState } from "react";

import { getApplicationLabel, isInstalledApplicationsAvailable } from "./installedApps";

/**
 * Names of the apps behind typed application IDs (e.g. "Instagram").
 * Only apps installed on this phone have a name; others are left out.
 */
export function useApplicationLabels(text: string): Record<string, string> {
  const [labels, setLabels] = useState<Record<string, string>>({});
  const idsKey = parseApplicationIds(text).ids.join(",");

  useEffect(() => {
    if (!isInstalledApplicationsAvailable || idsKey === "") {
      setLabels({});
      return;
    }

    let cancelled = false;

    Promise.all(
      idsKey.split(",").map(async (id) => {
        try {
          return [id, await getApplicationLabel(id)] as const;
        } catch {
          return [id, null] as const;
        }
      }),
    ).then((entries) => {
      if (cancelled) return;

      const next: Record<string, string> = {};

      for (const [id, label] of entries) {
        if (label) next[id] = label;
      }

      setLabels(next);
    });

    return () => {
      cancelled = true;
    };
  }, [idsKey]);

  return labels;
}
