/**
 * Skins — player appearance, no third-party service needed.
 *
 * - Offline accounts: pick a custom skin from the library; it is applied
 *   automatically on Play via a generated resource pack inside the instance
 *   (`tyx-skin-pack.zip`, injected into `options.txt`). Pure launcher-side,
 *   works fully offline.
 * - Microsoft accounts: the account's real Mojang skin is fetched
 *   from the session server and used automatically; it can also be saved
 *   into the library.
 *
 * Storage: `<appData>/tyx/skins` on desktop (Tauri fs), localStorage
 * data-URLs in the browser preview.
 */
import { isTauri } from "./tauri";

export type SkinModel = "classic" | "slim";

export interface SkinMeta {
  id: string;
  name: string;
  model: SkinModel;
  created: number;
  /** Browser preview only — desktop resolves bytes from disk on demand. */
  dataUrl?: string;
}

const LS_SKINS = "tyx:skins";
const SKIN_PACK_NAME = "tyx-skin-pack.zip";
const MAX_FILE_BYTES = 2 * 1024 * 1024;

function uid(prefix: string): string {
  return `${prefix}-${Date.now().toString(36)}-${Math.floor(Math.random() * 0xffffff).toString(36)}`;
}

// ── PNG helpers (no deps) ─────────────────────────────────────────────────

function pngSize(bytes: Uint8Array): { w: number; h: number } | null {
  if (bytes.length < 33) return null;
  const sig = [137, 80, 78, 71, 13, 10, 26, 10];
  for (let i = 0; i < 8; i++) if (bytes[i] !== sig[i]) return null;
  const w = (bytes[16] << 24) | (bytes[17] << 16) | (bytes[18] << 8) | bytes[19];
  const h = (bytes[20] << 24) | (bytes[21] << 16) | (bytes[22] << 8) | bytes[23];
  if (w <= 0 || h <= 0 || w > 1024 || h > 1024) return null;
  return { w, h };
}

export function bytesToDataUrl(bytes: Uint8Array, mime = "image/png"): string {
  let bin = "";
  const CH = 0x8000;
  for (let i = 0; i < bytes.length; i += CH) {
    bin += String.fromCharCode(...bytes.subarray(i, i + CH));
  }
  return `data:${mime};base64,${btoa(bin)}`;
}

export function dataUrlToBytes(url: string): Uint8Array {
  const b64 = url.split(",", 2)[1] ?? "";
  const bin = atob(b64);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}

/** Classic (4px arms) vs slim (3px arms): sample the inner-arm strip that
 * only exists on classic skins. Falls back to classic on any error. */
export async function detectSkinModel(dataUrl: string): Promise<SkinModel> {
  try {
    const img = await new Promise<HTMLImageElement>((resolve, reject) => {
      const el = new Image();
      el.onload = () => resolve(el);
      el.onerror = () => reject(new Error("undecodable"));
      el.src = dataUrl;
    });
    if (img.width !== 64 || (img.height !== 64 && img.height !== 32)) return "classic";
    const c = document.createElement("canvas");
    c.width = img.width;
    c.height = img.height;
    const ctx = c.getContext("2d", { willReadFrequently: true });
    if (!ctx) return "classic";
    ctx.drawImage(img, 0, 0);
    // Classic right-arm inner-top occupies x 48–52; slim arms end at x ~47.
    const d = ctx.getImageData(50, 16, 2, 4).data;
    let transparent = 0;
    for (let i = 3; i < d.length; i += 4) if (d[i] < 128) transparent++;
    return transparent >= 4 ? "slim" : "classic";
  } catch {
    return "classic";
  }
}

// ── Desktop fs helpers ────────────────────────────────────────────────────

async function skinsDir(): Promise<string> {
  const { appDataDir, join } = await import("@tauri-apps/api/path");
  return join(await appDataDir(), "tyx", "skins");
}

async function instanceDirFs(instanceId: string): Promise<string> {
  const { appDataDir, join } = await import("@tauri-apps/api/path");
  return join(await appDataDir(), "tyx", "instances", instanceId);
}

