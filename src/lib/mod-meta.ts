/**
 * Per-file mod metadata — lets update checks compare pinned versions
 * instead of guessing from filenames/timestamps.
 *
 * Stored in localStorage (browser + Tauri share it via WebView):
 *   tyx:mod-meta:<instanceId> = { "<filename>": ModMeta }
 */
import type { ProjectVersion } from "./modrinth";

export interface ModMeta {
  projectId: string;
  versionId: string;
  versionNumber: string;
  filename: string;
  updatedAt: number;
}

const key = (instanceId: string) => `tyx:mod-meta:${instanceId}`;

/**
 * Meta is keyed by the ENABLED filename: `foo.jar.disabled` and `foo.jar`
 * are the same mod. Without this, disabled mods always miss the
 * pinned-version fast path (and deletes leave stale entries behind).
 */
export function metaKey(filename: string): string {
  return filename.replace(/\.(disabled|off)$/i, "");
}

export function getModMetaMap(instanceId: string): Record<string, ModMeta> {
  try {
    return JSON.parse(localStorage.getItem(key(instanceId)) ?? "{}") as Record<string, ModMeta>;
  } catch {
    return {};
  }
}

export function getModMeta(instanceId: string, filename: string): ModMeta | null {
  return getModMetaMap(instanceId)[metaKey(filename)] ?? null;
}

export function setModMeta(instanceId: string, projectId: string, version: ProjectVersion, filename: string) {
  try {
    const m = getModMetaMap(instanceId);
    const k = metaKey(filename);
    m[k] = {
      projectId,
      versionId: version.id,
      versionNumber: version.version_number,
      filename: k,
      updatedAt: Date.now(),
    };
    localStorage.setItem(key(instanceId), JSON.stringify(m));
  } catch {
    /* ignore */
  }
}

/** Record a full install result (main file + deps get their own entries when known). */
export function recordModInstall(
  instanceId: string,
  projectId: string,
  version: ProjectVersion,
  installedFilename: string,
) {
  setModMeta(instanceId, projectId, version, installedFilename);
}

/** Drop a whole instance's meta map (instance deleted — no stale pins on ID reuse). */
export function clearAllModMeta(instanceId: string) {
  try {
    localStorage.removeItem(key(instanceId));
  } catch {
    /* ignore */
  }
}

export function clearModMeta(instanceId: string, filename: string) {
  try {
    const m = getModMetaMap(instanceId);
    delete m[metaKey(filename)];
    // Legacy entries may be stored under the raw name — drop both.
    delete m[filename];
    localStorage.setItem(key(instanceId), JSON.stringify(m));
  } catch {
    /* ignore */
  }
}
