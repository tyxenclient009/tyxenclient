/**
 * Typed bridge to the Rust backend with full browser-preview fallbacks.
 *
 * Contract: every function works in BOTH environments.
 *   - Tauri → real disk/process/Java (src-tauri/src/*.rs).
 *   - Browser → localStorage-backed simulation (flagged `simulated: true`).
 * Callers never branch on `isTauri` — they just use this module.
 */
import { isTauri, invokeOr } from "./tauri";

export interface InstanceSettings {
  ramMb: number;
  javaPath: string;
  jvmArgs: string;
  resW: number;
  resH: number;
}

export interface Instance {
  id: string;
  name: string;
  version: string;
  loader: string;
  loaderVersion: string;
  icon: string;
  created: number;
  lastPlayed?: number;
  path: string;
  settings: InstanceSettings;
}

export interface FileEntry {
  name: string;
  size: number;
  modified: number;
  isDir: boolean;
}

export interface JavaInstall {
  path: string;
  version: string;
  source: string;
}

export interface LaunchRequest {
  instanceId: string;
  username: string;
  uuid: string;
  token: string;
  ramMb: number;
  javaPath: string;
  jvmArgs: string;
  resW: number;
  resH: number;
  /** Direct-connect server host (empty = main menu). */
  server?: string;
  /** Direct-connect port (0/undefined = 25565). */
  port?: number;
  /** Saved servers → merged into servers.dat (in-game Multiplayer list). */
  servers?: { name: string; host: string; port: number }[];
}

export interface LaunchPreview {
  java: string;
  args: string[];
  workdir: string;
  ready: boolean;
  summary: string;
}

const LS_INST = "tyx:instances";
const LS_JAVA: JavaInstall[] = [
  { path: "java", version: "auto-detect (simulated)", source: "path" },
];

function lsLoad(): Instance[] {
  try {
    return JSON.parse(localStorage.getItem(LS_INST) ?? "[]") as Instance[];
  } catch {
    return [];
  }
}
function lsSave(list: Instance[]) {
  localStorage.setItem(LS_INST, JSON.stringify(list));
}

const rustInstance = (m: Record<string, unknown>): Instance => ({
  id: m.id as string,
  name: m.name as string,
  version: m.version as string,
  loader: m.loader as string,
  loaderVersion: (m.loaderVersion as string) ?? "",
  icon: (m.icon as string) ?? "pickaxe",
  created: m.created as number,
  lastPlayed: (m.lastPlayed as number | undefined) ?? undefined,
  path: m.path as string,
  settings: {
    ramMb: (m.settings as InstanceSettings)?.ramMb ?? 4096,
    javaPath: (m.settings as InstanceSettings)?.javaPath ?? "",
    jvmArgs: (m.settings as InstanceSettings)?.jvmArgs ?? "",
    resW: (m.settings as InstanceSettings)?.resW ?? 1280,
    resH: (m.settings as InstanceSettings)?.resH ?? 720,
  },
});

// ── Instances ────────────────────────────────────────────────────────────────

export async function listInstances(): Promise<Instance[]> {
  if (!isTauri) return lsLoad().sort((a, b) => b.created - a.created);
  const raw = await invokeOr<Record<string, unknown>[]>("instances_list", {}, []);
  return raw.map(rustInstance);
}

