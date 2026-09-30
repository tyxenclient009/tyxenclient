/**
 * Known-crashing mod versions — auto-healed on Play before boot.
 *
 * Case: ImmediatelyFast 1.14.2+1.21.11 crashes MC 1.21.11 during init:
 *   MixinShaderManager.checkForCoreShaderModifications targets
 *   method_62945 which no longer exists → "Mixin transformation of
 *   net.minecraft.class_10151 failed" → exit 0xffffffff.
 * Fixed upstream in 1.14.3+1.21.11-fabric.
 *
 * Strategy (best-effort, never blocks Play):
 *   1. Detect the bad jar (active or already .disabled).
 *   2. Try latest compatible version from Modrinth → install + delete old.
 *   3. Offline / no compatible version → disable (.disabled) so the game boots.
 */

export interface IncompatEntry {
  /** substring match on filename, lowercase */
  match: string;
  /** MC versions this entry applies to */
  mc: string[];
  /**
   * How to heal:
   * - "update": try latest compatible Modrinth build first, disable on failure.
   * - "disable": disable unconditionally (even the newest build crashes).
   */
  action: "update" | "disable";
  /** substring that identifies the bad build in the filename (update mode) */
  badBuild?: string;
  /** Modrinth project slug/id for the fixed build (update mode) */
  projectId?: string;
  reason: string;
}

export const KNOWN_BAD: IncompatEntry[] = [
  {
    match: "immediatelyfast",
    mc: ["1.21.11"],
    action: "disable",
    reason:
      "ImmediatelyFast crashes 1.21.11 at boot (MixinShaderManager targets method_62945, missing even in 1.14.3). Re-enable after its author ships a fixed build.",
  },
];

export async function quarantineIncompatibleMods(
  instanceId: string,
  mcVersion: string,
  loader: string,
  notify: (kind: "info" | "success" | "error", title: string, body?: string) => void,
): Promise<void> {
  const l = loader.toLowerCase();
  if (l !== "fabric") return;
  const hits = KNOWN_BAD.filter((e) => e.mc.includes(mcVersion));
  if (hits.length === 0) return;
  const { listInstanceFiles, deleteInstanceFile, toggleInstanceFile, installModWithDeps } =
    await import("./backend");
  let files: { name: string; isDir: boolean }[] = [];
  try {
    files = await listInstanceFiles(instanceId, "mods");
  } catch {
    return;
  }
  for (const entry of hits) {
    const bad = files.filter(
      (f) =>
        !f.isDir &&
        f.name.toLowerCase().includes(entry.match) &&
        (entry.action === "disable" ||
          (entry.badBuild && f.name.includes(entry.badBuild))) &&
        !f.name.toLowerCase().startsWith("tyxen-"),
    );
    for (const f of bad) {
      const disabled = /\.disabled$/i.test(f.name);
      if (entry.action === "update" && entry.projectId) {
        try {
          // Try self-heal: latest compatible build from Modrinth.
          const { latestCompatibleVersion, pickFile, markInstalledFor } = await import("./modrinth");
          const { setModMeta } = await import("./mod-meta");
          const v = await latestCompatibleVersion(entry.projectId, mcVersion, "fabric");
          if (v) {
            const paths = await installModWithDeps(instanceId, "mods", v, {
              gameVersion: mcVersion,
              loader: "fabric",
            });
            const main = paths[0]?.split(/[\\/]/).pop() ?? pickFile(v)?.filename;
          if (main) {
            markInstalledFor(instanceId, entry.projectId, main);
            setModMeta(instanceId, entry.projectId, v, main);
          }
            try {
              await deleteInstanceFile(instanceId, "mods", f.name);
            } catch {
              /* already replaced */
            }
            notify("success", "Fixed crashing mod", `${f.name} → updated (${v.version_number}).`);
            continue;
          }
        } catch {
          /* offline — fall through to disable */
        }
      }
      if (!disabled) {
        try {
          await toggleInstanceFile(instanceId, "mods", f.name);
          notify(
            "info",
            "Disabled crashing mod",
            `${f.name}: ${entry.reason} Update it in Browse when online.`,
          );
        } catch {
          /* best-effort */
        }
      } else {
        notify("info", "Crashing mod stays disabled", `${f.name}: ${entry.reason}`);
      }
    }
  }
}
