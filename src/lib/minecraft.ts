/**
 * Mojang version manifest — fetched live from the frontend (no Rust HTTP dep).
 * Falls back to the Rust `mc_versions_fallback` list (and a tiny embedded
 * list in the browser) when offline.
 */
import { invokeOr } from "./tauri";

export interface McVersion {
  id: string;
  type: "release" | "snapshot" | "old_alpha" | "old_beta";
  url: string;
  time: string;
  releaseTime: string;
}

let cache: McVersion[] | null = null;

const EMBEDDED = ["1.21.1", "1.21", "1.20.4", "1.20.1", "1.19.2", "1.18.2", "1.16.5", "1.12.2", "1.8.9"];

export async function fetchMcVersions(): Promise<McVersion[]> {
  if (cache) return cache;
  try {
    const res = await fetch("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
    if (!res.ok) throw new Error(`Mojang ${res.status}`);
    const data = (await res.json()) as { versions: McVersion[] };
    cache = data.versions;
    return cache;
  } catch {
    // Offline → Rust fallback (Tauri) or embedded list (browser).
    try {
      const fb = await invokeOr<{ id: string; kind: string }[]>("mc_versions_fallback", {}, []);
      if (fb.length) {
        cache = fb.map((v, i) => ({
          id: v.id,
          type: "release" as const,
          url: "",
          time: "",
          releaseTime: String(i),
        }));
        return cache;
      }
    } catch {
      /* fall through */
    }
    cache = EMBEDDED.map((id) => ({ id, type: "release" as const, url: "", time: "", releaseTime: "" }));
    return cache;
  }
}

/** Loader versions for a given MC version (live meta endpoints, graceful offline). */
export async function fetchLoaderVersions(loader: string, mcVersion: string): Promise<string[]> {
  const l = loader.toLowerCase();
  try {
    if (l === "fabric") {
      const r = await fetch(`https://meta.fabricmc.net/v2/versions/loader/${mcVersion}`);
      if (!r.ok) throw new Error("fabric meta");
      const j = (await r.json()) as { loader: { version: string } }[];
      return j.slice(0, 8).map((x) => x.loader.version);
    }
    if (l === "quilt") {
      const r = await fetch(`https://meta.quiltmc.org/v3/versions/loader/${mcVersion}`);
      if (!r.ok) throw new Error("quilt meta");
      const j = (await r.json()) as { loader: { version: string } }[];
      return j.slice(0, 8).map((x) => x.loader.version);
    }
    // Forge / NeoForge recommend the matching MC build; the full installer
    // resolves it in Phase-2 downloader — surface "recommended" here.
    if (l === "forge" || l === "neoforge") return ["recommended"];
  } catch {
    /* offline → suggest recommended */
  }
  return l === "vanilla" ? [] : ["recommended"];
}
