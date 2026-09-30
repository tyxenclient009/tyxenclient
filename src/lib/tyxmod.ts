/**
 * Tyxen stack — Lunar-style "it just works".
 *
 * On Play, Fabric instances on a supported MC version automatically get:
 *   1. tyxen (bundled in the app, exact version pin)
 *   2. Fabric API (latest compatible from Modrinth, SHA1-verified)
 *
 * Version truth: Tyxen declares `~1.21.11`, so only 1.21.11 is covered.
 * Best-effort: any failure only toasts, Play continues.
 *
 * NOTE: the old tyx-client companion mod and swiftclient are fully
 * retired — their jars are purged from instances on Play (LEGACY_PREFIXES).
 */

/** MC version → bundled mod jar (shipped in public/tyxen). */
const BUNDLED: Record<string, string> = {
  "1.21.11": "tyxen/tyxen-1.0.73.jar",
};

export function tyxModFor(mcVersion: string): string | null {
  return BUNDLED[mcVersion] ?? null;
}

export function supportsTyxMod(mcVersion: string, loader: string): boolean {
  return loader.toLowerCase() === "fabric" && tyxModFor(mcVersion) !== null;
}

/**
 * Dawn-style hidden stack: launcher-managed jars are invisible everywhere
 * (mod list, counts, update sweeps) — always installed, never deletable
 * from the UI, self-updated on Play. Users only ever see their own mods.
 */
const MANAGED_PREFIXES = ["tyxen-", "fabric-api-"];

/** Retired companion mods — deleted from instances, never installed. */
const LEGACY_PREFIXES = ["tyx-client-", "swiftclient-"];

export function isManagedMod(filename: string): boolean {
  const n = filename.toLowerCase();
  return MANAGED_PREFIXES.some((p) => n.startsWith(p));
}

/**
 * One shared definition of "user mod file" for every badge/list in the
 * app: a real file (no dirs), jar or zip (incl. .disabled/.off variants),
 * minus the hidden launcher-managed stack. Badges disagree when each
 * counter invents its own rule — so use this everywhere.
 */
export function countUserMods(files: { name: string; isDir: boolean }[]): number {
  return files.filter(
    (f) => !f.isDir && /\.(jar|zip)(\.disabled|\.off)?$/i.test(f.name) && !isManagedMod(f.name),
  ).length;
}

async function hasPrefix(instanceId: string, prefix: string): Promise<boolean> {
  const { listInstanceFiles } = await import("./backend");
  try {
    const files = await listInstanceFiles(instanceId, "mods");
    return files.some((f) => !f.isDir && f.name.toLowerCase().startsWith(prefix));
  } catch {
    return false;
  }
}

/** `tyxen-1.0.73.jar` → `1.0.73`, else null. */
function tyxenVer(filename: string): string | null {
  const m = /^tyxen-(.+)\.jar$/i.exec(filename.trim());
  return m ? m[1] : null;
}

/**
 * Copy the bundled jar into the instance's mods/ — with AUTO-UPDATE and a
 * NEVER-DOWNGRADE guard. An instance already running same-or-newer Tyxen
 * is left untouched no matter what this build ships — so an old launcher
 * can never roll a fixed mod back, and two launcher versions agree.
 * Same filename but different bytes (new build) → deleted + reinstalled,
 * so every player always runs the latest bundled mod after pressing Play.
 */
