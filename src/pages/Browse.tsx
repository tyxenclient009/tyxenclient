import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { Check, Clock, Loader2, RotateCcw } from "lucide-react";
import { FadeUp, Button } from "@/components/ui/Button";
import { Dropdown } from "@/components/ui/Dropdown";
import { CardGridSkeleton } from "@/components/ui/Skeleton";
import { EmptyState, ErrorState } from "@/components/ui/States";
import { ProjectDetail } from "@/components/dialogs/ProjectDetail";
import {
  DownloadIcon,
  ExploreIcon,
  HeartIcon,
  LoaderIcon,
  ModIcon,
  ModpackIcon,
  ResourcePackIcon,
  SearchIcon,
  ShaderIcon,
} from "@/components/icons/brand";
import { useInstances } from "@/stores/instances";
import { useToasts } from "@/stores/app";
import { downloadToInstance, installModpack } from "@/lib/backend";
import {
  LOADER_FILTERS,
  MC_FILTERS,
  formatDownloads,
  getFollows,
  getInstalled,
  getRecentSearches,
  hitCompatibility,
  isFollowed,
  markInstalled,
  pushRecentSearch,
  searchProjects,
  timeAgoIso,
  toggleFollow,
  type ProjectType,
  type SearchHit,
  type SortKey,
} from "@/lib/modrinth";
import { cn } from "@/lib/utils";

const SORTS: { id: SortKey; label: string }[] = [
  { id: "relevance", label: "Relevance" },
  { id: "downloads", label: "Downloads" },
  { id: "follows", label: "Follows" },
  { id: "updated", label: "Recently updated" },
  { id: "newest", label: "Newest" },
];

/** Modrinth sort keys the API actually supports (follows → downloads fallback). */
function apiSort(s: SortKey): SortKey {
  return s === "follows" ? "downloads" : s;
}

const TAB_ICONS: Record<ProjectType, typeof ModIcon> = {
  modpack: ModpackIcon,
  mod: ModIcon,
  resourcepack: ResourcePackIcon,
  shader: ShaderIcon,
};
const TAB_LABELS: { label: string; type: ProjectType }[] = [
  { label: "Modpacks", type: "modpack" },
  { label: "Mods", type: "mod" },
  { label: "Resource Packs", type: "resourcepack" },
  { label: "Shaders", type: "shader" },
];

const PAGE = 20;

/**
 * Projects installed in a target instance (recorded at install time +
 * verified against the real mods/resourcepacks/shaderpacks folders, so a
 * manually deleted jar stops showing Installed). Refreshes on installs and
 * target switches.
 */
function useTargetInstalled(instanceId: string | undefined, tick: number): Set<string> {
  const [set, setSet] = useState<Set<string>>(new Set());
  useEffect(() => {
    if (!instanceId) {
      setSet(new Set());
      return;
    }
    let live = true;
    (async () => {
      const { getInstalledFor } = await import("@/lib/modrinth");
      const recorded = getInstalledFor(instanceId);
      const ids = Object.keys(recorded);
      if (ids.length === 0) {
        if (live) setSet(new Set());
        return;
      }
      const { listInstanceFiles } = await import("@/lib/backend");
      const present = new Set<string>();
      for (const d of ["mods", "resourcepacks", "shaderpacks"]) {
        try {
          const fs = await listInstanceFiles(instanceId, d);
          fs.forEach((f) => {
            if (!f.isDir) present.add(f.name.toLowerCase());
          });
        } catch {
          /* a missing dir just means nothing installed there */
        }
      }
      if (!live) return;
      const ok = new Set<string>();
      for (const [pid, fn] of Object.entries(recorded)) {
        // Modpack marker (empty filename): counts only while the instance
        // actually still has files — a wiped instance loses the badge.
        if (!fn) {
          if (present.size > 0) ok.add(pid);
          continue;
        }
        if (present.has(fn.toLowerCase())) ok.add(pid);
      }
      setSet(ok);
    })().catch(() => {});
    return () => {
      live = false;
    };
  }, [instanceId, tick]);
  return set;
}

