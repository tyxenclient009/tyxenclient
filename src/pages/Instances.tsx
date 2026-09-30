import { useMemo, useState } from "react";
import { Play, Search, Square, Trash2 } from "lucide-react";
import { FadeUp, Button } from "@/components/ui/Button";
import { HoverCard, Badge } from "@/components/ui/Card";
import { CardGridSkeleton } from "@/components/ui/Skeleton";
import { EmptyState, ErrorState } from "@/components/ui/States";
import { CreateInstanceDialog } from "@/components/dialogs/CreateInstanceDialog";
import { InstanceIcon } from "@/components/icons/instances";
import { InstancesIcon, ModIcon } from "@/components/icons/brand";
import { useInstances } from "@/stores/instances";
import { useLaunch } from "@/stores/launch";
import { useToasts } from "@/stores/app";
import { playInstance } from "@/lib/play";
import { useModsCounts } from "@/lib/use-mods-count";
import { timeAgo } from "@/lib/utils";

/** Instances — full CRUD grid + create dialog + detail drawer. */
export function InstancesPage() {
  const { instances, loading, error, refresh, remove, select } = useInstances();
  const runningIds = useLaunch((s) => s.runningIds);
  const push = useToasts((s) => s.push);
  const [creating, setCreating] = useState(false);
  const [query, setQuery] = useState("");
  const modsCounts = useModsCounts(instances.map((i) => i.id));

  const stop = async (id: string) => {
    try {
      const { stopGame } = await import("@/lib/backend");
      await stopGame(id);
    } catch (e) {
      push({ kind: "error", title: "Couldn't stop", body: e instanceof Error ? e.message : String(e) });
    }
  };

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase();
    const list = q
      ? instances.filter((i) => `${i.name} ${i.version} ${i.loader}`.toLowerCase().includes(q))
      : instances;
    return [...list].sort((a, b) => (b.lastPlayed ?? 0) - (a.lastPlayed ?? 0));
  }, [instances, query]);

  return (
    <div className="mx-auto max-w-5xl">
      <FadeUp>
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold tracking-tight">Instances</h1>
            <p className="mt-1 text-sm text-ink-muted">
              Isolated <code className="rounded bg-surface-200/70 px-1 font-mono text-xs">.minecraft</code> folders — versions, loaders, mods never clash.
            </p>
          </div>
          <div className="flex w-full shrink-0 flex-wrap gap-2 sm:w-auto">
            <Button variant="primary" onClick={() => setCreating(true)}>+ Create</Button>
            <Button
              variant="secondary"
              onClick={async () => {
                try {
                  const { pickModpackFile } = await import("@/lib/backend");
                  const { importPackFromPath } = await import("@/lib/import-pack");
                  const p = await pickModpackFile();
                  if (p) await importPackFromPath(p);
                } catch (e) {
                  push({ kind: "error", title: "Import failed", body: e instanceof Error ? e.message : String(e) });
                }
              }}
            >
              Import .mrpack
            </Button>
          </div>
        </div>
      </FadeUp>

      <div className="mt-6">
        {loading ? (
          <CardGridSkeleton count={6} />
        ) : error ? (
          <ErrorState title="Couldn't load instances" hint={error} onRetry={refresh} />
        ) : instances.length === 0 ? (
          <FadeUp delay={0.05}>
            <EmptyState
              icon={InstancesIcon}
              title="No instances yet"
              hint="Each instance is an isolated profile with its own version, loader, mods, worlds and settings."
              actionLabel="Create your first instance"
              onAction={() => setCreating(true)}
            />
          </FadeUp>
        ) : (
          <>
            {instances.length > 3 && (
              <div className="relative mb-4 max-w-sm">
                <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-faint" />
                <input
                  className="input pl-9 text-sm"
                  placeholder={`Filter ${instances.length} instances…`}
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  aria-label="Filter instances"
                />
              </div>
            )}
            {visible.length === 0 ? (
              <EmptyState icon={Search} title="No matches" hint={`Nothing named "${query}".`} />
            ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {visible.map((inst, i) => (
              <FadeUp key={inst.id} delay={Math.min(i * 0.04, 0.2)}>
                <HoverCard onClick={() => select(inst.id)}>
                  <div className="flex items-center gap-3">
                    <div className="flex size-11 shrink-0 items-center justify-center rounded-lg border border-accent-500/25 bg-gradient-to-b from-accent-500/20 to-accent-500/5"><InstanceIcon icon={inst.icon} instanceId={inst.id} className="size-6 text-accent-400" imgClassName="size-8 rounded-lg object-cover" /></div>
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
                  </div>
                  <p className="mt-2 flex items-center gap-1.5 font-mono text-[11px] text-ink-faint">
                    <span>MC {inst.version} · {inst.settings.ramMb} MB</span>
                    {inst.id in modsCounts && (
                      <span className="flex items-center gap-1 rounded-full border border-white/10 bg-surface-200/60 px-1.5 py-px font-sans font-semibold">
                        <ModIcon className="size-3" /> {modsCounts[inst.id]}
                      </span>
                    )}
                  </p>
                  <div className="mt-3 flex gap-2" onClick={(e) => e.stopPropagation()}>
                    {runningIds.includes(inst.id) ? (
                      <Button variant="danger" size="sm" className="flex-1" onClick={() => stop(inst.id)}>
                        <Square className="fill-current" /> Stop
                      </Button>
                    ) : (
                      <Button variant="primary" size="sm" className="flex-1" onClick={() => playInstance(inst.id).catch((e) => push({ kind: "error", title: "Launch failed", body: String(e) }))}>
                        <Play /> Play
                      </Button>
                    )}
                    <Button variant="secondary" size="sm" onClick={() => select(inst.id)}>Manage</Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      aria-label={`Delete ${inst.name}`}
                      onClick={async () => {
                        if (!confirm(`Delete “${inst.name}” forever?`)) return;
                        await remove(inst.id);
                        push({ kind: "success", title: `Deleted “${inst.name}”` });
                      }}
                    >
                      <Trash2 />
                    </Button>
                  </div>
                </HoverCard>
              </FadeUp>
            ))}
          </div>
            )}
          </>
        )}
      </div>

      <CreateInstanceDialog open={creating} onClose={() => setCreating(false)} />
    </div>
  );
}
