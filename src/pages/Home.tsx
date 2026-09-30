import { useEffect, useState } from "react";
import { Pencil, Play, Plus, Square, TerminalSquare } from "lucide-react";
import { FadeUp, Button } from "@/components/ui/Button";
import { HoverCard, Badge } from "@/components/ui/Card";
import { CardGridSkeleton } from "@/components/ui/Skeleton";
import { EmptyState, ErrorState } from "@/components/ui/States";
import { CreateInstanceDialog } from "@/components/dialogs/CreateInstanceDialog";
import { HeroEditor } from "@/components/home/HeroEditor";
import { heroFilter, heroImageSrc, useHero } from "@/stores/hero";
import { ServersSection } from "@/components/home/ServersSection";
import { InstanceIcon } from "@/components/icons/instances";
import { ExploreIcon, InstancesIcon, ModIcon } from "@/components/icons/brand";
import { useInstances } from "@/stores/instances";
import { useLaunch } from "@/stores/launch";
import { useServers } from "@/stores/servers";
import { useNav } from "@/stores/app";
import { openConsoleWindow } from "@/lib/console-window";
import { useToasts } from "@/stores/app";
import { playInstance } from "@/lib/play";
import { useModsCounts } from "@/lib/use-mods-count";
import { timeAgo } from "@/lib/utils";

function greeting(): string {
  const h = new Date().getHours();
  if (h < 5) return "Good night";
  if (h < 12) return "Good morning";
  if (h < 17) return "Good afternoon";
  if (h < 21) return "Good evening";
  return "Good night";
}

