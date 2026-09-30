/**
 * Modrinth API client (v2) — no key needed for reads.
 * Docs: https://docs.modrinth.com/
 *
 * All calls are plain fetch so they work in the browser preview AND inside
 * Tauri. Facets (loader / version filters) follow Modrinth's nested-array
 * syntax, JSON-encoded into the `facets` query param.
 */

const BASE = "https://api.modrinth.com/v2";
const UA = "TyxLauncher/0.1.0 (contact: tyx.gg)";

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "User-Agent": UA },
  });
  if (!res.ok) throw new Error(`Mod index ${res.status}: ${res.statusText}`);
  return res.json() as Promise<T>;
}

export type ProjectType = "mod" | "modpack" | "resourcepack" | "shader";
export type SortKey = "relevance" | "downloads" | "follows" | "newest" | "updated";

export interface SearchHit {
  project_id: string;
  project_type: ProjectType;
  slug: string;
  title: string;
  /** Modrinth author username (present on /search, may be absent on old cache). */
  author?: string;
  description: string;
  icon_url: string | null;
  downloads: number;
  follows: number;
  versions: string[];
  categories: string[];
  loaders: string[];
  date_created: string;
  date_modified: string;
}

export interface SearchResult {
  hits: SearchHit[];
  offset: number;
  limit: number;
  total_hits: number;
}

export interface Project {
  id: string;
  slug: string;
  title: string;
  description: string;
  body: string;
  icon_url: string | null;
  downloads: number;
  followers: number;
  categories: string[];
  loaders: string[];
  game_versions: string[];
  project_type: ProjectType;
  gallery: { url: string; title: string }[];
  /** Modrinth parity fields (may be absent on old cache — all optional). */
  team?: string;
  published?: string;
  updated?: string;
  license?: { id: string; name: string; url?: string | null };
  client_side?: "required" | "optional" | "unsupported" | "unknown";
  server_side?: "required" | "optional" | "unsupported" | "unknown";
}

export interface ProjectVersion {
  id: string;
  name: string;
  version_number: string;
  loaders: string[];
  game_versions: string[];
  version_type: "release" | "beta" | "alpha";
  downloads: number;
  date_published: string;
  dependencies: { project_id: string | null; version_id: string | null; dependency_type: string }[];
  files: {
    url: string;
    filename: string;
    primary: boolean;
    size: number;
    hashes: { sha1: string; sha512: string };
  }[];
}

/** Canonical Modrinth web URL for a project (used by "View on Modrinth"). */
export function modrinthUrl(type: ProjectType, slugOrId: string): string {
  const seg = type === "mod" ? "mod" : type === "modpack" ? "modpack" : type === "resourcepack" ? "resourcepack" : "shader";
  return `https://modrinth.com/${seg}/${encodeURIComponent(slugOrId)}`;
}

export async function openOnModrinth(type: ProjectType, slugOrId: string): Promise<void> {
  const url = modrinthUrl(type, slugOrId);
  try {
    const { open } = await import("@tauri-apps/plugin-shell");
    await open(url);
  } catch {
    window.open(url, "_blank", "noopener");
  }
}

export const MODRINTH_TABS: { label: string; type: ProjectType }[] = [
  { label: "Modpacks", type: "modpack" },
  { label: "Mods", type: "mod" },
  { label: "Resource Packs", type: "resourcepack" },
  { label: "Shaders", type: "shader" },
];

export const LOADER_FILTERS = ["fabric", "forge", "quilt", "neoforge", "vanilla"] as const;
export const MC_FILTERS = ["1.21.1", "1.21", "1.20.4", "1.20.1", "1.19.2", "1.18.2", "1.16.5", "1.12.2", "1.8.9"] as const;

export interface SearchOptions {
  query: string;
  type: ProjectType;
  loaders?: string[];
  gameVersions?: string[];
  sort?: SortKey;
  offset?: number;
  limit?: number;
}

export function searchProjects(o: SearchOptions): Promise<SearchResult> {
  const facets: string[][] = [[`project_type:${o.type}`]];
  if (o.loaders?.length) facets.push(o.loaders.map((l) => `categories:${l}`));
  if (o.gameVersions?.length) facets.push(o.gameVersions.map((v) => `versions:${v}`));
  const params = new URLSearchParams({
    query: o.query,
    facets: JSON.stringify(facets),
    index: o.sort ?? "relevance",
    offset: String(o.offset ?? 0),
    limit: String(Math.min(o.limit ?? 20, 100)),
  });
  return get<SearchResult>(`/search?${params}`);
}

export function getProject(idOrSlug: string): Promise<Project> {
  return get<Project>(`/project/${encodeURIComponent(idOrSlug)}`);
}