export async function createInstance(input: {
  name: string;
  version: string;
  loader: string;
  loaderVersion?: string;
  icon: string;
}): Promise<Instance> {
  const loaderVersion = input.loaderVersion?.trim() || (input.loader === "Vanilla" ? "" : "recommended");
  if (!isTauri) {
    const now = Date.now();
    const inst: Instance = {
      id: `${input.name.toLowerCase().replace(/[^a-z0-9]+/g, "-")}-${now % 1_000_000}`,
      name: input.name,
      version: input.version,
      loader: input.loader,
      icon: input.icon,
      created: now,
      path: `<browser>/${input.name}`,
      loaderVersion,
      settings: { ramMb: 4096, javaPath: "", jvmArgs: "", resW: 1280, resH: 720 },
    };
    const list = lsLoad();
    list.unshift(inst);
    lsSave(list);
    return inst;
  }
  const raw = await invokeOr<Record<string, unknown>>("instance_create", {
    name: input.name,
    version: input.version,
    loader: input.loader,
    icon: input.icon,
  }, {});
  // loader_record writes loader.json; merge its version back so the returned
  // meta doesn't diverge (previously loaderVersion came back as "").
  try {
    const rec = await invokeOr<{ loaderVersion?: string; loader_version?: string } | null>(
      "loader_record",
      { id: (raw as Record<string, string>).id, loader: input.loader, mc_version: input.version, loader_version: loaderVersion },
      null,
    );
    const v = rec?.loaderVersion ?? rec?.loader_version ?? loaderVersion;
    const merged = { ...(raw as Record<string, unknown>), loaderVersion: v };
    return rustInstance(merged);
  } catch (e) {
    // Non-fatal: keep requested version locally so UI stays truthful.
    console.warn("[tyx] loader_record failed, using requested version:", e);
    return rustInstance({ ...(raw as Record<string, unknown>), loaderVersion });
  }
}

export async function deleteInstance(id: string): Promise<void> {
  if (!isTauri) {
    lsSave(lsLoad().filter((i) => i.id !== id));
    return;
  }
  await invokeOr("instance_delete", { id }, null);
}

export async function updateInstanceSettings(id: string, settings: InstanceSettings): Promise<Instance> {
  if (!isTauri) {
    const list = lsLoad().map((i) =>
      i.id === id ? { ...i, settings, lastPlayed: i.lastPlayed } : i,
    );
    lsSave(list);
    const inst = list.find((i) => i.id === id);
    if (!inst) throw new Error("Instance not found");
    return inst;
  }
  const raw = await invokeOr<Record<string, unknown> | null>("instance_update_settings", { id, settings }, null);
  // Rust returns the updated InstanceMeta — propagate it so callers stay in sync.
  if (raw && typeof raw === "object" && "id" in raw) return rustInstance(raw);
  const fresh = (await listInstances()).find((i) => i.id === id);
  if (!fresh) throw new Error("Instance not found after settings update");
  return fresh;
}

export async function listInstanceFiles(id: string, kind: string): Promise<FileEntry[]> {
  if (!isTauri) {
    // Browser demo content so tabs never look broken.
    if (kind === "mods") return [{ name: "sodium-1.20.4.jar", size: 2_104_331, modified: Date.now(), isDir: false }];
    return [];
  }
  return invokeOr<FileEntry[]>("instance_list_files", { id, kind }, []);
}

export async function deleteInstanceFile(id: string, kind: string, name: string): Promise<void> {
  if (!isTauri) return;
  await invokeOr("instance_delete_file", { id, kind, name }, null);
}

/** Enable/disable a mod (toggles `.disabled`). Returns the new file name. */
export async function toggleInstanceFile(id: string, kind: string, name: string): Promise<string> {
  if (!isTauri) return name;
  return invokeOr<string>("instance_toggle_file", { id, kind, name }, name);
}

export async function openInstanceFolder(id: string, subdir?: string): Promise<string> {
  if (!isTauri) throw new Error("Open folder needs the desktop app — run the .exe, not the browser preview.");
  // Native Rust opener (Explorer/Finder): no shell-plugin scope issues,
  // errors surface to the caller so buttons can toast them.
  const path = await invokeOr<string>(
    "open_instance_path",
    { id, subdir: subdir ?? null, filename: null },
    "",
  );
  if (!path) throw new Error("File manager didn't open — try again.");
  return path;
}

/** Reveal one file in Explorer/Finder (falls back to its folder). */
export async function revealInstanceFile(id: string, subdir: string, filename: string): Promise<string> {
  if (!isTauri) throw new Error("Reveal needs the desktop app.");
  const path = await invokeOr<string>(
    "open_instance_path",
    { id, subdir, filename },
    "",
  );
  if (!path) throw new Error("File manager didn't open — try again.");
  return path;
}

// ── Custom instance icons ─────────────────────────────────────────────────
// Icon values: builtin id ("pickaxe"), legacy emoji, `data:image/…` (browser
// uploads), or `file:<name>` (Tauri — file lives in the instance dir).

/**
 * Let the user pick an image. Returns `file:<abs path>` (Tauri, resolved +
 * copied by `instance_set_icon`) or a `data:image/…` URL (browser preview),
 * or null when cancelled. Throws on oversized files.
 */