/** Home — real instance grid backed by Rust disk store (or LS in browser). */
export function HomePage() {
  const { instances, loading, error, refresh, select } = useInstances();
  const lines = useLaunch((s) => s.lines);
  const runningIds = useLaunch((s) => s.runningIds);
  const push = useToasts((s) => s.push);
  const go = useNav((s) => s.go);
  const savedCount = useServers((s) => s.saved.length);
  const recentCount = useServers((s) => s.recent.length);
  const modsCounts = useModsCounts(instances.map((i) => i.id));
  const [creating, setCreating] = useState(false);
  const [editingBanner, setEditingBanner] = useState(false);
  const [playing, setPlaying] = useState<string | null>(null);
  const hero = useHero();
  // Hero parallax (brief §Micro-interactions): subtle wallpaper drift on
  // desktop pointer movement. Disabled on touch / reduced-motion.
  const [parallax, setParallax] = useState({ x: 0, y: 0 });

  useEffect(() => {
    refresh().catch(() => {});
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const onHeroMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (window.matchMedia?.("(prefers-reduced-motion: reduce)").matches) return;
    if (window.matchMedia?.("(hover: none)").matches) return; // touch: no parallax
    const r = e.currentTarget.getBoundingClientRect();
    const x = (e.clientX - r.left) / r.width - 0.5;
    const y = (e.clientY - r.top) / r.height - 0.5;
    setParallax({ x: Math.max(-1, Math.min(1, x)), y: Math.max(-1, Math.min(1, y)) });
  };

  const stop = async (id: string) => {
    try {
      const { stopGame } = await import("@/lib/backend");
      await stopGame(id);
    } catch (e) {
      push({ kind: "error", title: "Couldn't stop", body: e instanceof Error ? e.message : String(e) });
    }
  };

  const play = async (id: string) => {
    setPlaying(id);
    try {
      await playInstance(id);
      refresh().catch(() => {});
    } catch (e) {
      push({ kind: "error", title: "Launch failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setPlaying(null);
    }
  };

  return (
    <div className="mx-auto max-w-5xl">
      <FadeUp>
        {/* Hero — editable banner art + scrims + pointer parallax (desktop only) */}
        <div
          className="group relative overflow-hidden rounded-xl border border-line shadow-2"
          onMouseMove={onHeroMove}
          onMouseLeave={() => setParallax({ x: 0, y: 0 })}
        >
          <img
            src={heroImageSrc(hero)}
            alt="Home banner"
            className="h-80 w-full object-cover transition-transform duration-300 ease-out sm:h-72"
            style={{
              objectPosition: `${hero.posX}% ${hero.posY}%`,
              transform: `scale(${hero.zoom}) translate(${parallax.x * -10}px, ${parallax.y * -8}px)`,
              filter: heroFilter(hero),
            }}
            draggable={false}
          />
          {/* Legibility scrims: dark left for the greeting, dark bottom so the
              artwork stays vivid in BOTH themes (never a white surface fade
              over the art — that washed it out in light mode). */}
          <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-black/70 via-black/25 to-transparent" />
          <div className="pointer-events-none absolute inset-x-0 bottom-0 h-28 bg-gradient-to-t from-black/80 via-black/25 to-transparent" />
          {/* Animated accent hairline */}
          <div className="hero-gradient-bar absolute inset-x-0 bottom-0 h-0.5 opacity-70" aria-hidden />
          <div className="absolute inset-x-0 top-0 flex flex-col items-start gap-4 p-4 sm:flex-row sm:items-start sm:justify-between sm:p-5">
            <div className="min-w-0">
              <h1 className="text-2xl font-bold tracking-tight text-white drop-shadow-[0_2px_8px_rgb(0_0_0/0.8)]">
                {greeting()}
              </h1>
              <p className="mt-1 text-sm font-medium text-zinc-200 drop-shadow-[0_1px_6px_rgb(0_0_0/0.8)]">
                {instances.length ? "Pick an instance and jump back in." : "Create your first isolated instance."}
              </p>
              {instances.length > 0 && (
                <div className="mt-2.5 flex flex-wrap gap-1.5">
                  <span className="rounded-full border border-white/20 bg-black/45 px-2.5 py-0.5 text-[11px] font-semibold text-white backdrop-blur-md">
                    {instances.length} instance{instances.length === 1 ? "" : "s"}
                  </span>
                  {(savedCount + recentCount) > 0 && (
                    <span className="rounded-full border border-white/20 bg-black/45 px-2.5 py-0.5 text-[11px] font-semibold text-white backdrop-blur-md">
                      {savedCount + recentCount} server{(savedCount + recentCount) === 1 ? "" : "s"}
                    </span>
                  )}
                </div>
              )}
            </div>
            <div className="flex w-full shrink-0 flex-wrap justify-start gap-2 sm:w-auto sm:justify-end">
              <Button
                variant="secondary"
                onClick={() => setEditingBanner(true)}
                title="Edit banner image, crop and colors"
                aria-label="Edit home banner"
                className="border-white/20 bg-black/50 text-white backdrop-blur-md hover:bg-black/65 hover:text-white"
              >
                <Pencil /> Edit banner
              </Button>
              {lines.length > 0 && (
                <Button
                  variant="secondary"
                  onClick={() => openConsoleWindow()}
                  className="border-white/20 bg-black/50 text-white backdrop-blur-md hover:bg-black/65 hover:text-white"
                >
                  <TerminalSquare /> Console
                </Button>
              )}
              <Button
                variant="secondary"
                onClick={() => go("browse")}
                className="border-white/20 bg-black/50 text-white backdrop-blur-md hover:bg-black/65 hover:text-white"
              >
                <ExploreIcon /> Find mods
              </Button>
              <Button variant="primary" onClick={() => setCreating(true)} className="shadow-glow">
                <Plus /> New instance
              </Button>
            </div>
          </div>
        </div>
      </FadeUp>

      <div className="mt-6">
        {loading ? (
          <CardGridSkeleton count={4} />
        ) : error ? (
          <ErrorState title="Couldn't load instances" hint={error} onRetry={refresh} />
        ) : instances.length === 0 ? (
          <FadeUp delay={0.05}>
            <EmptyState
              icon={InstancesIcon}
              title="No instances yet"
              hint="Create your first instance — pick a Minecraft version and loader and we set up the isolated folder."
              actionLabel="Create instance"
              onAction={() => setCreating(true)}
            />
          </FadeUp>
        ) : (
          <>
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold tracking-tight">Your instances</h2>
              <span className="font-mono text-xs text-ink-faint">{instances.length} total</span>
            </div>
            <div className="mt-3 grid grid-cols-1 gap-4 sm:grid-cols-2">
              {[...instances]
                .sort((a, b) => (b.lastPlayed ?? 0) - (a.lastPlayed ?? 0))
                .map((inst, i) => (
                  <FadeUp key={inst.id} delay={Math.min(i * 0.05, 0.2)}>
                    <HoverCard onClick={() => select(inst.id)}>
                      <div className="flex items-center gap-3">
                        <div className="flex size-11 shrink-0 items-center justify-center rounded-lg border border-accent-500/25 bg-gradient-to-b from-accent-500/20 to-accent-500/5">
                          <InstanceIcon icon={inst.icon} instanceId={inst.id} className="size-6 text-accent-400" imgClassName="size-8 rounded-lg object-cover" />
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="flex items-center gap-1.5 truncate text-[15px] font-semibold">
                            <span className="truncate">{inst.name}</span>
                            {runningIds.includes(inst.id) && (
                              <span className="flex shrink-0 items-center gap-1 rounded-full border border-accent-500/40 bg-accent-500/10 px-2 py-px text-[10px] font-bold uppercase tracking-wider text-accent-400">
                                <span className="size-1.5 animate-pulse rounded-full bg-accent-400" />
                                Running
                              </span>
                            )}
                          </p>
                          <p className="text-xs text-ink-faint">{timeAgo(inst.lastPlayed)}</p>
                        </div>
                        <Badge tone="accent">{inst.loader}</Badge>
                        <Badge className="font-mono">{inst.version}</Badge>
                      </div>
                      <div className="mt-3 flex items-center gap-1.5 border-t border-white/5 pt-3 text-[11px] text-ink-faint">
                        <ModIcon className="size-3.5" />
                        {inst.id in modsCounts ? (
                          <span>{modsCounts[inst.id]} mod{modsCounts[inst.id] === 1 ? "" : "s"} · {inst.settings.ramMb} MB</span>
                        ) : (
                          <span>{inst.settings.ramMb} MB</span>
                        )}
                      </div>
                      <div className="mt-3 flex gap-2" onClick={(e) => e.stopPropagation()}>
                        {runningIds.includes(inst.id) ? (
                          <Button variant="danger" size="sm" className="flex-1" onClick={() => stop(inst.id)}>
                            <Square className="fill-current" /> Stop
                          </Button>
                        ) : (
                          <Button variant="primary" size="sm" className="flex-1" disabled={playing === inst.id} onClick={() => play(inst.id)}>
                            <Play /> {playing === inst.id ? "Launching…" : "Play"}
                          </Button>
                        )}
                        <Button variant="secondary" size="sm" onClick={() => select(inst.id)}>
                          Manage
                        </Button>
                      </div>
                    </HoverCard>
                  </FadeUp>
                ))}
            </div>
          </>
        )}
      </div>

      <CreateInstanceDialog open={creating} onClose={() => setCreating(false)} />
      <HeroEditor open={editingBanner} onClose={() => setEditingBanner(false)} />
      <ServersSection />
    </div>
  );
}
