/**
 * Instance icons — 8 purpose-built SVG marks replacing the old emoji set
 * (⛏️⚔️🏰🐉🌾⚙️🧪🌌). Same 24-grid stroke language as brand.tsx so the
 * whole launcher reads as one product.
 *
 * Storage stays a plain string id ("pickaxe" … "galaxy"). Old instances
 * saved with emoji are mapped via EMOJI_TO_ID, so nothing breaks.
 */

import { useEffect, useState } from "react";
import { ModpackIcon } from "@/components/icons/brand";
import { cn } from "@/lib/utils";

type IconProps = { className?: string };

function Base({ className, children }: IconProps & { children: React.ReactNode }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={cn("size-5 shrink-0", className)} aria-hidden>
      {children}
    </svg>
  );
}

/** Miner — pickaxe diagonal with head arc. */
export function PickaxeIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M5 19 14.5 9.5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" />
      <path d="M9.5 4.5c2.8-1.6 6.4-1.3 9 1.2.4.4.3 1-.2 1.2-2.9 1.1-5.5 3.2-6.9 6-.2.5-.9.5-1.2.1-2-2.9-2-6.2-.7-8.5Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <path d="M4 20.5 5.5 19" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" />
    </Base>
  );
}

/** Warrior — sword diagonal with guard + pommel. */
export function SwordIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M6 18 16.5 7.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="m14.5 4.5 5 5-1.8.4-3.6-3.6Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
      <path d="m7.2 15.2-2.4 2.4 1.6 1.6 2.4-2.4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="4.8" cy="19.2" r="1.1" stroke="currentColor" strokeWidth="1.7" />
    </Base>
  );
}

/** Kingdom — castle tower with battlements + gate. */
export function CastleIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M6 20v-9l-1.5-1.5V6h3v2.2h2V6h3v2.2h2V6h3v3.5L16 11v9" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M4 20h16" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      <path d="M10.2 20v-3.2a1.8 1.8 0 0 1 3.6 0V20" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
    </Base>
  );
}

/** Dragon — serpentine head with horn, eye + back spikes. */
export function DragonIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M4 18c3.5.5 5-1 6-3.5.8-2 1.2-4.5 2.5-6.5l3-2.5c.5-.4 1.2 0 1.1.6L16 9l2.5.5c.6.1.8.9.3 1.2L16 12.5c-1 2.5-2.5 4.5-5 5.5H4Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <circle cx="13.4" cy="9.4" r="0.9" fill="currentColor" />
      <path d="m9.5 5.5 1-2.5 1.5 2M7.5 8.5l-.5-2.5 2 1.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </Base>
  );
}

/** Farm — wheat stalk with grain pairs. */
export function WheatIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M12 21V9" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      <path d="M12 13c-2.5 0-4.5-1.8-5-4.5 2.7 0 4.5 1.6 5 4.5ZM12 13c2.5 0 4.5-1.8 5-4.5-2.7 0-4.5 1.6-5 4.5Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
      <path d="M12 9C9.8 9 8 7.4 7.6 4.8 10 4.8 11.7 6.4 12 9ZM12 9c2.2 0 4-1.6 4.4-4.2C14 4.8 12.3 6.4 12 9Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
      <path d="M8 21h8" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </Base>
  );
}

/** Tech — 8-spoke cog with round core. */
export function CogIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <circle cx="12" cy="12" r="3.4" stroke="currentColor" strokeWidth="1.8" />
      <path d="M12 3.5v2.3M12 18.2v2.3M3.5 12h2.3M18.2 12h2.3M6 6l1.6 1.6M16.4 16.4 18 18M18 6l-1.6 1.6M7.6 16.4 6 18" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </Base>
  );
}

/** Lab — erlenmeyer flask with bubbles. */
export function FlaskIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M9.5 3h5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      <path d="M10.5 3v5.2L5.4 17a1.5 1.5 0 0 0 1.3 2.2h10.6a1.5 1.5 0 0 0 1.3-2.2L13.5 8.2V3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M7.5 14.5h9" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
      <circle cx="11" cy="16.8" r="0.9" fill="currentColor" />
      <circle cx="13.6" cy="17.2" r="0.6" fill="currentColor" opacity="0.6" />
    </Base>
  );
}