export function getProjectVersions(
  idOrSlug: string,
  opts?: { loaders?: string[]; gameVersions?: string[]; limit?: number; offset?: number },
): Promise<ProjectVersion[]> {
  const params = new URLSearchParams();
  if (opts?.loaders?.length) params.set("loaders", JSON.stringify(opts.loaders));
  if (opts?.gameVersions?.length) params.set("game_versions", JSON.stringify(opts.gameVersions));
  if (typeof opts?.limit === "number") params.set("limit", String(Math.min(Math.max(opts.limit, 1), 100)));
  if (typeof opts?.offset === "number") params.set("offset", String(Math.max(opts.offset, 0)));
  const q = params.toString();
  return get<ProjectVersion[]>(`/project/${encodeURIComponent(idOrSlug)}/version${q ? `?${q}` : ""}`);
}

export function getVersion(versionId: string): Promise<ProjectVersion> {
  return get<ProjectVersion>(`/version/${encodeURIComponent(versionId)}`);
}

export type VersionChannel = "release" | "beta" | "alpha";

/** Why a version doesn't fit the target instance (Modrinth-app style reason). */
export function versionIncompatibility(
  v: ProjectVersion,
  mcVersion?: string,
  loader?: string,
): string | null {
  const l = (loader ?? "").toLowerCase();
  if (mcVersion && (v.game_versions ?? []).length > 0 && !(v.game_versions ?? []).includes(mcVersion)) {
    return `needs MC ${(v.game_versions ?? []).slice(0, 3).join(", ")}`;
  }
  if (l && l !== "vanilla" && ["fabric", "forge", "quilt", "neoforge"].includes(l)) {
    const ls = (v.loaders ?? []).map((x) => x.toLowerCase());
    if (ls.length > 0 && !ls.includes(l) && !ls.includes("minecraft")) {
      return `needs ${ (v.loaders ?? []).slice(0, 2).join(", ") || "another loader"}`;
    }
  }
  return null;
}

/** Best file to install: primary first, then first .jar/.zip. */
export function pickFile(v: ProjectVersion) {
  const files = v.files ?? [];
  return (
    files.find((f) => f.primary) ??
    files.find((f) => /\.(jar|zip)$/i.test(f.filename)) ??
    files[0]
  );
}