export async function pickImageFile(): Promise<string | null> {
  if (!isTauri) {
    return new Promise((resolve, reject) => {
      const el = document.createElement("input");
      el.type = "file";
      el.accept = "image/png,image/jpeg,image/webp";
      el.onchange = () => {
        const f = el.files?.[0];
        if (!f) {
          resolve(null);
          return;
        }
        if (f.size > 1024 * 1024) {
          reject(new Error("Image too large (max 1 MB in browser preview)."));
          return;
        }
        const r = new FileReader();
        r.onload = () => resolve(r.result as string);
        r.onerror = () => resolve(null);
        r.readAsDataURL(f);
      };
      el.click();
    });
  }
  const { open } = await import("@tauri-apps/plugin-dialog");
  const sel = await open({
    multiple: false,
    directory: false,
    filters: [{ name: "Images", extensions: ["png", "jpg", "jpeg", "webp"] }],
  });
  if (!sel || Array.isArray(sel)) return null;
  return `file:${sel}`;
}

/** Persist an icon value (builtin id, emoji, data-URL or `file:` upload). */
export async function setInstanceIcon(id: string, icon: string): Promise<Instance> {
  if (!isTauri) {
    if (!icon.startsWith("data:image/") && icon.length > 32) throw new Error("Invalid icon");
    const list = lsLoad().map((i) => (i.id === id ? { ...i, icon } : i));
    lsSave(list);
    const inst = list.find((i) => i.id === id);
    if (!inst) throw new Error("Instance not found");
    return inst;
  }
  const raw = await invokeOr<Record<string, unknown>>("instance_set_icon", { id, icon }, {});
  return rustInstance(raw);
}

/**
 * Resolve an icon value to a viewable image URL (or null).
 * `file:` icons come from the Rust side as data-URLs — no asset-protocol
 * scope needed, never stale. `data:` passes through.
 */
export async function resolveInstanceIcon(icon: string, instanceId?: string): Promise<string | null> {
  if (icon.startsWith("data:image/")) return icon;
  if (!icon.startsWith("file:")) return null;
  if (!isTauri || !instanceId) return null;
  try {
    const data = await invokeOr<string>("instance_icon_data", { id: instanceId }, "");
    return data.startsWith("data:image/") ? data : null;
  } catch {
    return null;
  }
}

/**
 * Raw bytes of one instance file as a data-URL (screenshots viewer).
 * Images only (png/jpg/webp, ≤8 MB) — throws otherwise, including in the
 * browser preview.
 */
export async function instanceFileData(id: string, kind: string, name: string): Promise<string> {
  if (!isTauri) throw new Error("File preview needs the desktop app.");
  const data = await invokeOr<string>("instance_file_data", { id, kind, name }, "");
  if (!data.startsWith("data:image/")) throw new Error(data || "Couldn't load that file.");
  return data;
}

// ── Java / launch ────────────────────────────────────────────────────────────

export async function detectJava(): Promise<JavaInstall[]> {
  if (!isTauri) return LS_JAVA;
  return invokeOr<JavaInstall[]>("java_detect", {}, []);
}

/** Find-or-download a Java runtime for `major`. Self-healing: Adoptium API (retried), then GitHub release, then Corretto. Auto-installs on demand. */
export async function ensureJava(major: number): Promise<JavaInstall> {
  if (!isTauri) {
    console.info(`[browser-stub] would auto-install Temurin ${major}`);
    return { ...LS_JAVA[0], version: `temurin ${major} (simulated)` };
  }
  return invokeOr<JavaInstall>("java_ensure", { major }, LS_JAVA[0]);
}

/** Game-file download progress (`tyx://assets`). */
export interface AssetsProgress {
  stage: string;
  done_files: number;
  total_files: number;
  done_bytes: number;
  total_bytes: number;
  current: string;
  /** Rolling speed bytes/sec (0 = unknown). */
  speed_bps?: number;
  /** ETA seconds (Number.MAX_SAFE_INTEGER / huge = unknown). */
  eta_secs?: number;
}