// ── Library CRUD ──────────────────────────────────────────────────────────

function lsLoad<T>(key: string): T[] {
  try {
    return JSON.parse(localStorage.getItem(key) ?? "[]") as T[];
  } catch {
    return [];
  }
}

export async function listSkins(): Promise<SkinMeta[]> {
  if (!isTauri) return lsLoad<SkinMeta>(LS_SKINS).sort((a, b) => b.created - a.created);
  const { readDir, readTextFile } = await import("@tauri-apps/plugin-fs");
  const dir = await skinsDir();
  let entries: { name?: string }[] = [];
  try {
    entries = await readDir(dir);
  } catch {
    return [];
  }
  const out: SkinMeta[] = [];
  for (const e of entries) {
    const name = e.name ?? "";
    if (!name.endsWith(".json")) continue;
    try {
      const raw = await readTextFile(`${dir}/${name}`);
      out.push(JSON.parse(raw) as SkinMeta);
    } catch {
      /* skip corrupt sidecars */
    }
  }
  return out.sort((a, b) => b.created - a.created);
}

async function saveSkinBytes(name: string, bytes: Uint8Array, model: SkinModel): Promise<SkinMeta> {
  if (bytes.length > MAX_FILE_BYTES) throw new Error("Image too large (max 2 MB).");
  const size = pngSize(bytes);
  if (!size) throw new Error("Not a valid PNG file.");
  if (!(size.w === 64 && (size.h === 64 || size.h === 32))) {
    throw new Error(`Skin must be 64×64 or 64×32 (got ${size.w}×${size.h}).`);
  }
  const id = uid("skin");
  if (!isTauri) {
    const meta: SkinMeta = { id, name, model, created: Date.now(), dataUrl: bytesToDataUrl(bytes) };
    const list = lsLoad<SkinMeta>(LS_SKINS);
    localStorage.setItem(LS_SKINS, JSON.stringify([meta, ...list]));
    return meta;
  }
  const { writeFile, mkdir, exists, writeTextFile } = await import("@tauri-apps/plugin-fs");
  const dir = await skinsDir();
  if (!(await exists(dir))) await mkdir(dir, { recursive: true });
  await writeFile(`${dir}/${id}.png`, bytes);
  const meta: SkinMeta = { id, name, model, created: Date.now() };
  await writeTextFile(`${dir}/${id}.json`, JSON.stringify(meta));
  return meta;
}

export async function fileToBytes(file: File): Promise<Uint8Array> {
  const buf = await file.arrayBuffer();
  return new Uint8Array(buf);
}

/** Validate + store an uploaded skin file. Auto-detects slim vs classic. */
export async function addSkinFile(file: File): Promise<SkinMeta> {
  const bytes = await fileToBytes(file);
  const clean = file.name.replace(/\.[^.]+$/, "").slice(0, 40) || "Custom skin";
  const model = await detectSkinModel(bytesToDataUrl(bytes));
  return saveSkinBytes(clean, bytes, model);
}

export async function loadSkinBytes(meta: SkinMeta): Promise<Uint8Array> {
  if (meta.dataUrl) return dataUrlToBytes(meta.dataUrl);
  if (!isTauri) throw new Error("Skin data missing.");
  const { readFile } = await import("@tauri-apps/plugin-fs");
  return readFile(`${await skinsDir()}/${meta.id}.png`);
}

export async function skinDataUrl(meta: SkinMeta): Promise<string> {
  if (meta.dataUrl) return meta.dataUrl;
  return bytesToDataUrl(await loadSkinBytes(meta));
}

export async function deleteSkin(meta: SkinMeta): Promise<void> {
  if (!isTauri) {
    localStorage.setItem(LS_SKINS, JSON.stringify(lsLoad<SkinMeta>(LS_SKINS).filter((s) => s.id !== meta.id)));
    return;
  }
  const { remove } = await import("@tauri-apps/plugin-fs");
  const dir = await skinsDir();
  await remove(`${dir}/${meta.id}.png`).catch(() => {});
  await remove(`${dir}/${meta.id}.json`).catch(() => {});
}