export function formatDownloads(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}K`;
  return String(n);
}

/** "3d ago" for ISO timestamps (Modrinth date_modified). */
export function timeAgoIso(iso?: string): string {
  if (!iso) return "";
  const ts = Date.parse(iso);
  if (Number.isNaN(ts)) return "";
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "just now";
  const m = Math.floor(s / 60);
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  const d = Math.floor(h / 24);
  if (d < 30) return `${d}d ago`;
  const mo = Math.floor(d / 30);
  if (mo < 12) return `${mo}mo ago`;
  return `${Math.floor(mo / 12)}y ago`;
}

/* ── Lightweight client-side logics (no backend needed) ─────────────────── */

// Recent searches (max 6, de-duplicated).
const LS_RECENT = "tyx:recent-searches";
export function getRecentSearches(): string[] {
  try {
    return JSON.parse(localStorage.getItem(LS_RECENT) ?? "[]") as string[];
  } catch {
    return [];
  }
}
export function pushRecentSearch(q: string) {
  const clean = q.trim();
  if (clean.length < 2) return;
  try {
    const list = getRecentSearches().filter((x) => x.toLowerCase() !== clean.toLowerCase());
    localStorage.setItem(LS_RECENT, JSON.stringify([clean, ...list].slice(0, 6)));
  } catch {
    /* ignore */
  }
}

// Followed projects (heart toggle, purely local bookmark).
const LS_FOLLOWS = "tyx:follows";
export function getFollows(): string[] {
  try {
    return JSON.parse(localStorage.getItem(LS_FOLLOWS) ?? "[]") as string[];
  } catch {
    return [];
  }
}
export function isFollowed(id: string): boolean {
  return getFollows().includes(id);
}
export function toggleFollow(id: string): boolean {
  const list = getFollows();
  const next = list.includes(id) ? list.filter((x) => x !== id) : [...list, id];
  try {
    localStorage.setItem(LS_FOLLOWS, JSON.stringify(next));
  } catch {
    /* ignore */
  }
  return next.includes(id);
}

// Installed markers (so cards can show "Installed ✓" after a download).
const LS_INSTALLED = "tyx:installed";
export function getInstalled(): string[] {
  try {
    return JSON.parse(localStorage.getItem(LS_INSTALLED) ?? "[]") as string[];
  } catch {
    return [];
  }
}
export function markInstalled(id: string) {
  try {
    const list = getInstalled();
    if (!list.includes(id)) localStorage.setItem(LS_INSTALLED, JSON.stringify([...list, id]));
  } catch {
    /* ignore */
  }
}

// Per-instance markers: projectId → installed filename ("" = modpack-made
// instance, kept without file checks). Lets Browse show Installed per
// target instance, Modrinth-style, and survive restarts.
const LS_INST_PREFIX = "tyx:installed:";
export function getInstalledFor(instanceId: string): Record<string, string> {
  try {
    return JSON.parse(localStorage.getItem(LS_INST_PREFIX + instanceId) ?? "{}") as Record<string, string>;
  } catch {
    return {};
  }
}
export function markInstalledFor(instanceId: string, projectId: string, filename: string) {
  try {
    const m = getInstalledFor(instanceId);
    m[projectId] = filename;
    localStorage.setItem(LS_INST_PREFIX + instanceId, JSON.stringify(m));
  } catch {
    /* ignore */
  }
}

/** Drop all per-instance install markers (instance deleted — no stale badges on ID reuse). */
export function clearInstalledFor(instanceId: string) {
  try {
    localStorage.removeItem(LS_INST_PREFIX + instanceId);
  } catch {
    /* ignore */
  }
}

/* ── Local jar → Modrinth identity (drawer logos) ────────────────────────── */

export interface ModIdentity {
  projectId: string;
  slug: string;
  title: string;
  icon_url: string | null;
}

const LOADER_TOKENS = new Set(["fabric", "forge", "neoforge", "quilt", "vanilla", "all", "universal", "standalone"]);
const BUILD_TOKENS = new Set(["release", "beta", "alpha", "build", "final", "hotfix", "client", "server", "common"]);

function isVersionToken(t: string): boolean {
  if (/\d/.test(t)) return true; // 0.141.6, 1.21.11, mc1.21, v2 …
  return LOADER_TOKENS.has(t) || BUILD_TOKENS.has(t);
}

/** `fabric-api-0.141.6+1.21.11.jar` → `fabric-api`; `sodium-fabric-0.6.9+mc1.21.1.jar` → `sodium`. */
export function guessModSlug(filename: string): string {
  const base = filename
    .toLowerCase()
    .replace(/\.(jar|zip|jar\.disabled|jar\.off)$/, "")
    .replace(/[_\s]+/g, "-");
  const parts = base.split("-").filter(Boolean);
  // Strip trailing version/build tokens (0.141.6, mc1.21.1, fabric, release…).
  while (parts.length > 1 && isVersionToken(parts[parts.length - 1])) parts.pop();
  return parts.join("-");
}

const norm = (s: string) => s.toLowerCase().replace(/[^a-z0-9]/g, "");

const identityCache = new Map<string, Promise<ModIdentity | null>>();

/**
 * Best-effort Modrinth identity for a local jar (icon + slug for the
 * drawer rows). Cached per filename; strict slug/title match or null
 * (caller renders the fallback tile). Never throws.
 */
export function resolveModIdentity(filename: string): Promise<ModIdentity | null> {
  const key = filename.toLowerCase();
  const hit = identityCache.get(key);
  if (hit) return hit;
  const p = (async (): Promise<ModIdentity | null> => {
    try {
      const q = guessModSlug(filename);
      if (q.length < 2) return null;
      const r = await searchProjects({ query: q, type: "mod", limit: 5 });
      const nq = norm(q);
      const best = (r.hits ?? []).find((h) => {
        const ns = norm(h.slug ?? "");
        const nt = norm(h.title ?? "");
        return ns === nq || nt === nq || nq.startsWith(ns) || ns.startsWith(nq);
      });
      if (!best) return null;
      return { projectId: best.project_id, slug: best.slug, title: best.title, icon_url: best.icon_url };
    } catch {
      return null;
    }
  })();
  identityCache.set(key, p);
  return p;
}

/**
 * Modrinth-grade compatibility check for a search hit vs a target instance.
 * Returns yes / no / unknown (unknown = API listed nothing to judge by).
 * Accurate rules: modpacks ignore loaders; mods/resourcepacks/shaders must
 * match both the MC version and the loader when the hit declares them.
 */
export function hitCompatibility(
  hit: SearchHit,
  mcVersion?: string,
  loader?: string,
): "yes" | "no" | "unknown" {
  const versions = hit.versions ?? [];
  const loaders = (hit.loaders ?? []).map((l) => l.toLowerCase());
  const wantLoader = (loader ?? "").toLowerCase();
  const vanilla = wantLoader === "" || wantLoader === "vanilla";

  let versionOk: boolean | null = null;
  if (mcVersion) {
    versionOk = versions.length === 0 ? null : versions.includes(mcVersion);
  }
  let loaderOk: boolean | null = null;
  if (!vanilla && hit.project_type === "mod") {
    loaderOk = loaders.length === 0 ? null : loaders.includes(wantLoader);
  }

  if (versionOk === false || loaderOk === false) return "no";
  if (versionOk === null && loaderOk === null) return "unknown";
  return "yes";
}

/** Newest version of a project compatible with an instance (or null). */
export async function latestCompatibleVersion(
  projectId: string,
  mcVersion?: string,
  loader?: string,
): Promise<ProjectVersion | null> {
  const l = (loader ?? "").toLowerCase();
  const loaders =
    l && l !== "vanilla" && ["fabric", "forge", "quilt", "neoforge"].includes(l) ? [l] : undefined;
  try {
    const vs = await getProjectVersions(projectId, {
      loaders,
      gameVersions: mcVersion ? [mcVersion] : undefined,
    });
    return vs[0] ?? null;
  } catch {
    return null;
  }
}
