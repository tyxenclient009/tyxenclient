import { ModpackIcon } from "@/components/icons/brand";
import { useModpackProgress } from "@/stores/modpackProgress";

/**
 * Global modpack install progress bar — mounted once in AppShell so it is
 * visible on Home, Instances and Browse while a .mrpack downloads.
 * Pure English labels + custom SVG icon (no emoji).
 */
export function ModpackProgressBanner() {
  const { active, name, pct, stage, current, doneFiles, totalFiles } = useModpackProgress();

  if (!active) return null;

  const safePct = Math.max(0, Math.min(100, Math.round(pct)));

  return (
    <div
      role="status"
      aria-live="polite"
      aria-label={`Installing ${name}, ${safePct} percent complete`}
      className="mx-3 mt-3 shrink-0 rounded-xl border border-accent-500/40 bg-surface-100/80 px-4 py-3 shadow-glow backdrop-blur-xl"
    >
      <div className="flex items-center gap-3">
        <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-accent-500/15">
          <ModpackIcon className="size-5 text-accent-400" />
        </span>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold">
            Installing <span className="text-accent-400">{name || "modpack"}</span>
            <span className="ml-2 font-mono text-xs font-bold text-accent-400">{safePct}%</span>
          </p>
          <p className="truncate text-xs text-ink-muted">
            {stage || "working"}
            {totalFiles > 0 && (
              <span className="font-mono">
                {" "}· {doneFiles}/{totalFiles} files
              </span>
            )}
            {current && <span className="font-mono"> · {current}</span>}
          </p>
          <div
            className="mt-1.5 h-1.5 overflow-hidden rounded-full bg-surface-300"
            role="progressbar"
            aria-valuenow={safePct}
            aria-valuemin={0}
            aria-valuemax={100}
          >
            <div
              className="hero-gradient-bar h-full rounded-full transition-all duration-300"
              style={{ width: `${Math.max(safePct, 2)}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
