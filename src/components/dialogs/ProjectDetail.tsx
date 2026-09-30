import { useEffect, useMemo, useState } from "react";
import { Calendar, Check, Loader2 } from "lucide-react";
import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { Dropdown } from "@/components/ui/Dropdown";
import { Badge } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Skeleton";
import { DownloadIcon, HeartIcon, LoaderIcon, ModIcon } from "@/components/icons/brand";
import { useInstances } from "@/stores/instances";
import { useToasts } from "@/stores/app";
import { downloadToInstance } from "@/lib/backend";
import {
  formatDownloads,
  getProject,
  getProjectVersions,
  isFollowed,
  markInstalled,
  pickFile,
  timeAgoIso,
  toggleFollow,
  versionIncompatibility,
  type Project,
  type ProjectType,
  type ProjectVersion,
  type SearchHit,
} from "@/lib/modrinth";
import { formatBytes, cn } from "@/lib/utils";

const KIND_TO_DIR = { mod: "mods", modpack: "mods", resourcepack: "resourcepacks", shader: "shaderpacks" } as const;

type DetailTab = "overview" | "versions" | "gallery";

/** Minimal markdown-ish renderer (no extra dep): headings, bold, code, lists. */
function Body({ text }: { text: string }) {
  const blocks = text.split(/\n{2,}/).slice(0, 40);
  return (
    <div className="space-y-3 text-sm leading-relaxed text-ink-muted">
      {blocks.map((b, i) => {
        if (/^#{1,3}\s/.test(b)) {
          return <h4 key={i} className="text-[15px] font-bold text-ink">{b.replace(/^#+\s*/, "")}</h4>;
        }
        if (/^(\s*[-*]\s)/m.test(b)) {
          return (
            <ul key={i} className="list-disc space-y-1 pl-5">
              {b.split("\n").map((l, j) => (
                <li key={j}>{l.replace(/^\s*[-*]\s*/, "").replace(/\*\*(.+?)\*\*/g, "$1")}</li>
              ))}
            </ul>
          );
        }
        return <p key={i} className="whitespace-pre-wrap">{b.replace(/\*\*(.+?)\*\*/g, "$1").replace(/`(.+?)`/g, "$1")}</p>;
      })}
    </div>
  );
}

/**
 * Project detail modal — Modrinth-style.
 * Header (icon, title, blurb, stats, follow), tab bar
 * (Overview / Versions / Gallery), install bar, version list with
 * game-version filter + per-file size + dependency badges.
 */
export function ProjectDetail({ hit, onClose }: { hit: SearchHit | null; onClose: () => void }) {
  const instances = useInstances((s) => s.instances);
  const push = useToasts((s) => s.push);
  const [project, setProject] = useState<Project | null>(null);
  const [versions, setVersions] = useState<ProjectVersion[]>([]);
  const [loading, setLoading] = useState(false);
  const [target, setTarget] = useState("");
  const [installing, setInstalling] = useState<string | null>(null);
  const [tab, setTab] = useState<DetailTab>("overview");
  const [versionFilter, setVersionFilter] = useState("");
  const [channelFilter, setChannelFilter] = useState<"all" | "release" | "beta" | "alpha">("all");
  const [visibleCount, setVisibleCount] = useState(20);
  const [followed, setFollowed] = useState(false);
  const [galleryIndex, setGalleryIndex] = useState(0);

  useEffect(() => {
    if (!hit) return;
    setProject(null);
    setVersions([]);
    setLoading(true);
    setTab("overview");
    setVersionFilter("");
    setChannelFilter("all");
    setVisibleCount(20);
    setGalleryIndex(0);
    setFollowed(isFollowed(hit.project_id));
    // Version-aware: ask the API for the target-relevant slice first
    // (Modrinth parity) instead of an unfiltered dump. Fetch up to 100 so
    // the Versions tab can page + filter by channel.
    const targetInst = useInstances.getState().instances[0];
    const lv = targetInst ? [targetInst.loader.toLowerCase()].filter((l) => ["fabric", "forge", "quilt", "neoforge"].includes(l)) : undefined;
    const gv = targetInst?.version ? [targetInst.version] : undefined;
    Promise.all([getProject(hit.project_id), getProjectVersions(hit.project_id, { loaders: lv, gameVersions: gv, limit: 100 }).catch(() => getProjectVersions(hit.project_id, { limit: 100 }))])
      .then(([p, v]) => {
        setProject(p);
        setVersions(v.slice(0, 100));
        setTarget((t) => t || useInstances.getState().instances[0]?.id || "");
      })
      .catch((e) => push({ kind: "error", title: "Couldn't load project", body: String(e) }))
      .finally(() => setLoading(false));
  }, [hit?.project_id]); // eslint-disable-line react-hooks/exhaustive-deps

  // Re-filter versions whenever the TARGET instance changes — the initial
  // fetch is scoped to instances[0], which may differ from the chosen target.
  useEffect(() => {
    const pid = hit?.project_id;
    if (!pid || !target) return;
    let live = true;
    const inst = useInstances.getState().instances.find((i) => i.id === target);
    const ptype = project?.project_type ?? hit?.project_type ?? "mod";
    const lv = ptype === "mod" && inst ? [inst.loader.toLowerCase()].filter((l) => ["fabric", "forge", "quilt", "neoforge"].includes(l)) : undefined;
    const gv = inst?.version ? [inst.version] : undefined;
    getProjectVersions(pid, { loaders: lv, gameVersions: gv, limit: 100 })
      .catch(() => getProjectVersions(pid, { limit: 100 }))
      .then((v) => { if (live) { setVersions(v.slice(0, 100)); setVisibleCount(20); } })
      .catch((e) => push({ kind: "error", title: "Couldn't load versions", body: String(e) }));
    return () => { live = false; };
  }, [target, hit?.project_id]); // eslint-disable-line react-hooks/exhaustive-deps

  const gameVersions = useMemo(() => {
    const set = new Set<string>();
    versions.forEach((v) => (v.game_versions ?? []).forEach((g) => set.add(g)));
    return [...set].slice(0, 12);
  }, [versions]);

  const filtered = useMemo(() => {
    let list = versionFilter ? versions.filter((v) => (v.game_versions ?? []).includes(versionFilter)) : versions;
    if (channelFilter !== "all") list = list.filter((v) => v.version_type === channelFilter);
    return list;
  }, [versions, versionFilter, channelFilter]);
  const visible = filtered.slice(0, visibleCount);

  const install = async (v: ProjectVersion) => {
    // Modpacks become their own instance (fresh profile + files + overrides).
    if ((project?.project_type ?? hit?.project_type ?? "mod") === "modpack") {
      setInstalling(v.id);
      const { useModpackProgress } = await import("@/stores/modpackProgress");
      const packName = project?.title ?? hit?.title ?? "modpack";
      useModpackProgress.getState().start(packName);
      push({ kind: "info", title: `Installing ${packName}`, body: "Creating a new instance. Watch the progress bar at the top." });
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
        const { installModpack } = await import("@/lib/backend");
        const inst = await installModpack(project?.id ?? hit?.project_id ?? "", v.id);
        markInstalled(hit?.project_id ?? project?.id ?? "");
        const { markInstalledFor } = await import("@/lib/modrinth");
        markInstalledFor(inst.id, hit?.project_id ?? project?.id ?? "", "");
        const st = useInstances.getState();
        if (!st.instances.some((i) => i.id === inst.id)) {
          useInstances.setState((s) => ({ instances: [inst, ...s.instances] }));
        }
        await st.refresh().catch(() => {});
        useInstances.getState().select(inst.id);
        useModpackProgress.getState().finish();
        push({ kind: "success", title: `Installed ${inst.name}`, body: `${inst.version} · ${inst.loader} — mods, configs and overrides installed. Open Worlds to see your saves.` });
        onClose();
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
    if (!target) {
      push({ kind: "error", title: "No instance selected", body: "Create an instance first." });
      return;
    }
    const file = pickFile(v);
    if (!file) {
      push({ kind: "error", title: "No downloadable file on this version" });
      return;
    }
    const ptype = project?.project_type ?? hit?.project_type ?? "mod";
    const dir = KIND_TO_DIR[ptype] ?? "mods";
    const inst = instances.find((i) => i.id === target);
    if (ptype === "mod" && inst && inst.loader.toLowerCase() === "vanilla") {
      push({ kind: "error", title: "Needs a modded instance", body: "Mods can't install on Vanilla — create a Fabric/Forge/Quilt/NeoForge instance first." });
      return;
    }
    setInstalling(v.id);
    try {
      const { installModWithDeps } = await import("@/lib/backend");
      const paths = await installModWithDeps(target, dir, v, {
        gameVersion: inst?.version,
        loader: inst ? inst.loader.toLowerCase() : undefined,
      });
      markInstalled(hit?.project_id ?? project?.id ?? "");
      const { markInstalledFor } = await import("@/lib/modrinth");
      const pid = hit?.project_id ?? project?.id ?? "";
      const mainFile = paths[0]?.split(/[\\/]/).pop() ?? "";
      markInstalledFor(target, pid, mainFile);
      try {
        const { setModMeta } = await import("@/lib/mod-meta");
        if (mainFile && pid) setModMeta(target, pid, v, mainFile);
      } catch {
        /* meta is best-effort */
      }
      const mainName = paths[0]?.split(/[\\/]/).pop() ?? file?.filename ?? "file";
      const extra = paths.length > 1 ? ` + ${paths.length - 1} deps` : "";
      push({ kind: "success", title: `Installed ${mainName} (${formatBytes(file?.size ?? 0)})${extra}`, body: paths[0] });
    } catch (e) {
      push({ kind: "error", title: "Install failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setInstalling(null);
    }
  };

  const flipFollow = () => {
    if (!hit && !project) return;
    const id = hit?.project_id ?? project!.id;
    const now = toggleFollow(id);
    setFollowed(now);
    push({ kind: "info", title: now ? `Following ${project?.title ?? hit?.title}` : "Unfollowed" });
  };

  return (
    <Modal open={!!hit} onClose={onClose} wide>
      {!hit ? null : loading || !project ? (
        <div className="space-y-3">
          <Skeleton className="h-8 w-1/2" />
          <Skeleton className="h-40 w-full" />
          <Skeleton className="h-24 w-full" />
        </div>
      ) : (
        <div>
          {/* Header — Modrinth style */}
          <div className="flex items-start gap-4">
            {project.icon_url ? (
              <img src={project.icon_url} alt="" className="size-16 shrink-0 rounded-xl border" />
            ) : (
              <div className="flex size-16 shrink-0 items-center justify-center rounded-xl border bg-surface-300">
                <ModIcon className="size-7 text-ink-muted" />
              </div>
            )}
            <div className="min-w-0 flex-1">
              <div className="flex items-start gap-2">
                <h2 className="min-w-0 flex-1 truncate text-xl font-bold tracking-tight">{project.title}</h2>
                <button
                  onClick={flipFollow}
                  aria-pressed={followed}
                  title={followed ? "Unfollow" : "Follow"}
                  className={cn(
                    "flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-semibold transition active:scale-95",
                    followed
                      ? "border-accent-500/60 bg-accent-500/10 text-accent-400"
                      : "text-ink-muted hover:text-ink",
                  )}
                >
                  <HeartIcon filled={followed} />
                  {followed ? "Following" : "Follow"}
                </button>
              </div>
              <p className="mt-0.5 line-clamp-2 text-sm text-ink-muted">{project.description}</p>
              {hit?.author && (
                <p className="mt-0.5 text-xs text-ink-faint">
                  by <span className="font-medium text-ink-muted">{hit.author}</span>
                </p>
              )}
              {/* Stats row */}
              <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-[12px] text-ink-muted">
                <span className="flex items-center gap-1.5">
                  <DownloadIcon className="size-3.5" />
                  <strong className="text-ink">{formatDownloads(project.downloads)}</strong> downloads
                </span>
                <span className="flex items-center gap-1.5">
                  <HeartIcon />
                  <strong className="text-ink">{formatDownloads(project.followers)}</strong> followers
                </span>
                <span className="flex items-center gap-1.5">
                  <Calendar className="size-3.5" />
                  Updated {timeAgoIso(versions[0]?.date_published)}
                </span>
              </div>
              <div className="mt-2 flex flex-wrap gap-1.5">
                <Badge tone="accent">{project.project_type}</Badge>
                {(project.loaders ?? []).slice(0, 4).map((l) => (
                  <Badge key={l}>
                    <LoaderIcon loader={l} /> {l}
                  </Badge>
                ))}
                {(project.game_versions ?? []).slice(0, 3).map((g) => (
                  <Badge key={g} className="font-mono">{g}</Badge>
                ))}
                {project.client_side && project.client_side !== "unknown" && (
                  <Badge>client: {project.client_side}</Badge>
                )}
                {project.server_side && project.server_side !== "unknown" && (
                  <Badge>server: {project.server_side}</Badge>
                )}
                {project.license?.name && (
                  <Badge>{project.license.name}</Badge>
                )}
              </div>
              <p className="mt-1 font-mono text-[11px] text-ink-faint">{project.slug}</p>
            </div>
          </div>

          {/* Install bar */}
          <div className="mt-4 flex gap-2 rounded-md border border-white/10 bg-surface-50 p-2">
            <Dropdown
              ariaLabel="Target instance"
              value={target}
              onChange={setTarget}
              options={instances.map((i) => ({ value: i.id, label: i.name, hint: i.version }))}
              placeholder="No instances — create one first"
              className="max-w-[220px]"
            />
            <Button
              variant="primary"
              size="sm"
              className="flex-1"
              disabled={!versions.length || installing !== null}
              onClick={() => versions[0] && install(versions[0])}
            >
              {installing ? <Loader2 className="animate-spin" /> : <DownloadIcon />} Install latest
              {versions[0] ? ` · ${versions[0].version_number}` : ""}
            </Button>
          </div>

          {/* Tabs */}
          <div className="mt-4 flex gap-1 border-b" role="tablist" aria-label="Project sections">
            {(["overview", "versions", "gallery"] as DetailTab[]).map((t) => (
              <button
                key={t}
                role="tab"
                aria-selected={tab === t}
                onClick={() => setTab(t)}
                className={cn(
                  "rounded-t-md px-3 py-2 text-[13px] font-semibold capitalize transition",
                  tab === t
                    ? "bg-surface-200/70 text-ink"
                    : "text-ink-muted hover:bg-surface-200/40 hover:text-ink",
                )}
              >
                {t}
                {t === "versions" && (
                  <span className="ml-1.5 rounded-full bg-surface-300 px-1.5 py-px font-mono text-[11px]">
                    {filtered.length}
                  </span>
                )}
                {t === "gallery" && (project.gallery ?? []).length > 0 && (
                  <span className="ml-1.5 rounded-full bg-surface-300 px-1.5 py-px font-mono text-[11px]">
                    {(project.gallery ?? []).length}
                  </span>
                )}
              </button>
            ))}
          </div>

          {tab === "overview" && (
            <div className="mt-3">
              <h3 className="text-sm font-bold uppercase tracking-wider text-ink-muted">About</h3>
              <div className="mt-2 max-h-64 overflow-y-auto rounded-md border bg-surface-50 p-3">
                <Body text={project.body || project.description} />
              </div>
              {(project.categories ?? []).length > 0 && (
                <div className="mt-3 flex flex-wrap gap-1.5">
                  {(project.categories ?? []).map((c) => (
                    <Badge key={c}>{c}</Badge>
                  ))}
                </div>
              )}
            </div>
          )}

          {tab === "versions" && (
            <div className="mt-3">
              {gameVersions.length > 0 && (
                <div className="mb-2 flex flex-wrap items-center gap-1.5">
                  <span className="text-xs font-semibold uppercase tracking-wider text-ink-faint">MC</span>
                  <button
                    onClick={() => { setVersionFilter(""); setVisibleCount(20); }}
                    className={cn(
                      "rounded-full border px-2 py-0.5 font-mono text-[11px] transition",
                      !versionFilter ? "border-accent-500/60 bg-accent-500/10 text-ink" : "text-ink-muted",
                    )}
                  >
                    all
                  </button>
                  {gameVersions.map((g) => (
                    <button
                      key={g}
                      onClick={() => { setVersionFilter((f) => (f === g ? "" : g)); setVisibleCount(20); }}
                      className={cn(
                        "rounded-full border px-2 py-0.5 font-mono text-[11px] transition",
                        versionFilter === g
                          ? "border-accent-500/60 bg-accent-500/10 text-ink"
                          : "text-ink-muted hover:text-ink",
                      )}
                    >
                      {g}
                    </button>
                  ))}
                </div>
              )}
              <div className="mb-2 flex flex-wrap items-center gap-1.5">
                <span className="text-xs font-semibold uppercase tracking-wider text-ink-faint">Channel</span>
                {(["all", "release", "beta", "alpha"] as const).map((c) => (
                  <button
                    key={c}
                    onClick={() => { setChannelFilter(c); setVisibleCount(20); }}
                    className={cn(
                      "rounded-full border px-2 py-0.5 text-[11px] font-medium capitalize transition",
                      channelFilter === c
                        ? "border-accent-500/60 bg-accent-500/10 text-ink"
                        : "text-ink-muted hover:text-ink",
                    )}
                  >
                    {c}
                  </button>
                ))}
                <span className="ml-auto font-mono text-[11px] text-ink-faint">
                  {filtered.length} version{filtered.length === 1 ? "" : "s"}
                </span>
              </div>
              <div className="max-h-72 space-y-1.5 overflow-y-auto pr-1">
                {visible.map((v) => {
                  const file = pickFile(v);
                  const targetInst = instances.find((i) => i.id === target);
                  const reason = targetInst ? versionIncompatibility(v, targetInst.version, targetInst.loader) : null;
                  const compatible = !reason;
                  return (
                    <div key={v.id} className="flex items-center gap-3 rounded-md border bg-surface-50 px-3 py-2 text-sm">
                      <div className="min-w-0 flex-1">
                        <p className="flex items-center gap-1.5 truncate font-semibold">
                          <span className="truncate">{v.name}</span>
                          {(v.dependencies ?? []).some((d) => d.dependency_type === "required") && (
                            <Badge tone="warn" className="shrink-0">needs deps</Badge>
                          )}
                          {target && (
                            <span title={reason ?? undefined}>
                              <Badge tone={compatible ? "accent" : "neutral"} className="shrink-0">
                                {compatible ? "✓ fits instance" : reason ?? "not for instance"}
                              </Badge>
                            </span>
                          )}
                        </p>
                        <p className="font-mono text-[11px] text-ink-faint">
                          {(v.game_versions ?? []).slice(0, 4).join(", ")} · {(v.loaders ?? []).join(", ")} ·{" "}
                          {formatDownloads(v.downloads)} · {file ? formatBytes(file.size) : "?"} ·{" "}
                          {timeAgoIso(v.date_published)}
                        </p>
                        {(v.dependencies ?? []).filter((d) => d.project_id).length > 0 && (
                          <p className="mt-0.5 truncate text-[11px] text-ink-faint">
                            Requires: {(v.dependencies ?? []).filter((d) => d.project_id).length} project(s)
                          </p>
                        )}
                      </div>
                      <Badge tone={v.version_type === "release" ? "accent" : "warn"}>{v.version_type}</Badge>
                      <Button variant="secondary" size="sm" disabled={installing !== null} onClick={() => install(v)}>
                        {installing === v.id ? <Loader2 className="animate-spin" /> : <DownloadIcon />}
                      </Button>
                    </div>
                  );
                })}
                {filtered.length === 0 && <p className="text-sm text-ink-faint">No versions for this filter.</p>}
                {visible.length < filtered.length && (
                  <button
                    onClick={() => setVisibleCount((c) => c + 20)}
                    className="w-full rounded-md border border-dashed py-2 text-xs font-semibold text-ink-muted transition hover:border-accent-500/50 hover:text-ink"
                  >
                    Show more ({filtered.length - visible.length} left)
                  </button>
                )}
              </div>
            </div>
          )}

          {tab === "gallery" && (
            <div className="mt-3">
              {(project.gallery ?? []).length === 0 ? (
                <p className="text-sm text-ink-faint">No gallery images published.</p>
              ) : (
                <>
                  <img
                    src={(project.gallery ?? [])[Math.min(galleryIndex, (project.gallery ?? []).length - 1)]?.url}
                    alt={(project.gallery ?? [])[Math.min(galleryIndex, (project.gallery ?? []).length - 1)]?.title}
                    className="h-64 w-full rounded-md border object-cover"
                  />
                  <div className="mt-2 grid grid-cols-6 gap-2">
                    {(project.gallery ?? []).slice(0, 6).map((g, i) => (
                      <button
                        key={g.url}
                        onClick={() => setGalleryIndex(i)}
                        className={cn(
                          "overflow-hidden rounded-md border transition",
                          i === galleryIndex ? "border-accent-500/70 ring-2 ring-accent-500/25" : "opacity-70 hover:opacity-100",
                        )}
                      >
                        <img src={g.url} alt={g.title} className="h-12 w-full object-cover" loading="lazy" />
                      </button>
                    ))}
                  </div>
                </>
              )}
            </div>
          )}

          <p className="mt-4 flex items-center gap-1.5 text-[11px] text-ink-faint">
            <Check className="size-3 text-accent-400" /> All downloads are SHA1-verified against publisher hashes
            before touching your instance.
          </p>
        </div>
      )}
    </Modal>
  );
}
