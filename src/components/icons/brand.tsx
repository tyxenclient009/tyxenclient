/**
 * Brand icons — inline SVG React components (no extra deps, crisp at any size).
 *
 * Rule: Lucide ONLY for generic UI glyphs (chevrons, gear, search…).
 * Anything brand-relevant (app mark, Microsoft, account types) gets a
 * purpose-built mark below so it never looks like a placeholder.
 */

import { cn } from "@/lib/utils";

/** Tyx mark — isometric cube nodding at Minecraft blocks, accent green. */
export function TyxLogo({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" className={cn("size-5", className)} aria-hidden>
      <rect x="1" y="1" width="30" height="30" rx="7" fill="#101613" />
      {/* cube top */}
      <path d="M16 7.5 23.5 11.7 16 15.9 8.5 11.7Z" fill="#4fd68a" />
      {/* cube left */}
      <path d="M8.5 12.9v7.7L16 24.8v-7.7Z" fill="#1ea86a" />
      {/* cube right */}
      <path d="M23.5 12.9v7.7L16 24.8v-7.7Z" fill="#178a57" />
      {/* spark */}
      <circle cx="23.4" cy="7.6" r="1.8" fill="#7ee2a8" />
    </svg>
  );
}

/** Official Microsoft 4-square mark (red/green/blue/yellow). */
export function MicrosoftLogo({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 23 23" className={cn("size-4 shrink-0", className)} aria-hidden>
      <rect x="1" y="1" width="10" height="10" fill="#f25022" />
      <rect x="12" y="1" width="10" height="10" fill="#7fba00" />
      <rect x="1" y="12" width="10" height="10" fill="#00a4ef" />
      <rect x="12" y="12" width="10" height="10" fill="#ffb900" />
    </svg>
  );
}

/** Guest/dev glyph — dashed-outline avatar reads "provisional", unlike a real avatar. */
export function GuestIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={cn("size-4 shrink-0", className)} aria-hidden>
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.6" strokeDasharray="3 2.4" opacity="0.7" />
      <circle cx="12" cy="9.4" r="3" stroke="currentColor" strokeWidth="1.6" />
      <path d="M6.8 17.2c.9-2.6 2.9-4 5.2-4s4.3 1.4 5.2 4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  );
}

/** Terminal/dev glyph for the offline card header. */
export function TerminalIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={cn("size-4 shrink-0", className)} aria-hidden>
      <rect x="3" y="4" width="18" height="16" rx="3" stroke="currentColor" strokeWidth="1.8" />
      <path d="m7 9 3 3-3 3M12.5 15H17" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

/* ── Custom nav + product icons (stroke = currentColor, 24 grid) ────────────
   These replace generic Lucide glyphs for brand-relevant surfaces so the
   launcher reads as its own product, not a template. */

type IconProps = { className?: string };

function Base({ className, children }: IconProps & { children: React.ReactNode }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={cn("size-[18px] shrink-0", className)} aria-hidden>
      {children}
    </svg>
  );
}

/** Home — chunky house with Tyx-green door cut. */
export function HomeIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M4 10.5 12 4l8 6.5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M6 9.8V19a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V9.8" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" />
      <rect x="10.2" y="13.5" width="3.6" height="6.5" rx="1" fill="currentColor" opacity="0.85" />
    </Base>
  );
}

/** Instances — stacked isometric cubes (isolated profiles metaphor). */
export function InstancesIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M12 3 20 7.2 12 11.4 4 7.2Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <path d="M4.5 12 12 16l7.5-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M4.5 16 12 20l7.5-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" opacity="0.55" />
    </Base>
  );
}

/** Explore — Modrinth-style compass with needle. */
export function ExploreIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <circle cx="12" cy="12" r="8.5" stroke="currentColor" strokeWidth="1.8" />
      <path d="m15.5 8.5-2 5-5 2 2-5Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <circle cx="12" cy="12" r="0.9" fill="currentColor" />
    </Base>
  );
}