/* ── Sidebar primitives (Modrinth-app style) ─────────────────────────────── */

function SideSection({ title, action, children }: { title: string; action?: ReactNode; children: ReactNode }) {
  return (
    <section className="rounded-xl border border-white/10 bg-surface-100/70 p-3">
      <div className="mb-2 flex items-center justify-between">
        <h3 className="text-[11px] font-bold uppercase tracking-wider text-ink-faint">{title}</h3>
        {action}
      </div>
      <div className="space-y-0.5">{children}</div>
    </section>
  );
}

function CheckRow({
  checked,
  onClick,
  children,
  mono,
}: {
  checked: boolean;
  onClick: () => void;
  children: ReactNode;
  mono?: boolean;
}) {
  return (
    <button
      onClick={onClick}
      aria-pressed={checked}
      className={cn(
        "flex w-full items-center gap-2.5 rounded-lg px-2 py-1.5 text-[13px] transition",
        checked ? "bg-accent-500/10 text-ink" : "text-ink-muted hover:bg-surface-200/60 hover:text-ink",
      )}
    >
      <span
        className={cn(
          "flex size-4 shrink-0 items-center justify-center rounded border transition",
          checked ? "border-accent-500 bg-accent-500 text-white" : "border-white/20 bg-surface-300/50",
        )}
      >
        {checked && <Check className="size-3" />}
      </span>
      <span className={cn("flex min-w-0 flex-1 items-center gap-1.5 truncate", mono && "font-mono text-xs")}>
        {children}
      </span>
    </button>
  );
}

function TypeRow({
  active,
  onClick,
  icon,
  label,
}: {
  active: boolean;
  onClick: () => void;
  icon: ReactNode;
  label: string;
}) {
  return (
    <button
      onClick={onClick}
      aria-pressed={active}
      className={cn(
        "flex w-full items-center gap-2.5 rounded-lg px-2 py-1.5 text-[13px] font-medium transition",
        active ? "bg-accent-500/10 text-ink shadow-1" : "text-ink-muted hover:bg-surface-200/60 hover:text-ink",
      )}
    >
      <span className={cn(active ? "text-accent-400" : "text-ink-faint")}>{icon}</span>
      {label}
      {active && <Check className="ml-auto size-3.5 text-accent-400" />}
    </button>
  );
}

/**
 * Browse — Modrinth-app style discovery.
 * Sidebar facets (type / loaders / game versions / following) + live search,
 * 5 sorts, infinite scroll, clean list rows with author, stats and one-click
 * SHA1-verified installs.
 */