async function injectBundled(instanceId: string, bundledPath: string): Promise<string> {
  const filename = bundledPath.split("/").pop() ?? "tyxen.jar";
  const bundledVer = tyxenVer(filename);
  const res = await fetch(bundledPath);
  if (!res.ok) throw new Error(`bundled mod missing (${res.status})`);
  const bytes = new Uint8Array(await res.arrayBuffer());
  const { listInstanceFiles, deleteInstanceFile } = await import("./backend");
  // Newest Tyxen already present? Hands off — never downgrade, never
  // rewrite. (This is what stops old/new launcher builds fighting.)
  try {
    const files = await listInstanceFiles(instanceId, "mods");
    let best: string | null = null;
    for (const f of files) {
      if (f.isDir) continue;
      const v = tyxenVer(f.name);
      if (v && (!best || cmpVer(v, best) > 0)) best = v;
    }
    if (best && bundledVer && cmpVer(best, bundledVer) >= 0 && best !== bundledVer) {
      return files.find((f) => !f.isDir && tyxenVer(f.name) === best)!.name;
    }
    if (best && bundledVer && cmpVer(best, bundledVer) >= 0) {
      // Same version present — refresh only if bytes differ (hotfix build).
      const cur = files.find((f) => !f.isDir && f.name.toLowerCase() === filename.toLowerCase());
      if (cur && cur.size === bytes.byteLength) return cur.name;
    }
  } catch {
    /* missing mods dir — handled below */
  }
  // Purge STALE jars first — otherwise the player keeps running last
  // month's mod forever. Retired tyx-client/swiftclient jars go too.
  // (Newer-or-equal Tyxen is preserved by the guard above.)
  try {
    const files = await listInstanceFiles(instanceId, "mods");
    for (const f of files) {
      const n = f.name.toLowerCase();
      if (f.isDir) continue;
      const staleSwift = n.startsWith("swiftclient-") && n !== filename.toLowerCase();
      const v = tyxenVer(f.name);
      const staleTyxen = v !== null && bundledVer !== null && cmpVer(v, bundledVer) < 0;
      const legacyTyx = LEGACY_PREFIXES.some((p) => n.startsWith(p));
      const outdatedSameName = n === filename.toLowerCase() && f.size !== bytes.byteLength;
      if (staleSwift || staleTyxen || legacyTyx || outdatedSameName) {
        try {
          await deleteInstanceFile(instanceId, "mods", f.name);
        } catch {
          /* keep going */
        }
      }
    }
  } catch {
    /* missing mods dir — created below */
  }
  if (await hasPrefix(instanceId, filename.toLowerCase().replace(/\.jar$/, ""))) return filename;
  const { appDataDir, join } = await import("@tauri-apps/api/path");
  const { writeFile, mkdir, exists } = await import("@tauri-apps/plugin-fs");
  const dir = await join(await appDataDir(), "tyx", "instances", instanceId, "mods");
  if (!(await exists(dir))) await mkdir(dir, { recursive: true });
  await writeFile(await join(dir, filename), bytes);
  return filename;
}

/** Latest compatible Fabric API from Modrinth (idempotent, SHA1-verified). */
async function ensureFabricApi(instanceId: string, mcVersion: string): Promise<void> {
  // Dedupe first: two Fabric API jars (e.g. 0.141.4 + 0.141.6) load
  // unpredictably — keep the newest active one, drop the rest.
  try {
    const { listInstanceFiles, deleteInstanceFile } = await import("./backend");
    const jars = (await listInstanceFiles(instanceId, "mods"))
      .filter((f) => !f.isDir && /^fabric-api-.*\.jar$/i.test(f.name))
      .sort((a, b) => cmpVer(verOf(a.name), verOf(b.name)));
    for (const stale of jars.slice(0, -1)) {
      try { await deleteInstanceFile(instanceId, "mods", stale.name); } catch { /* keep going */ }
    }
    if (jars.length > 1) {
      const { markInstalledFor } = await import("./modrinth");
      markInstalledFor(instanceId, "fabric-api", jars[jars.length - 1].name);
    }
  } catch {
    /* missing mods dir — created by install below */
  }
  if (await hasPrefix(instanceId, "fabric-api-")) return;
  const { getProjectVersions, pickFile } = await import("./modrinth");
  const { downloadToInstance } = await import("./backend");
  const vs = await getProjectVersions("fabric-api", {
    loaders: ["fabric"],
    gameVersions: [mcVersion],
  });
  const v = vs[0];
  if (!v) throw new Error("no Fabric API for " + mcVersion);
  const f = pickFile(v);
  if (!f) throw new Error("no Fabric API file");
  await downloadToInstance(instanceId, "mods", f.url, f.filename, f.hashes?.sha1);
  const { markInstalledFor } = await import("./modrinth");
  const { setModMeta } = await import("./mod-meta");
  markInstalledFor(instanceId, "fabric-api", f.filename);
  setModMeta(instanceId, "P7dR8mSH" /* fabric-api */, v, f.filename);
}

/** Where the launcher checks for Tyxen updates (Modrinth-style manifest). */
const UPDATE_MANIFEST = "http://api.tyxen.space:11789/api/tyxen/latest";

export interface TyxenUpdate {
  version: string;
  changelog: string;
  url: string;
}

/** Installed tyxen version (from mods/tyxen-*.jar), or null if absent. */
export async function installedTyxenVersion(instanceId: string): Promise<string | null> {
  const { listInstanceFiles } = await import("./backend");
  try {
    const files = await listInstanceFiles(instanceId, "mods");
    for (const f of files) {
      const m = /^tyxen-(.+)\.jar$/i.exec(f.name);
      if (m && !f.isDir) return m[1];
    }
  } catch {
    /* missing mods dir */
  }
  return null;
}

/** Check the update manifest — returns update info if a newer version exists. */
export async function checkTyxenUpdate(instanceId: string): Promise<TyxenUpdate | null> {
  try {
    const { tyxenFetch } = await import("./tyxen-http");
    const res = await tyxenFetch("/api/tyxen/latest");
    if (!res.ok) return null;
    const latest = (await res.json()) as { version?: string; url?: string; changelog?: string };
    if (!latest?.version) return null;
    const installed = await installedTyxenVersion(instanceId);
    if (installed && cmpVer(latest.version, installed) <= 0) return null;
    return { version: latest.version, changelog: latest.changelog ?? "", url: latest.url ?? "" };
  } catch {
    return null;
  }
}