/** Settings — 8-tooth gear with round core. */
export function GearIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path
        d="M12 8.4a3.6 3.6 0 1 0 0 7.2 3.6 3.6 0 0 0 0-7.2Z"
        stroke="currentColor"
        strokeWidth="1.8"
      />
      <path
        d="M12 2.8v2.4M12 18.8v2.4M2.8 12h2.4M18.8 12h2.4M5.5 5.5l1.7 1.7M16.8 16.8l1.7 1.7M18.5 5.5l-1.7 1.7M7.2 16.8l-1.7 1.7"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </Base>
  );
}

/** Collapse rail — panel with chevron pointing in. */
export function CollapseIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <rect x="3.5" y="4" width="17" height="16" rx="2.5" stroke="currentColor" strokeWidth="1.8" />
      <path d="M13.5 9l-2.5 3 2.5 3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M9.5 4v16" stroke="currentColor" strokeWidth="1.4" opacity="0.5" />
    </Base>
  );
}

/** Expand rail — panel with chevron pointing out. */
export function ExpandIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <rect x="3.5" y="4" width="17" height="16" rx="2.5" stroke="currentColor" strokeWidth="1.8" />
      <path d="m10.5 9 2.5 3-2.5 3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M9.5 4v16" stroke="currentColor" strokeWidth="1.4" opacity="0.5" />
    </Base>
  );
}

/** Play — rounded triangle in a ring (big Press-Play energy). */
export function PlayIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <circle cx="12" cy="12" r="8.5" stroke="currentColor" strokeWidth="1.8" />
      <path d="M10 8.8v6.4c0 .6.7 1 1.2.7l4.6-3.2c.5-.3.5-1 0-1.4L11.2 8.1c-.5-.3-1.2 0-1.2.7Z" fill="currentColor" />
    </Base>
  );
}

/** Mod — wrench + cube combo. */
export function ModIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M14.5 6.5a3.5 3.5 0 0 0-4.6 4.2L4 16.6V20h3.4l5.9-5.9a3.5 3.5 0 0 0 4.2-4.6l-2.6 2.6-2.1-.6-.6-2.1Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
    </Base>
  );
}

/** Modpack — box with heart/clasp. */
export function ModpackIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M12 3 20 7v10l-8 4-8-4V7Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <path d="M12 12 20 7M12 12 4 7M12 12v9" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
    </Base>
  );
}

/** Resource pack — paintbrush over pixels. */
export function ResourcePackIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <rect x="3.5" y="3.5" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.7" />
      <rect x="13.5" y="3.5" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.7" opacity="0.55" />
      <path d="m13.5 13.5 4.8 4.8a1.6 1.6 0 0 1-2.3 2.3l-4.8-4.8Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
      <path d="m14.5 12.5 2 2" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
    </Base>
  );
}

/** Shader — sparkle / sun over horizon. */
export function ShaderIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M18.4 5.6l-2.1 2.1M7.7 16.3l-2.1 2.1" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
      <circle cx="12" cy="12" r="3.2" stroke="currentColor" strokeWidth="1.8" />
    </Base>
  );
}

/** Loader cube — tiny per-loader mark (fabric weave / anvil / quilt patch). */
export function LoaderIcon({ loader, className }: { loader: string; className?: string }) {
  const l = loader.toLowerCase();
  const dot = l.includes("fabric") ? "#7ee2a8" : l.includes("forge") ? "#f59e0b" : l.includes("quilt") ? "#c084fc" : l.includes("neo") ? "#60a5fa" : "currentColor";
  return (
    <svg viewBox="0 0 24 24" fill="none" className={cn("size-3.5 shrink-0", className)} aria-hidden>
      <path d="M12 3.5 19.5 7.7 12 11.9 4.5 7.7Z" stroke={dot} strokeWidth="1.8" strokeLinejoin="round" />
      <path d="M4.5 12 12 16.2 19.5 12" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M4.5 16 12 20.2 19.5 16" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" opacity="0.45" />
    </svg>
  );
}

