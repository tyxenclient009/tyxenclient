import { useEffect, useState } from "react";
import { Check, Download, Loader2, Plus, Search } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Skeleton";
import { DownloadIcon, LoaderIcon, ModIcon, ResourcePackIcon, ShaderIcon } from "@/components/icons/brand";
import { useToasts } from "@/stores/app";
import { downloadToInstance } from "@/lib/backend";
import {
  formatDownloads,
  getProjectVersions,
  markInstalled,
  pickFile,
  searchProjects,
  type ProjectType,
  type SearchHit,
} from "@/lib/modrinth";
import { formatBytes, cn } from "@/lib/utils";

type ContentType = "mod" | "resourcepack" | "shader";

const TYPE_TABS: { id: ContentType; label: string; Icon: typeof ModIcon; dir: "mods" | "resourcepacks" | "shaderpacks" }[] = [
  { id: "mod", label: "Mods", Icon: ModIcon, dir: "mods" },
  { id: "resourcepack", label: "Resources", Icon: ResourcePackIcon, dir: "resourcepacks" },
  { id: "shader", label: "Shaders", Icon: ShaderIcon, dir: "shaderpacks" },
];

/** Instance loader name → Modrinth facet (Vanilla = no loader filter). */
function loaderFacet(loader: string): string[] {
  const l = loader.toLowerCase();
  if (l.includes("neoforge")) return ["neoforge"];
  if (l.includes("fabric")) return ["fabric"];
  if (l.includes("forge")) return ["forge"];
  if (l.includes("quilt")) return ["quilt"];
  return [];
}

/**
 * Per-instance Modrinth installer — lives at the top of the instance's
 * Mods tab. Search is pre-filtered to THIS instance's loader + MC version,
 * and installs the newest COMPATIBLE version (facets passed to the versions
 * endpoint), SHA1-verified straight into this instance's folder.
 */