/** Galaxy — spiral arms around a core + stars. */
export function GalaxyIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <ellipse cx="12" cy="12" rx="8.5" ry="3.6" transform="rotate(-24 12 12)" stroke="currentColor" strokeWidth="1.7" />
      <ellipse cx="12" cy="12" rx="8.5" ry="3.6" transform="rotate(38 12 12)" stroke="currentColor" strokeWidth="1.4" opacity="0.55" />
      <circle cx="12" cy="12" r="1.6" fill="currentColor" />
      <circle cx="18.6" cy="5.4" r="1" fill="currentColor" opacity="0.8" />
      <circle cx="5.8" cy="18.6" r="0.8" fill="currentColor" opacity="0.6" />
    </Base>
  );
}

/* ── Registry + backward-compatible resolver ───────────────────────────── */

export const INSTANCE_ICONS = [
  { id: "pickaxe", label: "Miner", Icon: PickaxeIcon },
  { id: "sword", label: "Warrior", Icon: SwordIcon },
  { id: "castle", label: "Kingdom", Icon: CastleIcon },
  { id: "dragon", label: "Dragon", Icon: DragonIcon },
  { id: "wheat", label: "Farm", Icon: WheatIcon },
  { id: "cog", label: "Tech", Icon: CogIcon },
  { id: "flask", label: "Lab", Icon: FlaskIcon },
  { id: "galaxy", label: "Galaxy", Icon: GalaxyIcon },
  { id: "modpack", label: "Modpack", Icon: ModpackIcon },
] as const;

export type InstanceIconId = (typeof INSTANCE_ICONS)[number]["id"];

/** Old emoji values → new ids (instances created before the SVG set). */
const EMOJI_TO_ID: Record<string, InstanceIconId> = {
  "⛏️": "pickaxe",
  "⛏": "pickaxe",
  "⚔️": "sword",
  "⚔": "sword",
  "🏰": "castle",
  "🐉": "dragon",
  "🌾": "wheat",
  "⚙️": "cog",
  "⚙": "cog",
  "🧪": "flask",
  "🌌": "galaxy",
};

/**
 * Uploaded/builtin icon renderer.
 *
 * Accepted values:
 *   - builtin id ("pickaxe") or legacy emoji → custom SVG
 *   - `data:image/…` (browser uploads, Rust data-URLs) → <img> directly
 *   - `file:<name>` → fetched via `instance_icon_data` (needs instanceId)
 * Unknown values fall back to raw text so nothing ever breaks.
 */
export function InstanceIcon({
  icon,
  instanceId,
  className,
  imgClassName,
}: {
  icon: string;
  instanceId?: string;
  /** @deprecated use instanceId (asset-protocol free). Kept for compat. */
  instancePath?: string;
  className?: string;
  imgClassName?: string;
}) {
  const [url, setUrl] = useState<string | null>(null);

  const isData = icon.startsWith("data:image/");
  const isFile = icon.startsWith("file:");

  useEffect(() => {
    if (!isFile) {
      setUrl(null);
      return;
    }
    let live = true;
    (async () => {
      try {
        const { resolveInstanceIcon } = await import("@/lib/backend");
        const u = await resolveInstanceIcon(icon, instanceId);
        if (live) setUrl(u);
      } catch {
        if (live) setUrl(null);
      }
    })();
    return () => {
      live = false;
    };
  }, [icon, instanceId, isFile]);

  if (isData) {
    return <img src={icon} alt="" className={cn("size-6 rounded-md object-cover", imgClassName ?? className)} draggable={false} />;
  }
  if (isFile) {
    if (url) {
      return <img src={url} alt="" className={cn("size-6 rounded-md object-cover", imgClassName ?? className)} draggable={false} />;
    }
    return <ImageIcon className={className} />;
  }
  const id = EMOJI_TO_ID[icon] ?? icon;
  const found = INSTANCE_ICONS.find((i) => i.id === id);
  if (!found) return <span className={className}>{icon}</span>;
  const Icon = found.Icon;
  return <Icon className={className} />;
}

/** Generic image glyph for upload tiles / placeholders. */
export function ImageIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <rect x="3.5" y="4.5" width="17" height="15" rx="2.5" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="9" cy="10" r="1.6" stroke="currentColor" strokeWidth="1.6" />
      <path d="m5.5 17.5 4.5-4.5 3 3 2.5-2.5 3 3" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
    </Base>
  );
}