export async function onAssetsProgress(cb: (p: AssetsProgress) => void): Promise<() => void> {
  if (!isTauri) return () => {};
  const { listen } = await import("@tauri-apps/api/event");
  const unlisten = await listen<AssetsProgress>("tyx://assets", (e) => cb(e.payload));
  return unlisten;
}
/** Progress of automatic runtime downloads (`tyx://java-dl`). */
export async function onJavaDl(
  cb: (p: { major: number; downloaded: number; total: number }) => void,
): Promise<() => void> {
  if (!isTauri) return () => {};
  const { listen } = await import("@tauri-apps/api/event");
  const unlisten = await listen<{ major: number; downloaded: number; total: number }>(
    "tyx://java-dl",
    (e) => cb(e.payload),
  );
  return unlisten;
}

export async function offlineUuid(username: string): Promise<string> {
  if (!isTauri) {
    // Deterministic fallback (matches Rust shape loosely).
    let h1 = 0x811c9dc5, h2 = 0x01000193;
    const s = `OfflinePlayer:${username}`;
    for (const c of s) {
      h1 = Math.imul(h1 ^ c.codePointAt(0)!, 16777619) >>> 0;
      h2 = Math.imul(h2 + c.codePointAt(0)!, 31) >>> 0;
    }
    const hex = (n: number) => n.toString(16).padStart(8, "0");
    return `${hex(h1)}-${hex(h2).slice(0, 4)}-3${hex(h1).slice(1, 4)}-8${hex(h2).slice(1, 4)}-${hex(h1)}${hex(h2).slice(0, 4)}`;
  }
  return invokeOr<string>("offline_uuid", { username }, "");
}

export async function launchPreview(req: LaunchRequest): Promise<LaunchPreview> {
  if (!isTauri) {
    const java = req.javaPath || LS_JAVA[0].path;
    return {
      java,
      args: [`-Xmx${req.ramMb}M`, "--username", req.username, "--uuid", req.uuid, "--version", "(browser preview)"],
      workdir: `<browser>/${req.instanceId}`,
      ready: false,
      summary: `${req.instanceId} as ${req.username} (simulated)`,
    };
  }
  return invokeOr<LaunchPreview>("launch_preview", { req }, {
    java: "", args: [], workdir: "", ready: false, summary: "",
  });
}

export async function launchGame(req: LaunchRequest): Promise<number> {
  if (!isTauri) {
    // Browser simulation — emit the same event shape the Rust side emits.
    const lines = [
      "[tyx] starting simulated client…",
      "[LWJGL] Version 3.3.3 (browser simulation)",
      `[Auth] session for ${req.username} verified (offline/dev)`,
      "[Client] simulated main-menu reached. Hook up Tauri for a real boot.",
    ];
    lines.forEach((line, i) =>
      setTimeout(() => window.dispatchEvent(new CustomEvent("tyx:game-log", { detail: { stream: "stdout", line } })), 300 * (i + 1)),
    );
    // Touch lastPlayed in the LS store.
    lsSave(lsLoad().map((x) => (x.id === req.instanceId ? { ...x, lastPlayed: Date.now() } : x)));
    return 0;
  }
  // Rust spawns async and returns a placeholder pid (0) — real status flows
  // via tyx://game-log. Return it so callers can distinguish dispatch vs failure.
  const pid = await invokeOr<number>("launch_game", { req }, 0);
  await invokeOr("instance_touch", { id: req.instanceId }, null).catch((e) => {
    console.warn("[tyx] instance_touch failed:", e);
  });
  return typeof pid === "number" ? pid : 0;
}

/** Stop game(s): one instance when `id` is given, everything running when omitted. */
export async function stopGame(id?: string | null): Promise<string> {
  if (!isTauri) throw new Error("Stop needs the desktop app.");
  return invokeOr<string>("stop_game", { instanceId: id ?? null }, "");
}

/** Subscribe to game-log events from BOTH backends (Tauri emit + browser CustomEvent). */
export async function onGameLog(cb: (log: { stream: string; line: string; instanceId?: string }) => void): Promise<() => void> {
  if (!isTauri) {
    const handler = (e: Event) => cb((e as CustomEvent).detail);
    window.addEventListener("tyx:game-log", handler);
    return () => window.removeEventListener("tyx:game-log", handler);
  }
  const { listen } = await import("@tauri-apps/api/event");
  const unlisten = await listen<{ stream: string; line: string; instance_id?: string }>("tyx://game-log", (e) =>
    cb({ stream: e.payload.stream, line: e.payload.line, instanceId: e.payload.instance_id || undefined }),
  );
  return unlisten;
}