/** Persist a model change (classic ↔ slim), desktop sidecar + browser LS. */
export async function updateSkinModel(meta: SkinMeta, model: SkinModel): Promise<void> {  if (!isTauri) {
    localStorage.setItem(
      LS_SKINS,
      JSON.stringify(lsLoad<SkinMeta>(LS_SKINS).map((s) => (s.id === meta.id ? { ...s, model } : s))),
    );
    return;
  }
  const { writeTextFile } = await import("@tauri-apps/plugin-fs");
  await writeTextFile(`${await skinsDir()}/${meta.id}.json`, JSON.stringify({ ...meta, model }));
}

/** Rename a library skin (sidecar / LS meta). */
export async function updateSkinName(meta: SkinMeta, name: string): Promise<void> {
  const clean = name.trim().slice(0, 40) || meta.name;
  if (!isTauri) {
    localStorage.setItem(
      LS_SKINS,
      JSON.stringify(lsLoad<SkinMeta>(LS_SKINS).map((s) => (s.id === meta.id ? { ...s, name: clean } : s))),
    );
    return;
  }
  const { writeTextFile } = await import("@tauri-apps/plugin-fs");
  await writeTextFile(`${await skinsDir()}/${meta.id}.json`, JSON.stringify({ ...meta, name: clean }));
}

/** Swap a library skin's PNG (validated, model re-detected). Keeps id. */
export async function replaceSkinBytes(meta: SkinMeta, bytes: Uint8Array): Promise<SkinModel> {
  if (bytes.length > MAX_FILE_BYTES) throw new Error("Image too large (max 2 MB).");
  const size = pngSize(bytes);
  if (!size) throw new Error("Not a valid PNG file.");
  if (!(size.w === 64 && (size.h === 64 || size.h === 32))) {
    throw new Error(`Skin must be 64×64 or 64×32 (got ${size.w}×${size.h}).`);
  }
  const model = await detectSkinModel(bytesToDataUrl(bytes));
  if (!isTauri) {
    localStorage.setItem(
      LS_SKINS,
      JSON.stringify(
        lsLoad<SkinMeta>(LS_SKINS).map((s) =>
          s.id === meta.id ? { ...s, dataUrl: bytesToDataUrl(bytes), model } : s,
        ),
      ),
    );
    return model;
  }
  const { writeFile, writeTextFile } = await import("@tauri-apps/plugin-fs");
  const dir = await skinsDir();
  await writeFile(`${dir}/${meta.id}.png`, bytes);
  await writeTextFile(`${dir}/${meta.id}.json`, JSON.stringify({ ...meta, model }));
  return model;
}

// ── Premium (Mojang) textures ─────────────────────────────────────────────

export interface MojangTextures {
  skinUrl?: string;
  slim?: boolean;
}

/**
 * Mojang still serves texture PNGs over plain `http://` — the launcher
 * runs on `https://tauri.localhost`, so raw http image loads die as mixed
 * content (blank previews). textures.minecraft.net answers https fine,
 * so every texture URL is upgraded at the choke points below.
 */
export function httpsTexture(url: string | undefined | null): string | undefined {
  if (!url) return undefined;
  if (url.startsWith("http://")) return `https://${url.slice("http://".length)}`;
  return url;
}

/** Real skin for a premium UUID (undashed). No auth needed. */
export async function fetchMojangTextures(uuidUndashed: string): Promise<MojangTextures> {
  const res = await fetch(`https://sessionserver.mojang.com/session/minecraft/profile/${uuidUndashed}`);
  if (res.status === 404 || res.status === 204) throw new Error("No Minecraft profile found for this account.");
  if (!res.ok) throw new Error(`Profile lookup failed (${res.status}).`);
  const j = (await res.json()) as { properties?: { name: string; value: string }[] };
  const raw = j.properties?.find((p) => p.name === "textures")?.value;
  if (!raw) return {};
  const decoded = JSON.parse(atob(raw)) as {
    textures?: { SKIN?: { url: string; metadata?: { model?: string } } };
  };
  return {
    skinUrl: httpsTexture(decoded.textures?.SKIN?.url),
    slim: decoded.textures?.SKIN?.metadata?.model === "slim",
  };
}