/** Download + install a Tyxen update (purges the old jar). */
export async function updateTyxen(instanceId: string, info: TyxenUpdate): Promise<string> {
  const { listInstanceFiles, deleteInstanceFile, downloadToInstance } = await import("./backend");
  const files = await listInstanceFiles(instanceId, "mods").catch(() => []);
  for (const f of files) {
    if (!f.isDir && f.name.toLowerCase().startsWith("tyxen-")) {
      try {
        await deleteInstanceFile(instanceId, "mods", f.name);
      } catch {
        /* keep going */
      }
    }
  }
  const base = UPDATE_MANIFEST.replace(/\/api\/tyxen\/latest$/, "");
  const url = info.url.startsWith("http") ? info.url : base + info.url;
  const filename = `tyxen-${info.version}.jar`;
  await downloadToInstance(instanceId, "mods", url, filename);
  return filename;
}

/** `fabric-api-0.141.6+1.21.11.jar` → `0.141.6+1.21.11` (for cmpVer). */
function verOf(filename: string): string {
  return filename.replace(/^fabric-api-/i, "").replace(/\.jar$/i, "");
}

function cmpVer(a: string, b: string): number {  const pa = a.split(".").map((x) => parseInt(x, 10) || 0);
  const pb = b.split(".").map((x) => parseInt(x, 10) || 0);
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const d = (pa[i] ?? 0) - (pb[i] ?? 0);
    if (d !== 0) return d;
  }
  return 0;
}

/**
 * Ensure the Tyxen stack for this instance. Update flow (Modrinth-style):
 * 1. Ask the update manifest for the newest version.
 * 2. If installed is older/missing → download it (purges the old one).
 * 3. Offline or manifest down → fall back to the bundled jar.
 * Returns true when the stack is present. Never throws.
 */
export async function ensureTyxMod(
  instanceId: string,
  mcVersion: string,
  loader: string,
  notify: (kind: "info" | "success" | "error", title: string, body?: string) => void,
): Promise<boolean> {
  const isFabric = loader.toLowerCase() === "fabric";
  if (!isFabric) return false; // vanilla/forge: nothing to inject, stay silent
  const bundled = tyxModFor(mcVersion);
  if (!bundled) {
    return false; // No client mod ships — vanilla + user mods, stay silent.
  }
  const { isTauri } = await import("./tauri");
  if (!isTauri) return false; // desktop only — browser can't write mods/
  try {
    // 1) update check — newer on server?
    try {
      const { tyxenFetch } = await import("./tyxen-http");
      const res = await tyxenFetch("/api/tyxen/latest");
      if (res.ok) {
        const latest = (await res.json()) as { version?: string; url?: string; changelog?: string };
        if (latest?.version && latest?.url) {
          const { listInstanceFiles, deleteInstanceFile, downloadToInstance } = await import("./backend");
          const files = await listInstanceFiles(instanceId, "mods").catch(() => []);
          let installed: string | null = null;
          for (const f of files) {
            const m = /^tyxen-(.+)\.jar$/i.exec(f.name);
            if (m && !f.isDir) installed = m[1];
          }
          if (!installed || cmpVer(latest.version, installed) > 0) {
            // purge old tyxen jars, install the new one
            for (const f of files) {
              if (!f.isDir && f.name.toLowerCase().startsWith("tyxen-")) {
                try {
                  await deleteInstanceFile(instanceId, "mods", f.name);
                } catch {
                  /* keep going */
                }
              }
            }
            const base = UPDATE_MANIFEST.replace(/\/api\/tyxen\/latest$/, "");
            const url = latest.url.startsWith("http") ? latest.url : base + latest.url;
            const filename = `tyxen-${latest.version}.jar`;
            await downloadToInstance(instanceId, "mods", url, filename);
            await ensureFabricApi(instanceId, mcVersion);
            notify("success", `Tyxen updated to ${latest.version}`, latest.changelog ?? "Press Play — new version ready.");
            return true;
          }
        }
      }
    } catch {
      /* offline or server down — fall through to bundled */
    }
    // 2) bundled fallback (same-name auto-update inside)
    const jar = await injectBundled(instanceId, bundled);
    await ensureFabricApi(instanceId, mcVersion);
    notify("success", "Tyxen ready", `${jar} + Fabric API — Right Shift in-game opens the menu.`);
    return true;
  } catch (e) {
    notify("info", "Tyxen skipped", e instanceof Error ? e.message : String(e));
    return false;
  }
}