// ── Mod file download → instance mods folder ────────────────────────────────
// Uses the http + fs plugins (no new Rust deps). SHA1 is verified when the
// Modrinth hash is known; mismatches abort before touching an existing file.
// Retries transient failures (3 attempts, backoff) so public Wi-Fi doesn't
// cause "Install failed" spam.

async function fetchBytes(url: string): Promise<Uint8Array> {
  const { fetch } = await import("@tauri-apps/plugin-http");
  let lastErr = "";
  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const res = await fetch(url, { headers: { "User-Agent": "TyxLauncher/0.1.0" } });
      if (!res.ok) throw new Error(`Download failed (${res.status})`);
      return new Uint8Array(await res.arrayBuffer());
    } catch (e) {
      lastErr = e instanceof Error ? e.message : String(e);
      if (attempt < 2) await new Promise((r) => setTimeout(r, 600 * (attempt + 1)));
    }
  }
  throw new Error(lastErr || "Download failed");
}

export async function downloadToInstance(
  instanceId: string,
  kind: "mods" | "resourcepacks" | "shaderpacks",
  url: string,
  filename: string,
  sha1?: string,
  onProgress?: (pct: number) => void,
): Promise<string> {
  const safe = filename.replace(/[\\/]/g, "_");
  if (!isTauri) {
    onProgress?.(100);
    console.info(`[browser-stub] would download ${url} → ${kind}/${safe}`);
    return `<browser>/${instanceId}/${kind}/${safe}`;
  }
  const buf = await fetchBytes(url);

  if (sha1) {
    const digest = await crypto.subtle.digest("SHA-1", buf as BufferSource);
    const hex = [...new Uint8Array(digest)].map((b) => b.toString(16).padStart(2, "0")).join("");
    if (hex !== sha1.toLowerCase()) throw new Error(`SHA1 mismatch for ${safe} — retry install`);
  }
  const { appDataDir, join } = await import("@tauri-apps/api/path");
  const { writeFile, mkdir, exists, stat, rename } = await import("@tauri-apps/plugin-fs");
  const dir = await join(await appDataDir(), "tyx", "instances", instanceId, kind);
  if (!(await exists(dir))) await mkdir(dir, { recursive: true });
  const dest = await join(dir, safe);
  // Skip re-download when the file already exists with the exact byte size
  // (Modrinth-app parity: idempotent reinstalls).
  try {
    if (await exists(dest)) {
      const st = await stat(dest);
      if (st.size === buf.byteLength) {
        onProgress?.(100);
        return dest;
      }
    }
  } catch {
    /* stat failed — fall through to write */
  }
  // Atomic write: tmp + rename so a crash never leaves a half-jar.
  const tmp = `${dest}.tmp`;
  await writeFile(tmp, buf);
  try {
    await rename(tmp, dest);
  } catch {
    // rename not available on some backends — fall back to direct write.
    await writeFile(dest, buf);
  }
  onProgress?.(100);
  return dest;
}

/**
 * Modrinth-parity install: downloads the file + its REQUIRED dependencies
 * (transitive, cycle-safe, max 10 total) into the same instance folder.
 * Returns all installed paths. A failed dep never fails the main install.
 */