/** Save a premium account skin into the local library. */
export async function saveUrlAsSkin(url: string, name: string): Promise<SkinMeta> {
  const res = await fetch(httpsTexture(url) ?? url, { headers: { "User-Agent": "TyxLauncher/0.1.0" } });
  if (!res.ok) throw new Error(`Skin download failed (${res.status}).`);
  const bytes = new Uint8Array(await res.arrayBuffer());
  const model = await detectSkinModel(bytesToDataUrl(bytes));
  return saveSkinBytes(name.slice(0, 40) || "Account skin", bytes, model);
}

// ── Offline apply: skin resource pack (stored zip, no deps) ───────────────

function crc32Table(): Uint32Array {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
}

const CRC_TABLE = crc32Table();

function crc32(bytes: Uint8Array): number {
  let c = 0xffffffff;
  for (let i = 0; i < bytes.length; i++) c = CRC_TABLE[(c ^ bytes[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function enc(s: string): Uint8Array {
  return new TextEncoder().encode(s);
}

function u16(n: number): Uint8Array {
  return new Uint8Array([n & 0xff, (n >> 8) & 0xff]);
}

function u32(n: number): Uint8Array {
  return new Uint8Array([n & 0xff, (n >> 8) & 0xff, (n >> 16) & 0xff, (n >> 24) & 0xff]);
}

function concat(parts: Uint8Array[]): Uint8Array {
  const total = parts.reduce((a, p) => a + p.length, 0);
  const out = new Uint8Array(total);
  let o = 0;
  for (const p of parts) {
    out.set(p, o);
    o += p.length;
  }
  return out;
}

/** Minimal stored (uncompressed) zip — Minecraft loads these fine. */
function buildStoredZip(files: { name: string; data: Uint8Array }[]): Uint8Array {
  const locals: Uint8Array[] = [];
  const centrals: Uint8Array[] = [];
  let offset = 0;
  for (const f of files) {
    const name = enc(f.name);
    const crc = crc32(f.data);
    const local = concat([
      u32(0x04034b50), u16(20), u16(0x0800), u16(0), u16(0x545e), u16(0x5a54),
      u32(crc), u32(f.data.length), u32(f.data.length), u16(name.length), u16(0), name,
    ]);
    locals.push(local, f.data);
    centrals.push(
      concat([
        u32(0x02014b50), u16(20), u16(20), u16(0x0800), u16(0), u16(0x545e), u16(0x5a54),
        u32(crc), u32(f.data.length), u32(f.data.length), u16(name.length),
        u16(0), u16(0), u16(0), u16(0), u32(0), u32(offset), name,
      ]),
    );
    offset += local.length + f.data.length;
  }
  const central = concat(centrals);
  const localAll = concat(locals);
  const end = concat([
    u32(0x06054b50), u16(0), u16(0), u16(files.length), u16(files.length),
    u32(central.length), u32(localAll.length), u16(0),
  ]);
  return concat([localAll, central, end]);
}

/** Best-effort pack_format for an MC version (a mismatch only shows a
 * warning in the pack screen — packs injected via options.txt still load). */
export function packFormatFor(mcVersion: string): number {
  const parts = mcVersion.split(".").map((p) => parseInt(p, 10));
  // New Mojang year-based scheme (26.x, …): modern format (warning-only on mismatch).
  if (parts[0] >= 21 && !parts.some((n) => Number.isNaN(n))) return 80;
  if (parts[0] !== 1 || parts.some((n) => Number.isNaN(n))) return 15;
  const [, minor = 0, patch = 0] = parts;
  if (minor >= 21) return 34;
  if (minor === 20) return patch >= 5 ? 32 : patch >= 2 ? 18 : 15;
  if (minor === 19) return patch >= 4 ? 13 : patch >= 3 ? 12 : 9;
  if (minor === 18) return 8;
  if (minor === 17) return 7;
  if (minor === 16) return 6;
  if (minor === 15) return 5;
  if (minor <= 14 && minor >= 13) return 4;
  if (minor === 12) return 3;
  return 1;
}

function skinPackZip(skin: Uint8Array, model: SkinModel, mcVersion: string): Uint8Array {
  const mcmeta = enc(
    JSON.stringify({ pack: { pack_format: packFormatFor(mcVersion), description: "Tyx Launcher player skin" } }),
  );
  const files = [
    { name: "pack.mcmeta", data: mcmeta },
    { name: "assets/minecraft/textures/entity/player/wide/steve.png", data: skin },
    { name: "assets/minecraft/textures/entity/player/slim/alex.png", data: skin },
  ];
  void model;
  return buildStoredZip(files);
}

async function readOptionsEntries(dir: string): Promise<{ text: string; packs: string[] }> {
  const { readTextFile } = await import("@tauri-apps/plugin-fs");
  let text = "";
  try {
    text = await readTextFile(`${dir}/options.txt`);
  } catch {
    return { text: "", packs: [] };
  }
  const m = /^resourcePacks:\[(.*)\]$/m.exec(text);
  let packs: string[] = [];
  if (m) {
    try {
      packs = JSON.parse(`[${m[1]}]`) as string[];
    } catch {
      packs = [];
    }
  }
  return { text, packs };
}

async function writeOptionsPacks(dir: string, prev: string, packs: string[]): Promise<void> {
  const { writeTextFile } = await import("@tauri-apps/plugin-fs");
  const line = `resourcePacks:[${packs.map((p) => JSON.stringify(p)).join(",")}]`;
  const next = /^resourcePacks:\[.*\]$/m.test(prev) ? prev.replace(/^resourcePacks:\[.*\]$/m, line) : `${prev}${prev.endsWith("\n") || prev === "" ? "" : "\n"}${line}\n`;
  await writeTextFile(`${dir}/options.txt`, next);
}

/**
 * Apply an offline skin: write the generated pack + pin it in options.txt.
 * Non-Tauri preview is a no-op (logged, like other browser stubs).
 */
export async function applyOfflineSkin(
  instanceId: string,
  mcVersion: string,
  skin: Uint8Array,
  model: SkinModel,
): Promise<void> {
  if (!isTauri) {
    console.info("[browser-stub] would apply offline skin pack");
    return;
  }
  const { writeFile, mkdir, exists } = await import("@tauri-apps/plugin-fs");
  const dir = await instanceDirFs(instanceId);
  const rp = `${dir}/resourcepacks`;
  if (!(await exists(rp))) await mkdir(rp, { recursive: true });
  await writeFile(`${rp}/${SKIN_PACK_NAME}`, skinPackZip(skin, model, mcVersion));
  const { text, packs } = await readOptionsEntries(dir);
  const entry = `file/${SKIN_PACK_NAME}`;
  if (!packs.includes(entry)) await writeOptionsPacks(dir, text, [entry, ...packs]);
}

/** Remove a previously applied offline skin (pack + options pin). */
export async function clearOfflineSkin(instanceId: string): Promise<void> {
  if (!isTauri) return;
  const { remove, exists } = await import("@tauri-apps/plugin-fs");
  const dir = await instanceDirFs(instanceId);
  const zip = `${dir}/resourcepacks/${SKIN_PACK_NAME}`;
  if (await exists(zip)) await remove(zip).catch(() => {});
  const { text, packs } = await readOptionsEntries(dir);
  const entry = `file/${SKIN_PACK_NAME}`;
  if (packs.includes(entry)) {
    await writeOptionsPacks(dir, text, packs.filter((p) => p !== entry)).catch(() => {});
  }
}
