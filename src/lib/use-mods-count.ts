import { useEffect, useState } from "react";
import { listInstanceFiles } from "@/lib/backend";
import { countUserMods } from "@/lib/tyxmod";

/**
 * Mod counts per instance (USER jars in `mods/` — the hidden Tyx stack
 * doesn't count), for card badges. Local disk reads — fast, cached per
 * id-set, never throws.
 */
export function useModsCounts(ids: string[]): Record<string, number> {
  const [counts, setCounts] = useState<Record<string, number>>({});
  const key = ids.join(",");
  useEffect(() => {
    if (ids.length === 0) {
      setCounts({});
      return;
    }
    let live = true;
    Promise.allSettled(
      ids.map(async (id) => {
        const files = await listInstanceFiles(id, "mods");
        return { id, n: countUserMods(files) };
      }),
    ).then((rs) => {
      if (!live) return;
      const m: Record<string, number> = {};
      rs.forEach((r) => {
        if (r.status === "fulfilled") m[r.value.id] = r.value.n;
      });
      setCounts(m);
    });
    return () => {
      live = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);
  return counts;
}