export async function installModWithDeps(
  instanceId: string,
  kind: "mods" | "resourcepacks" | "shaderpacks",
  version: { files: { url: string; filename: string; hashes: { sha1: string } }[]; dependencies: { project_id: string | null; version_id: string | null; dependency_type: string }[]; game_versions: string[]; loaders: string[] },
  opts?: { gameVersion?: string; loader?: string },
): Promise<string[]> {
  const { getProjectVersions, pickFile } = await import("./modrinth");
  const { markInstalled } = await import("./modrinth");
  const out: string[] = [];
  const seen = new Set<string>();
  const primary = version.files.find((f) => (f as { primary?: boolean }).primary) ?? version.files.find((f) => /\.(jar|zip)$/i.test(f.filename)) ?? version.files[0];
  if (!primary) throw new Error("No downloadable file on this version");
  out.push(await downloadToInstance(instanceId, kind, primary.url, primary.filename, primary.hashes?.sha1));

  const queue: { projectId: string; depth: number }[] = (version.dependencies ?? [])
    .filter((d) => d.dependency_type === "required" && d.project_id)
    .map((d) => ({ projectId: d.project_id as string, depth: 1 }));
  while (queue.length > 0 && out.length < 10) {
    const { projectId, depth } = queue.shift()!;
    const key = projectId.toLowerCase();
    if (seen.has(key) || depth > 3) continue;
    seen.add(key);
    try {
      const vs = await getProjectVersions(projectId, {
        loaders: opts?.loader ? [opts.loader] : undefined,
        gameVersions: opts?.gameVersion ? [opts.gameVersion] : undefined,
      });
      const dv = vs[0];
      if (!dv) continue;
      const df = pickFile(dv);
      if (!df) continue;
      out.push(await downloadToInstance(instanceId, kind, df.url, df.filename, df.hashes?.sha1));
      markInstalled(projectId);
      // Transitive required deps.
      for (const sub of (dv.dependencies ?? []).filter((d) => d.dependency_type === "required" && d.project_id)) {
        if (out.length + queue.length >= 10) break;
        queue.push({ projectId: sub.project_id as string, depth: depth + 1 });
      }
    } catch {
      /* a failed dep must never fail the main install */
    }
  }
  return out;
}

// ── Modpack install (.mrpack → fresh instance, Modrinth-App parity) ────────
// The Rust side downloads the pack, creates an isolated instance from the
// pack's own name/version/loader, fetches every client file (sha1-verified)
// and applies overrides. Progress streams over tyx://assets + game-log.

export async function installModpack(projectId: string, versionId?: string): Promise<Instance> {
  if (!isTauri) throw new Error("Modpack install needs the desktop app (browser can't write instances).");
  const { getProjectVersions, pickFile } = await import("./modrinth");
  const vs = await getProjectVersions(projectId);
  const v = (versionId ? vs.find((x) => x.id === versionId) : undefined) ?? vs[0];
  if (!v) throw new Error("No versions published");
  const file = pickFile(v);
  if (!file || !/\.mrpack$/i.test(file.filename)) throw new Error("That version has no .mrpack file");
  const raw = await invokeOr<Record<string, unknown>>(
    "modpack_install",
    { url: file.url, filename: file.filename, path: null },
    {},
  );
  if (!raw || !raw.id) throw new Error("Modpack install returned nothing");
  return rustInstance(raw);
}

/** Install a LOCAL .mrpack file (drag-drop / file picker imports). */
export async function importModpackFile(path: string): Promise<Instance> {
  if (!isTauri) throw new Error("Modpack import needs the desktop app.");
  const name = path.split(/[\\/]/).pop() ?? "pack.mrpack";
  const raw = await invokeOr<Record<string, unknown>>(
    "modpack_install",
    { url: "", filename: name, path },
    {},
  );
  if (!raw || !raw.id) throw new Error("Modpack import returned nothing");
  return rustInstance(raw);
}

/** File picker for .mrpack (null when cancelled). */
export async function pickModpackFile(): Promise<string | null> {
  if (!isTauri) return null;
  const { open } = await import("@tauri-apps/plugin-dialog");
  const sel = await open({
    multiple: false,
    directory: false,
    filters: [{ name: "Modpack", extensions: ["mrpack"] }],
  });
  if (!sel || Array.isArray(sel)) return null;
  return sel;
}

/**
 * Export an instance as .mrpack (save dialog + Rust zip builder).
 * Returns the destination path, or null when cancelled.
 */
export async function exportInstancePack(id: string, suggestedName: string): Promise<string | null> {
  if (!isTauri) throw new Error("Pack export needs the desktop app.");
  const { save } = await import("@tauri-apps/plugin-dialog");
  const safe = `${suggestedName.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "") || "modpack"}.mrpack`;
  const dest = await save({
    defaultPath: safe,
    filters: [{ name: "Modpack", extensions: ["mrpack"] }],
  });
  if (!dest) return null;
  return invokeOr<string>("modpack_export", { id, dest }, dest);
}