export function BrowsePage() {
  const instances = useInstances((s) => s.instances);
  const push = useToasts((s) => s.push);
  const [tab, setTab] = useState<ProjectType>("mod");
  const [query, setQuery] = useState("");
  const [debounced, setDebounced] = useState("");
  const [loaders, setLoaders] = useState<string[]>([]);
  const [versions, setVersions] = useState<string[]>([]);
  const [sort, setSort] = useState<SortKey>("relevance");
  const [followsOnly, setFollowsOnly] = useState(false);
  const [hits, setHits] = useState<SearchHit[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [more, setMore] = useState(false);
  const [error, setError] = useState("");
  const [selected, setSelected] = useState<SearchHit | null>(null);
  const [installing, setInstalling] = useState<string | null>(null);
  const [target, setTarget] = useState("");
  const [recent, setRecent] = useState<string[]>(() => getRecentSearches());
  const [followTick, setFollowTick] = useState(0);
  const [installedTick, setInstalledTick] = useState(0);
  const sentinel = useRef<HTMLDivElement>(null);

  // Debounce the query so we don't hammer the API per keystroke.
  useEffect(() => {
    const t = setTimeout(() => {
      setDebounced(query);
      if (query.trim().length >= 2) {
        pushRecentSearch(query);
        setRecent(getRecentSearches());
      }
    }, 350);
    return () => clearTimeout(t);
  }, [query]);

  const load = useCallback(
    async (offset: number, append: boolean) => {
      if (offset === 0) {
        setLoading(true);
        setError("");
      } else {
        setMore(true);
      }
      try {
        const r = await searchProjects({
          query: debounced,
          type: tab,
          loaders,
          gameVersions: versions,
          sort: apiSort(sort),
          offset,
          limit: PAGE,
        });
        let list = Array.isArray(r.hits) ? r.hits : [];
        // Client-side follows sort (API has no follows index on this mirror).
        if (sort === "follows") list = [...list].sort((a, b) => (b.follows ?? 0) - (a.follows ?? 0));
        setHits((h) => (append ? [...h, ...list] : list));
        setTotal(typeof r.total_hits === "number" ? r.total_hits : list.length);
      } catch (e) {
        if (offset === 0) setError(e instanceof Error ? e.message : String(e));
      } finally {
        setLoading(false);
        setMore(false);
      }
    },
    [debounced, tab, loaders, versions, sort],
  );

  useEffect(() => {
    load(0, false);
  }, [load]);

  // Infinite scroll — load next page when the sentinel appears.
  useEffect(() => {
    const el = sentinel.current;
    if (!el) return;
    const io = new IntersectionObserver(
      (es) => {
        if (es[0].isIntersecting && !loading && !more && hits.length < total) {
          load(hits.length, true);
        }
      },
      { rootMargin: "400px" },
    );
    io.observe(el);
    return () => io.disconnect();
  }, [hits.length, total, loading, more, load]);

  const toggle = (list: string[], v: string, set: (x: string[]) => void) =>
    set(list.includes(v) ? list.filter((x) => x !== v) : [...list, v]);

  const activeFilterCount = loaders.length + versions.length + (followsOnly ? 1 : 0);
  const clearFilters = () => {
    setLoaders([]);
    setVersions([]);
    setFollowsOnly(false);
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const followedIds = useMemo(() => new Set(getFollows()), [followTick]);
  // Per-target-instance truth: recorded installs whose file still exists.
  const targetId = target || instances[0]?.id;
  const targetInstalled = useTargetInstalled(targetId, installedTick);
  const targetInst = instances.find((i) => i.id === targetId);

  const visible = followsOnly ? hits.filter((h) => followedIds.has(h.project_id)) : hits;

  const quickInstall = async (hit: SearchHit) => {
    // Modpacks don't drop a jar into an instance — they BECOME an instance
    // (fresh isolated profile + all files + overrides, Modrinth-style).
    if (hit.project_type === "modpack") {
      setInstalling(hit.project_id);
      const { useModpackProgress } = await import("@/stores/modpackProgress");
      useModpackProgress.getState().start(hit.title || hit.slug || "modpack");
      push({ kind: "info", title: `Installing ${hit.title}`, body: "Creating a new instance. Watch the progress bar at the top." });
      const { onAssetsProgress } = await import("@/lib/backend");
      const unlisten = await onAssetsProgress((p) => {
        const pct =
          p.total_bytes > 0
            ? (p.done_bytes / p.total_bytes) * 100
            : p.total_files > 0
              ? (p.done_files / p.total_files) * 100
              : 0;
        useModpackProgress.getState().update({
          pct,
          stage: p.stage || "installing",
          current: p.current || "",
          doneFiles: p.done_files,
          totalFiles: p.total_files,
        });
      }).catch(() => () => {});
      try {
        const inst = await installModpack(hit.project_id);
        markInstalled(hit.project_id);
        const { markInstalledFor } = await import("@/lib/modrinth");
        markInstalledFor(inst.id, hit.project_id, "");
        setInstalledTick((t) => t + 1);
        const st = useInstances.getState();
        if (!st.instances.some((i) => i.id === inst.id)) {
          useInstances.setState((s) => ({ instances: [inst, ...s.instances] }));
        }
        await st.refresh().catch(() => {});
        useInstances.getState().select(inst.id);
        useModpackProgress.getState().finish();
        push({ kind: "success", title: `Installed ${inst.name}`, body: `${inst.version} · ${inst.loader} — mods, configs and overrides installed. Open Worlds to see your saves.` });
      } catch (e) {
        useModpackProgress.getState().finish();
        push({ kind: "error", title: "Modpack install failed", body: e instanceof Error ? e.message : String(e) });
      } finally {
        try {
          unlisten();
        } catch {
          /* ignore */
        }
        setInstalling(null);
      }
      return;
    }
    const id = targetId;
    if (!id) {
      push({ kind: "error", title: "No instance", body: "Create an instance first, then install." });
      return;
    }
    setInstalling(hit.project_id);
    try {
      const { getProjectVersions } = await import("@/lib/modrinth");
      const { installModWithDeps } = await import("@/lib/backend");
      // Version-aware: prefer the TARGET instance's MC version + loader
      // (Modrinth-app parity), fall back to active filters. Loaders only
      // apply to mods — resourcepacks/shaders must not be loader-filtered
      // (Modrinth 400s/empties on those queries).
      const targetInst = instances.find((i) => i.id === id);
      const isMod = hit.project_type === "mod";
      const gv = targetInst?.version ? [targetInst.version] : versions.length ? versions : undefined;
      const lv = isMod && targetInst ? [targetInst.loader.toLowerCase()].filter((l) => ["fabric","forge","quilt","neoforge"].includes(l)) : undefined;
      const effLoaders = isMod ? (lv?.length ? lv : loaders.length ? loaders : (hit.loaders ?? []).slice(0, 1)) : undefined;
      const vs = await getProjectVersions(hit.project_id, { loaders: effLoaders, gameVersions: gv });
      const v = vs[0];
      if (!v) throw new Error(`No compatible version${targetInst ? ` for ${targetInst.version} · ${targetInst.loader}` : ""} — try another instance or fewer filters`);
      const dir = (tab === "resourcepack" ? "resourcepacks" : tab === "shader" ? "shaderpacks" : "mods") as "mods" | "resourcepacks" | "shaderpacks";
      if (isMod && targetInst && targetInst.loader.toLowerCase() === "vanilla") {
        throw new Error(`${hit.title} is a mod — it needs a Fabric/Forge/Quilt/NeoForge instance, not Vanilla. Create a modded instance first.`);
      }
      const paths = await installModWithDeps(id, dir, v, {
        gameVersion: targetInst?.version,
        loader: effLoaders?.[0] ?? undefined,
      });
      markInstalled(hit.project_id);
      const { markInstalledFor } = await import("@/lib/modrinth");
      const mainFile = paths[0]?.split(/[\\/]/).pop() ?? "";
      markInstalledFor(id, hit.project_id, mainFile);
      // Pin exact version for update checks (Modrinth parity).
      try {
        const { setModMeta } = await import("@/lib/mod-meta");
        if (mainFile) setModMeta(id, hit.project_id, v, mainFile);
      } catch {
        /* meta is best-effort */
      }
      setInstalledTick((t) => t + 1);
      const extra = paths.length > 1 ? ` + ${paths.length - 1} required dependenc${paths.length - 1 === 1 ? "y" : "ies"}` : "";
      push({ kind: "success", title: `Installed ${paths[0].split(/[\\/]/).pop()}${extra}`, body: paths[0] });
    } catch (e) {
      push({ kind: "error", title: "Install failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setInstalling(null);
    }
  };

  const flipFollow = (hit: SearchHit) => {
    const now = toggleFollow(hit.project_id);
    setFollowTick((t) => t + 1);
    push({ kind: "info", title: now ? `Following ${hit.title}` : `Unfollowed ${hit.title}` });
  };

  return (
    <div className="mx-auto max-w-6xl">
      <FadeUp>
        {/* Header: title + live count + install target */}
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="flex items-center gap-2 text-2xl font-bold tracking-tight">
              <ExploreIcon className="size-6 text-accent-400" />
              Discover
              <span className="rounded-full bg-surface-200/80 px-2.5 py-0.5 font-mono text-xs font-semibold text-ink-muted">
                {total.toLocaleString()}
              </span>
            </h1>
            <p className="mt-1 text-sm text-ink-muted">
              {targetInst ? (
                <>Showing versions compatible with <span className="font-semibold text-ink">{targetInst.name}</span> first.</>
              ) : (
                <>Live from the community index.</>
              )}
            </p>
          </div>
          {instances.length > 0 && (
            <Dropdown
              ariaLabel="Install target"
              value={target}
              onChange={setTarget}
              options={[
                { value: "", label: `Install to: ${instances[0]?.name} (default)` },
                ...instances.map((i) => ({ value: i.id, label: `Install to: ${i.name}`, hint: i.version })),
              ]}
              className="h-9 w-full min-w-0 text-xs sm:w-auto sm:min-w-[220px]"
            />
          )}
        </div>

        {/* Search + sort row */}
        <div className="mt-4 flex flex-wrap items-center gap-2">
          <div className="relative min-w-0 flex-1">
            <SearchIcon className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-faint" />
            <input
              className="input pl-9"
              placeholder={`Search ${tab === "mod" ? "mods" : tab === "modpack" ? "modpacks" : tab === "resourcepack" ? "resource packs" : "shaders"} — try "sodium", "create", "shaders"…`}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              aria-label="Search"
            />
          </div>
          <Dropdown
            ariaLabel="Sort"
            value={sort}
            onChange={(v) => setSort(v as SortKey)}
            options={SORTS.map((s) => ({ value: s.id, label: s.label }))}
            className="w-full min-w-0 sm:w-auto sm:min-w-[170px]"
          />
        </div>

        {/* Recent searches */}
        {!query && recent.length > 0 && (
          <div className="mt-2 flex flex-wrap items-center gap-1.5 text-xs">
            <span className="font-semibold uppercase tracking-wider text-ink-faint">Recent</span>
            {recent.map((r) => (
              <button
                key={r}
                onClick={() => setQuery(r)}
                className="rounded-full border border-white/10 px-2.5 py-1 text-ink-muted transition hover:border-accent-500/50 hover:text-ink"
              >
                {r}
              </button>
            ))}
          </div>
        )}
      </FadeUp>

      {/* Body: sidebar + results */}
      <div className="mt-5 flex flex-col gap-4 lg:flex-row">
        {/* Sidebar facets */}
        <aside className="w-full shrink-0 space-y-3 lg:w-60">
          <SideSection
            title="Content type"
            action={
              <span className="font-mono text-[11px] text-ink-faint">
                {total.toLocaleString()}
              </span>
            }
          >
            {TAB_LABELS.map((t) => {
              const Icon = TAB_ICONS[t.type];
              return (
                <TypeRow
                  key={t.type}
                  active={tab === t.type}
                  onClick={() => setTab(t.type)}
                  icon={<Icon className="size-4" />}
                  label={t.label}
                />
              );
            })}
          </SideSection>

          <SideSection
            title="Loaders"
            action={
              loaders.length > 0 && (
                <button onClick={() => setLoaders([])} className="text-[11px] font-semibold text-accent-400 hover:underline">
                  Clear
                </button>
              )
            }
          >
            {LOADER_FILTERS.map((l) => (
              <CheckRow key={l} checked={loaders.includes(l)} onClick={() => toggle(loaders, l, setLoaders)}>
                <LoaderIcon loader={l} />
                <span className="capitalize">{l}</span>
              </CheckRow>
            ))}
          </SideSection>

          <SideSection
            title="Game versions"
            action={
              versions.length > 0 && (
                <button onClick={() => setVersions([])} className="text-[11px] font-semibold text-accent-400 hover:underline">
                  Clear
                </button>
              )
            }
          >
            {MC_FILTERS.map((v) => (
              <CheckRow key={v} checked={versions.includes(v)} onClick={() => toggle(versions, v, setVersions)} mono>
                {v}
              </CheckRow>
            ))}
          </SideSection>

          <SideSection title="Library">
            <CheckRow checked={followsOnly} onClick={() => setFollowsOnly((f) => !f)}>
              <HeartIcon filled={followsOnly} />
              Following only
            </CheckRow>
          </SideSection>

          {activeFilterCount > 0 && (
            <button
              onClick={clearFilters}
              className="flex w-full items-center justify-center gap-1.5 rounded-xl border border-dashed border-white/15 py-2 text-xs font-semibold text-ink-muted transition hover:border-accent-500/50 hover:text-ink"
            >
              <RotateCcw className="size-3.5" />
              Reset all filters ({activeFilterCount})
            </button>
          )}
        </aside>

        {/* Results */}
        <div className="min-w-0 flex-1">
          {loading ? (
            <CardGridSkeleton count={6} />
          ) : error ? (
            <ErrorState title="Mod index is unreachable" hint={error} onRetry={() => load(0, false)} />
          ) : visible.length === 0 ? (
            <EmptyState
              icon={ExploreIcon}
              title={followsOnly ? "Nothing followed yet" : "No results"}
              hint={
                followsOnly
                  ? "Tap the heart on any project to pin it here."
                  : `Nothing matches "${debounced}". Try fewer filters.`
              }
            />
          ) : (
            <>
              <div className="space-y-2">
                {visible.map((h, i) => {
                  const followed = followedIds.has(h.project_id) || isFollowed(h.project_id);
                  // Target-only: the global "ever installed" set is never
                  // pruned, so OR-ing it would badge every instance forever.
                  const installed = targetInstalled.has(h.project_id);
                  const compat = targetInst ? hitCompatibility(h, targetInst.version, targetInst.loader) : "unknown";
                  const Icon = TAB_ICONS[h.project_type] ?? ModIcon;
                  return (
                    <FadeUp key={h.project_id} delay={Math.min((i % PAGE) * 0.015, 0.15)}>
                      <article
                        onClick={() => setSelected(h)}
                        onKeyDown={(event) => {
                          if (event.target !== event.currentTarget) return;
                          if (event.key === "Enter" || event.key === " ") {
                            event.preventDefault();
                            setSelected(h);
                          }
                        }}
                        role="button"
                        tabIndex={0}
                        aria-label={`View details for ${h.title}`}
                        className="group flex cursor-pointer gap-3 rounded-xl border border-white/10 bg-surface-100/70 p-3 transition hover:border-accent-500/40 hover:bg-surface-100 hover:shadow-2 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/60"
                      >
                        {h.icon_url ? (
                          <img
                            src={h.icon_url}
                            alt=""
                            className="size-12 shrink-0 rounded-lg border border-white/10 object-cover"
                            loading="lazy"
                          />
                        ) : (
                          <div className="flex size-12 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-surface-300">
                            <Icon className="size-5 text-ink-muted" />
                          </div>
                        )}

                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-1.5">
                            <h2 className="truncate text-[15px] font-semibold tracking-tight group-hover:text-accent-400">
                              {h.title}
                            </h2>
                            {installed && (
                              <span title="Installed in the target instance" className="flex shrink-0 items-center gap-1 rounded-full bg-accent-500/15 px-1.5 py-px text-[10px] font-bold text-accent-400">
                                <Check className="size-3" /> Installed
                              </span>
                            )}
                          </div>
                          {h.author && (
                            <p className="truncate text-xs text-ink-faint">by <span className="font-medium text-ink-muted">{h.author}</span></p>
                          )}
                          <p className="mt-0.5 truncate text-[13px] text-ink-muted">{h.description}</p>

                          <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-ink-muted">
                            <span className="flex items-center gap-1" title="Downloads">
                              <DownloadIcon className="size-3.5" />
                              <span className="font-semibold text-ink">{formatDownloads(h.downloads ?? 0)}</span>
                            </span>
                            <span className="flex items-center gap-1" title="Followers">
                              <HeartIcon />
                              {formatDownloads(h.follows ?? 0)}
                            </span>
                            <span className="flex items-center gap-1" title={`Updated ${h.date_modified}`}>
                              <Clock className="size-3.5" />
                              {timeAgoIso(h.date_modified)}
                            </span>
                            {(h.loaders ?? []).slice(0, 2).map((l) => (
                              <span key={l} className="hidden items-center gap-1 rounded-full bg-surface-200/70 px-2 py-px text-[11px] font-medium sm:inline-flex">
                                <LoaderIcon loader={l} />
                                {l}
                              </span>
                            ))}
                            {(h.versions ?? []).slice(0, 1).map((v) => (
                              <span key={v} className="hidden rounded-full bg-surface-200/70 px-2 py-px font-mono text-[11px] md:inline-block">
                                {v}
                              </span>
                            ))}
                            {targetInst && compat !== "unknown" && (
                              <span className={cn(
                                "flex items-center gap-1 text-[11px] font-semibold",
                                compat === "yes" ? "text-accent-400" : "text-amber-400/90",
                              )}>
                                <span className={cn(
                                  "size-1.5 rounded-full",
                                  compat === "yes" ? "bg-accent-400" : "bg-amber-400/90",
                                )} />
                                {compat === "yes" ? "Compatible" : `Not for ${targetInst.version}`}
                              </span>
                            )}
                          </div>
                        </div>

                        <div
                          className="flex shrink-0 flex-col items-end justify-between gap-2"
                          onClick={(e) => e.stopPropagation()}
                        >
                          <button
                            title={followed ? "Unfollow" : "Follow"}
                            aria-label={followed ? `Unfollow ${h.title}` : `Follow ${h.title}`}
                            aria-pressed={followed}
                            onClick={() => flipFollow(h)}
                            className={cn(
                              "rounded-lg border p-1.5 transition active:scale-90",
                              followed
                                ? "border-accent-500/50 bg-accent-500/10 text-accent-400"
                                : "border-white/10 text-ink-faint hover:border-white/25 hover:text-ink",
                            )}
                          >
                            <HeartIcon filled={followed} />
                          </button>
                          <Button
                            variant={installed ? "secondary" : "primary"}
                            size="sm"
                            disabled={installing === h.project_id}
                            onClick={() => quickInstall(h)}
                            className="min-w-[86px]"
                          >
                            {installing === h.project_id ? (
                              <Loader2 className="animate-spin" />
                            ) : installed ? (
                              "Reinstall"
                            ) : (
                              "Install"
                            )}
                          </Button>
                        </div>
                      </article>
                    </FadeUp>
                  );
                })}
              </div>
              <div ref={sentinel} className="flex justify-center py-6">
                {more && <Loader2 className="animate-spin text-ink-faint" />}
                {!more && hits.length < total && (
                  <Button variant="secondary" size="sm" onClick={() => load(hits.length, true)}>
                    Load more ({(total - hits.length).toLocaleString()} left)
                  </Button>
                )}
                {!more && hits.length >= total && hits.length > 0 && (
                  <p className="text-xs text-ink-faint">— end of results —</p>
                )}
              </div>
            </>
          )}
        </div>
      </div>

      <ProjectDetail hit={selected} onClose={() => setSelected(null)} />
    </div>
  );
}