export function InstanceModInstall({
  instanceId,
  version,
  loader,
  onInstalled,
}: {
  instanceId: string;
  version: string;
  loader: string;
  onInstalled: () => void;
}) {
  const push = useToasts((s) => s.push);
  const [open, setOpen] = useState(false);
  const [type, setType] = useState<ContentType>("mod");
  const [query, setQuery] = useState("");
  const [debounced, setDebounced] = useState("");
  const [hits, setHits] = useState<SearchHit[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [installing, setInstalling] = useState<string | null>(null);
  const [done, setDone] = useState<Set<string>>(new Set());

  const facets = loaderFacet(loader);
  const dir = TYPE_TABS.find((t) => t.id === type)!.dir;
  // Loaders only apply to mods. Resource packs/shaders must not be
  // loader-filtered (Modrinth 400s/empties on those queries).
  const isModTab = type === "mod";
  const versionLoaders = isModTab && facets.length ? facets : undefined;
  const isVanillaMods = isModTab && facets.length === 0;

  useEffect(() => {
    const t = setTimeout(() => setDebounced(query), 350);
    return () => clearTimeout(t);
  }, [query]);

  useEffect(() => {
    if (!open) return;
    let cancelled = false;
    setLoading(true);
    setError("");
    searchProjects({
      query: debounced,
      type: type as ProjectType,
      loaders: versionLoaders,
      gameVersions: version ? [version] : [],
      sort: debounced ? "relevance" : "downloads",
      offset: 0,
      limit: 8,
    })
      .then((r) => {
        if (!cancelled) setHits(Array.isArray(r.hits) ? r.hits : []);
      })
      .catch((e) => {
        if (!cancelled) setError(e instanceof Error ? e.message : String(e));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, debounced, type, version, loader]);

  const install = async (hit: SearchHit) => {
    // Vanilla instances can't run mods at all — block before downloading
    // an incompatible jar (packs/shaders are fine).
    if (isVanillaMods) {
      push({ kind: "error", title: "Needs a modded instance", body: "This is a Vanilla instance — mods need Fabric/Forge/Quilt/NeoForge. Resource packs and shaders work here." });
      return;
    }
    setInstalling(hit.project_id);
    try {
      const vs = await getProjectVersions(hit.project_id, {
        loaders: versionLoaders,
        gameVersions: version ? [version] : undefined,
      });
      const v = vs[0];
      if (!v) throw new Error(`No ${version || ""} ${versionLoaders?.join("/") ?? ""} version published`);
      const { installModWithDeps } = await import("@/lib/backend");
      const paths = await installModWithDeps(instanceId, dir, v, {
        gameVersion: version,
        loader: versionLoaders?.[0],
      });
      markInstalled(hit.project_id);
      const { markInstalledFor } = await import("@/lib/modrinth");
      const mainFile = paths[0]?.split(/[\\/]/).pop() ?? "";
      markInstalledFor(instanceId, hit.project_id, mainFile);
      try {
        const { setModMeta } = await import("@/lib/mod-meta");
        if (mainFile) setModMeta(instanceId, hit.project_id, v, mainFile);
      } catch {
        /* meta best-effort */
      }
      setDone((d) => new Set(d).add(hit.project_id));
      const mainName = paths[0]?.split(/[\\/]/).pop() ?? "file";
      const extra = paths.length > 1 ? ` + ${paths.length - 1} deps` : "";
      push({ kind: "success", title: `Installed ${mainName}${extra}`, body: paths[0] });
      onInstalled();
    } catch (e) {
      push({ kind: "error", title: "Install failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setInstalling(null);
    }
  };

  return (
    <div className="mb-3 overflow-hidden rounded-lg border border-white/10 bg-surface-50/60">
      <button
        onClick={() => setOpen((o) => !o)}
        aria-expanded={open}
        className="flex w-full items-center gap-2 px-3 py-2.5 text-left transition hover:bg-surface-200/40"
      >
        <span className="flex size-7 items-center justify-center rounded-md bg-accent-500/15 text-accent-400">
          <Plus className="size-4" />
        </span>
        <span className="flex-1 text-sm font-semibold">Install mods into this instance</span>
        <span className="flex items-center gap-1">
          {facets.map((f) => (
            <Badge key={f}><LoaderIcon loader={f} /> {f}</Badge>
          ))}
          {version && <Badge className="font-mono">{version}</Badge>}
        </span>
      </button>

      {open && (
        <div className="border-t border-white/10 p-3">
          <div className="flex flex-wrap items-center gap-2">
            <div className="relative min-w-[160px] flex-1">
              <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-ink-faint" />
              <input
                className="input h-9 pl-8 text-[13px]"
                placeholder={`Search ${type}s…`}
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                aria-label="Search mods for this instance"
              />
            </div>
            <div className="flex gap-1 rounded-md border border-white/10 bg-surface-100/60 p-0.5">
              {TYPE_TABS.map((t) => {
                const Icon = t.Icon;
                const active = type === t.id;
                return (
                  <button
                    key={t.id}
                    onClick={() => setType(t.id)}
                    aria-pressed={active}
                    className={cn(
                      "flex items-center gap-1 rounded px-2 py-1 text-xs font-medium transition",
                      active ? "bg-surface-300 text-ink shadow-1" : "text-ink-muted hover:text-ink",
                    )}
                  >
                    <Icon className={cn("size-3.5", active ? "text-accent-400" : "text-ink-faint")} />
                    {t.label}
                  </button>
                );
              })}
            </div>
          </div>

          <div className="mt-2 max-h-72 space-y-1.5 overflow-y-auto pr-0.5">
            {loading ? (
              <>
                <Skeleton className="h-14 w-full" />
                <Skeleton className="h-14 w-full" />
                <Skeleton className="h-14 w-full" />
              </>
            ) : error ? (
              <div className="rounded-md border border-red-500/25 bg-red-500/5 px-3 py-3 text-center text-[13px]">
                <p className="font-semibold text-red-400">Mod index unreachable</p>
                <p className="mt-0.5 font-mono text-[11px] text-ink-muted">{error}</p>
              </div>
            ) : hits.length === 0 ? (
              <p className="px-1 py-3 text-center text-[13px] text-ink-faint">
                {debounced ? `Nothing compatible with ${loader} ${version}. Try fewer words.` : "Type to search — blank shows the most popular."}
              </p>
            ) : (
              hits.map((h) => {
                const wasInstalled = done.has(h.project_id);
                const busy = installing === h.project_id;
                return (
                  <div key={h.project_id} className="flex items-center gap-2.5 rounded-md border border-white/10 bg-surface-100/60 px-2.5 py-2">
                    {h.icon_url ? (
                      <img src={h.icon_url} alt="" className="size-9 shrink-0 rounded-md border" loading="lazy" />
                    ) : (
                      <div className="flex size-9 shrink-0 items-center justify-center rounded-md border bg-surface-300">
                        <DownloadIcon className="size-4 text-ink-muted" />
                      </div>
                    )}
                    <div className="min-w-0 flex-1">
                      <p className="flex items-center gap-1.5 truncate text-[13px] font-semibold">
                        <span className="truncate">{h.title ?? h.slug}</span>
                        {wasInstalled && <Check className="size-3.5 shrink-0 text-accent-400" />}
                      </p>
                      <p className="truncate text-xs text-ink-muted">
                        ⬇ {formatDownloads(h.downloads ?? 0)} · {(h.versions ?? []).slice(0, 2).join(", ")}
                      </p>
                    </div>
                    <Button variant={wasInstalled ? "secondary" : "primary"} size="sm" disabled={busy} onClick={() => install(h)}>
                      {busy ? <Loader2 className="animate-spin" /> : wasInstalled ? <Check /> : <Download />}
                      {busy ? "" : wasInstalled ? "" : "Get"}
                    </Button>
                  </div>
                );
              }))}
          </div>
          <p className="mt-2 text-[11px] text-ink-faint">
            Only versions matching <span className="font-semibold text-ink-muted">{loader} · {version}</span> are offered — SHA1-verified into this instance's <span className="font-mono">{dir}/</span>.
          </p>
        </div>
      )}
    </div>
  );
}
