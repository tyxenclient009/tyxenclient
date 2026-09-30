/**
 * Official vanilla skins — bundled locally so the
 * Skin section works offline with zero downloads and zero CORS issues.
 *
 * Skins: the 9 Mojang default skins (Steve, Alex, Ari, Efe, Kai, Makena,
 * Noor, Sunny, Zuri) as shipped in the vanilla client (64×64).
 *
 * Microsoft accounts still use the live Mojang account skin
 * automatically (see lib/skins.ts fetchMojangTextures) — these bundled
 * officials are the offline picker + fallback.
 */
import type { SkinModel } from "./skins";

export interface DefaultSkin {
  id: string;
  name: string;
  model: SkinModel;
  make: () => string;
}

const SKIN_DEFS: { id: string; name: string; model: SkinModel; file: string }[] = [
  { id: "vanilla-steve", name: "Steve — Vanilla", model: "classic", file: "/skins/vanilla/steve.png" },
  { id: "vanilla-alex", name: "Alex — Vanilla", model: "slim", file: "/skins/vanilla/alex.png" },
  { id: "vanilla-ari", name: "Ari — Vanilla", model: "classic", file: "/skins/vanilla/ari.png" },
  { id: "vanilla-efe", name: "Efe — Vanilla", model: "slim", file: "/skins/vanilla/efe.png" },
  { id: "vanilla-kai", name: "Kai — Vanilla", model: "classic", file: "/skins/vanilla/kai.png" },
  { id: "vanilla-makena", name: "Makena — Vanilla", model: "slim", file: "/skins/vanilla/makena.png" },
  { id: "vanilla-noor", name: "Noor — Vanilla", model: "slim", file: "/skins/vanilla/noor.png" },
  { id: "vanilla-sunny", name: "Sunny — Vanilla", model: "classic", file: "/skins/vanilla/sunny.png" },
  { id: "vanilla-zuri", name: "Zuri — Vanilla", model: "classic", file: "/skins/vanilla/zuri.png" },
];

export const DEFAULT_SKINS: DefaultSkin[] = SKIN_DEFS.map((d) => ({
  id: d.id,
  name: d.name,
  model: d.model,
  make: () => d.file,
}));

/** Legacy custom-skin ids (pre-official era) → official replacements. */
const LEGACY_SKIN_IDS: Record<string, string> = {
  "def-steve": "vanilla-steve",
  "def-alex": "vanilla-alex",
};

export function getDefaultSkin(id?: string): DefaultSkin | undefined {
  if (!id) return undefined;
  return DEFAULT_SKINS.find((s) => s.id === id) ?? DEFAULT_SKINS.find((s) => s.id === LEGACY_SKIN_IDS[id]);
}
