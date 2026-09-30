/**
 * Update engine — Modrinth parity outdated detection.
 *
 * Strategy per mod file:
 *   1. Resolve project identity (cached Modrinth search).
 *   2. Fetch latest compatible version for the instance (MC + loader).
 *   3. Compare: stored meta versionNumber (exact) > filename contains check >
 *      published-timestamp fallback.
 */
import { latestCompatibleVersion, resolveModIdentity, type ProjectVersion } from "./modrinth";
import { getModMeta } from "./mod-meta";
import { listInstanceFiles } from "./backend";
import { isManagedMod } from "./tyxmod";

export interface ModUpdate {
  file: string;
  size: number;
  modified: number;
  projectId: string;
  title: string;
  current: string;
  latest: string;
  versionRef: ProjectVersion;
}

function currentLabel(instanceId: string, filename: string): string {
  return getModMeta(instanceId, filename)?.versionNumber ?? "";
}

/** Check one instance for outdated mods. Concurrency-capped, never throws. */
export async function checkForUpdates(
  instanceId: string,
  mcVersion?: string,
  loader?: string,
  opts?: { limit?: number; concurrency?: number },
): Promise<{ updates: ModUpdate[]; checked: number }> {
  const limit = opts?.limit ?? 50;
  const concurrency = opts?.concurrency ?? 4;
  let files: { name: string; size: number; modified: number; isDir: boolean }[] = [];
  try {
    files = await listInstanceFiles(instanceId, "mods");
  } catch {
    return { updates: [], checked: 0 };
  }
  // Managed stack (SwiftClient, Fabric API) is launcher-owned — never
  // offered for updates, never shown as outdated.
  const jars = files
    .filter(
      (f) =>
        !f.isDir && /\.(jar|zip)(\.disabled|\.off)?$/i.test(f.name) && !isManagedMod(f.name),
    )
    .slice(0, limit);
  const updates: ModUpdate[] = [];
  // Simple worker pool to avoid hammering Modrinth (429s).
  let i = 0;
  async function worker() {
    while (i < jars.length) {
      const f = jars[i++];
      try {
        const id = await resolveModIdentity(f.name.replace(/\.(disabled|off)$/i, ""));
        if (!id?.projectId) continue;
        const v = await latestCompatibleVersion(id.projectId, mcVersion, loader);
        if (!v) continue;
        const current = currentLabel(instanceId, f.name);
        const isNewer =
          current && current !== v.version_number
            ? true
            : !f.name.includes(v.version_number) &&
              Number.isFinite(Date.parse(v.date_published)) &&
              Date.parse(v.date_published) > f.modified + 60_000;
        if (isNewer) {
          updates.push({
            file: f.name,
            size: f.size,
            modified: f.modified,
            projectId: id.projectId,
            title: id.title,
            current: current || "installed",
            latest: v.version_number,
            versionRef: v,
          });
        }
      } catch {
        /* per-file failures never fail the sweep */
      }
    }
  }
  await Promise.all(Array.from({ length: Math.min(concurrency, jars.length) }, () => worker()));
  return { updates, checked: jars.length };
}

/** Update every entry from checkForUpdates. Returns {ok, failed}. */
export async function updateAllMods(
  instanceId: string,
  updates: ModUpdate[],
  opts?: { gameVersion?: string; loader?: string; onEach?: (u: ModUpdate, idx: number, total: number) => void },
): Promise<{ ok: number; failed: { file: string; error: string }[] }> {
  const { installModWithDeps } = await import("./backend");
  const { markInstalledFor } = await import("./modrinth");
  const { setModMeta } = await import("./mod-meta");
  let ok = 0;
  const failed: { file: string; error: string }[] = [];
  for (let n = 0; n < updates.length; n++) {
    const u = updates[n];
    try {
      opts?.onEach?.(u, n, updates.length);
      const l = (opts?.loader ?? "").toLowerCase();
      const paths = await installModWithDeps(instanceId, "mods", u.versionRef, {
        gameVersion: opts?.gameVersion,
        loader: ["fabric", "forge", "quilt", "neoforge"].includes(l) ? l : undefined,
      });
      const main = paths[0]?.split(/[\\/]/).pop() ?? u.versionRef.files[0]?.filename ?? u.file;
      markInstalledFor(instanceId, u.projectId, main);
      setModMeta(instanceId, u.projectId, u.versionRef, main);
      // Remove the stale jar when the filename changed (true update, not overwrite).
      if (main.toLowerCase() !== u.file.toLowerCase()) {
        try {
          const { deleteInstanceFile } = await import("./backend");
          const { clearModMeta } = await import("./mod-meta");
          await deleteInstanceFile(instanceId, "mods", u.file);
          clearModMeta(instanceId, u.file);
        } catch {
          /* stale file cleanup is best-effort */
        }
      }
      ok++;
    } catch (e) {
      failed.push({ file: u.file, error: e instanceof Error ? e.message : String(e) });
    }
  }
  return { ok, failed };
}