/** Download with tray — chunkier than Lucide for Install buttons. */
export function DownloadIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M12 4v10.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="m7.5 11 4.5 4.5L16.5 11" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M4.5 19.5h15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </Base>
  );
}

/** Search — magnifier with Tyx flare dot. */
export function SearchIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <circle cx="11" cy="11" r="6.5" stroke="currentColor" strokeWidth="1.9" />
      <path d="m16 16 4.5 4.5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" />
    </Base>
  );
}

/** Heart / follow — for Modrinth-like follows. */
export function HeartIcon({ className, filled }: IconProps & { filled?: boolean }) {
  return (
    <svg viewBox="0 0 24 24" className={cn("size-3.5 shrink-0", className)} aria-hidden fill={filled ? "currentColor" : "none"}>
      <path
        d="M12 20.5C7 16.5 3.5 13.3 3.5 9.6A4.6 4.6 0 0 1 8.1 5c1.6 0 3 .9 3.9 2.2A4.6 4.6 0 0 1 15.9 5a4.6 4.6 0 0 1 4.6 4.6c0 3.7-3.5 6.9-8.5 10.9Z"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinejoin="round"
      />
    </svg>
  );
}

/** Skins — a player tee for the appearance section. */
export function SkinIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M9 4 4 7.2 6 11l2-1v10h8V10l2 1 2-3.8L15 4a3 3 0 0 1-6 0Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
    </Base>
  );
}

/** Server rack — for saved/recent game servers (Modrinth-style join). */
export function MegaphoneIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M4 10v5l3 1 1 4h2.5l-1-3.6L19 19V6l-9.5 2.4L4 10z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" fill="none" />
      <path d="M19 9.5a2.5 2.5 0 0 1 0 6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" fill="none" />
      <path d="M7.5 20.5h4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </Base>
  );
}

export function ServerIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <rect x="4" y="4" width="16" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.8" />
      <rect x="4" y="13" width="16" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.8" />
      <path d="M7 7.5h6M7 16.5h6" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
      <circle cx="16.8" cy="7.5" r="1" fill="currentColor" />
      <circle cx="16.8" cy="16.5" r="1" fill="currentColor" opacity="0.55" />
    </Base>
  );
}

/** Worlds — globe inside a folder (saved single-player worlds). */
export function WorldIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M3.5 7a2 2 0 0 1 2-2h4l2 2.5h7a2 2 0 0 1 2 2V17a2 2 0 0 1-2 2h-13a2 2 0 0 1-2-2Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <circle cx="12" cy="13.5" r="3" stroke="currentColor" strokeWidth="1.6" />
      <path d="M9 13.5h6M12 10.5c-1.2 1-1.2 5 0 6M12 10.5c1.2 1 1.2 5 0 6" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
    </Base>
  );
}

/** Screenshots — framed landscape image. */
export function ShotsIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <rect x="3.5" y="5" width="17" height="14" rx="2.5" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="9" cy="10" r="1.6" stroke="currentColor" strokeWidth="1.6" />
      <path d="m5.5 17 4.5-4.5 3 3 2.5-2.5 3 3" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
    </Base>
  );
}

/** Logs — scroll / document lines. */
export function LogsIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M7 3.5h10a1 1 0 0 1 1 1V19a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4.5a1 1 0 0 1 1-1Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <path d="M9 8.5h6M9 12h6M9 15.5h4" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
    </Base>
  );
}

/** Folder — generic directory (world folders, configs). */
export function FolderIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M3.5 7a2 2 0 0 1 2-2h4l2 2.5h7a2 2 0 0 1 2 2V17a2 2 0 0 1-2 2h-13a2 2 0 0 1-2-2Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
    </Base>
  );
}

/** Crash — alert triangle for the error boundary (no emoji). */
export function CrashIcon({ className }: IconProps) {
  return (
    <Base className={className}>
      <path d="M12 4 21 19.5H3Z" stroke="currentColor" strokeWidth="1.9" strokeLinejoin="round" />
      <path d="M12 10v4.5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" />
      <circle cx="12" cy="17" r="1.1" fill="currentColor" />
    </Base>
  );
}
