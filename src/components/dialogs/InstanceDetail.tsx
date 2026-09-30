import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useState } from "react";
import { Download, Pencil, Play, Square, Trash2, X, FolderOpen, Settings2, ChevronLeft, ChevronRight, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { Badge } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/States";
import { FolderIcon, LoaderIcon, LogsIcon, ModIcon, ShotsIcon, WorldIcon } from "@/components/icons/brand";
import { useInstances } from "@/stores/instances";
import { useLaunch } from "@/stores/launch";
import { useToasts } from "@/stores/app";
import { formatBytes, cn } from "@/lib/utils";
import * as api from "@/lib/backend";
import { playInstance } from "@/lib/play";
import { InstanceIcon } from "@/components/icons/instances";
import { InstanceModInstall } from "./InstanceModInstall";
import { resolveModIdentity, type ModIdentity } from "@/lib/modrinth";
import { getModMeta } from "@/lib/mod-meta";
import { countUserMods, isManagedMod } from "@/lib/tyxmod";
import { useScrollLock } from "@/lib/scroll-lock";

const TABS = [
  { id: "mods", label: "Mods", icon: ModIcon },
  { id: "saves", label: "Worlds", icon: WorldIcon },
  { id: "screenshots", label: "Shots", icon: ShotsIcon },
  { id: "logs", label: "Logs", icon: LogsIcon },
  { id: "settings", label: "Settings", icon: Settings2 },
] as const;

type Tab = (typeof TABS)[number]["id"];

/**
 * Instance detail drawer — mods/worlds/screenshots/logs tabs + per-instance
 * settings (RAM slider, Java override, JVM args, resolution). Right-side
 * slide-over so the grid stays visible underneath.
 */
export function InstanceDetail() {
  const instances = useInstances((s) => s.instances);
  const selectedId = useInstances((s) => s.selectedId);
  const select = useInstances((s) => s.select);
  const saveSettings = useInstances((s) => s.saveSettings);
  const setIcon = useInstances((s) => s.setIcon);
  const remove = useInstances((s) => s.remove);
  const push = useToasts((s) => s.push);
  const runningIds = useLaunch((s) => s.runningIds);

  const onStop = async () => {
    if (!inst) return;
    try {
      await api.stopGame(inst.id);
    } catch (e) {
      push({ kind: "error", title: "Couldn't stop", body: e instanceof Error ? e.message : String(e) });
    }
  };

  const openFolder = async (subdir?: string) => {
    if (!inst || opening) return;
    const key = subdir || "root";
    setOpening(key);
    try {
      await api.openInstanceFolder(inst.id, subdir);
    } catch (e) {
      push({ kind: "error", title: "Couldn't open folder", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setOpening(null);
    }
  };

  const inst = instances.find((i) => i.id === selectedId);
  const [tab, setTab] = useState<Tab>("mods");
  const [files, setFiles] = useState<api.FileEntry[]>([]);
  const [modQuery, setModQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [draft, setDraft] = useState<api.InstanceSettings | null>(null);
  const [launching, setLaunching] = useState(false);
  /** Bumped after installs so the file list refetches. */
  const [filesTick, setFilesTick] = useState(0);
  /** Screenshot data-URL cache (filename → image) for the Shots gallery. */
  const [shotUrls, setShotUrls] = useState<Record<string, string>>({});
  /** Lightbox index into the sorted shots list (null = closed). */
  const [lightbox, setLightbox] = useState<number | null>(null);
  /** Per-tab file counts for the pill badges. */
  const [counts, setCounts] = useState<Record<string, number>>({});
  const [iconBusy, setIconBusy] = useState(false);
  const [opening, setOpening] = useState<string | null>(null);
  /** Modrinth update sweep results (null = not checked yet). */
  const [updates, setUpdates] = useState<import("@/lib/updates").ModUpdate[] | null>(null);
  const [tyxenUpdate, setTyxenUpdate] = useState<import("@/lib/tyxmod").TyxenUpdate | null>(null);
  const [updatingTyxen, setUpdatingTyxen] = useState(false);
  const [checking, setChecking] = useState(false);
  const [updatingAll, setUpdatingAll] = useState(false);

  useEffect(() => {
    setTab("mods");
    setCounts({});
    setFilesTick((t) => t + 1);
    setUpdates(null);
    if (inst) setDraft({ ...inst.settings });
  }, [selectedId]); // eslint-disable-line react-hooks/exhaustive-deps

  // Drawer open = background stays put (same lock as every Modal).
  useScrollLock(!!inst);

  // Preload counts for every file tab the moment the drawer opens, so
  // Worlds / Shots / Logs badges show immediately (no need to visit each
  // tab first). Failures stay silent — the per-tab loader reports them.
  useEffect(() => {
    if (!inst) return;
    let live = true;
    Promise.allSettled(
      ["mods", "saves", "screenshots", "logs"].map(async (kind) => {
        const all = await api.listInstanceFiles(inst.id, kind);
        // Managed stack (Tyxen, Fabric API) stays hidden like Dawn —
        // users only ever see their own files here. The mods badge uses
        // the shared counter so Home/Instances/drawer always agree.
        const files =
          kind === "mods" ? all.filter((f) => f.isDir || !isManagedMod(f.name)) : all;
        return { kind, files, count: kind === "mods" ? countUserMods(all) : files.length };
      }),
    ).then((rs) => {
      if (!live) return;
      const next: Record<string, number> = {};
      rs.forEach((r) => {
        if (r.status === "fulfilled") next[r.value.kind] = r.value.count;
      });
      setCounts((c) => ({ ...c, ...next }));
    });
    return () => {
      live = false;
    };
  }, [inst?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!inst || tab === "settings") return;
    const kind = tab === "saves" ? "saves" : tab;
    setLoading(true);
    api
      .listInstanceFiles(inst.id, kind)
      .then((all) => {
        const f =
          kind === "mods" ? all.filter((x) => x.isDir || !isManagedMod(x.name)) : all;
        setFiles(f);
        setCounts((c) => ({ ...c, [kind]: kind === "mods" ? countUserMods(all) : f.length }));
      })
      .catch((e) => push({ kind: "error", title: `Couldn't list ${tab}`, body: String(e) }))
      .finally(() => setLoading(false));
  }, [inst?.id, tab, filesTick]); // eslint-disable-line react-hooks/exhaustive-deps

  // Prune cached screenshot images for files that no longer exist (or when
  // switching instances) so deleted shots never render stale.
  useEffect(() => {
    setShotUrls((prev) => {
      if (!inst) return {};
      const alive = new Set(files.map((f) => f.name));
      let dirty = false;
      const next: Record<string, string> = {};
      for (const [k, v] of Object.entries(prev)) {
        const [iid, ...rest] = k.split("::");
        const fn = rest.join("::");
        if (iid === inst.id && alive.has(fn)) next[k] = v;
        else dirty = true;
      }
      return dirty ? next : prev;
    });
    setLightbox(null);
  }, [inst?.id, files]); // eslint-disable-line react-hooks/exhaustive-deps

  if (!inst) return null;

  const onPlay = async () => {
    setLaunching(true);
    try {
      await playInstance(inst.id);
    } catch (e) {
      push({ kind: "error", title: "Launch failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setLaunching(false);
    }
  };

  const onDeleteFile = async (name: string) => {
    try {
      const { clearModMeta } = await import("@/lib/mod-meta");
      clearModMeta(inst.id, name);
    } catch {
      /* ignore */
    }
    await api.deleteInstanceFile(inst.id, tab, name);
    setFiles((f) => f.filter((x) => x.name !== name));
    setUpdates((u) => (u ? u.filter((x) => x.file !== name) : u));
    push({ kind: "success", title: `Removed ${name}` });
  };

  const checkUpdates = async () => {
    if (!inst || checking) return;
    setChecking(true);
    try {
      const { checkForUpdates } = await import("@/lib/updates");
      const { checkTyxenUpdate } = await import("@/lib/tyxmod");
      const [{ updates: found, checked }, tyx] = await Promise.all([
        checkForUpdates(inst.id, inst.version, inst.loader),
        checkTyxenUpdate(inst.id),
      ]);
      setUpdates(found);
      setTyxenUpdate(tyx);
      push({
        kind: found.length > 0 ? "info" : "success",
        title: found.length > 0 ? `${found.length} update${found.length === 1 ? "" : "s"} available` : `All mods up to date (${checked} checked)`,
      });
    } catch (e) {
      push({ kind: "error", title: "Update check failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setChecking(false);
    }
  };

  const updateTyxenNow = async () => {
    if (!inst || !tyxenUpdate || updatingTyxen) return;
    setUpdatingTyxen(true);
    try {
      const { updateTyxen } = await import("@/lib/tyxmod");
      const filename = await updateTyxen(inst.id, tyxenUpdate);
      setTyxenUpdate(null);
      setFilesTick((t) => t + 1);
      push({ kind: "success", title: `Tyxen updated to ${tyxenUpdate.version}`, body: filename });
    } catch (e) {
      push({ kind: "error", title: "Tyxen update failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setUpdatingTyxen(false);
    }
  };

  const updateAll = async () => {
    if (!inst || !updates?.length || updatingAll) return;
    setUpdatingAll(true);
    try {
      const { updateAllMods } = await import("@/lib/updates");
      const { ok, failed } = await updateAllMods(inst.id, updates, {
        gameVersion: inst.version,
        loader: inst.loader,
        onEach: (u, idx, total) =>
          push({ kind: "info", title: `Updating ${idx + 1}/${total}: ${u.title}` }),
      });
      if (ok > 0) {
        push({ kind: "success", title: `Updated ${ok} mod${ok === 1 ? "" : "s"}` });
        // Keep exactly the entries that FAILED (successes may have renamed
        // files, so positional slice() would drop the wrong rows).
        const failedFiles = new Set(failed.map((f) => f.file));
        setUpdates((u) => (u ? u.filter((x) => failedFiles.has(x.file)) : u));
        setFilesTick((t) => t + 1);
      }
      for (const f of failed) {
        push({ kind: "error", title: `Failed: ${f.file}`, body: f.error });
      }
      if (failed.length === 0) setUpdates([]);
    } finally {
      setUpdatingAll(false);
    }
  };

  const onSave = async () => {
    if (!draft) return;
    await saveSettings(inst.id, draft);
    push({ kind: "success", title: "Instance settings saved" });
  };

  const changeIcon = async () => {
    if (!inst || iconBusy) return;
    setIconBusy(true);
    try {
      const picked = await api.pickImageFile();
      if (!picked) return;
      await setIcon(inst.id, picked);
      push({ kind: "success", title: "Instance icon updated" });
    } catch (e) {
      push({ kind: "error", title: "Couldn't set icon", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setIconBusy(false);
    }
  };

  return (
    <AnimatePresence>
      <motion.div
        className="fixed inset-0 z-[60] bg-black/40 backdrop-blur-[2px]"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={() => select(undefined)}
      />
      <motion.aside
        className="drawer-float fixed bottom-3 right-3 top-14 z-[61] flex w-full max-w-[560px] flex-col rounded-xl border border-white/10 bg-surface-100/85 shadow-3 backdrop-blur-xl"
        initial={{ x: 420 }}
        animate={{ x: 0 }}
        exit={{ x: 420 }}
        transition={{ type: "spring", stiffness: 340, damping: 34 }}
      >
        <div className="hero-gradient-bar h-0.5 w-full opacity-70" aria-hidden />
        {/* Header */}
        <div className="flex items-center gap-3 border-b border-white/10 p-4">
          <button
            onClick={() => changeIcon()}
            title="Change icon (custom upload)"
            aria-label="Change instance icon"
            className="group/icon relative flex size-12 shrink-0 items-center justify-center rounded-xl border border-accent-500/30 bg-gradient-to-b from-accent-500/20 to-accent-500/5 shadow-glow transition hover:border-accent-500/60"
          >
            <InstanceIcon icon={inst.icon} instanceId={inst.id} className="size-7 text-accent-400" imgClassName="size-10 rounded-[10px] object-cover" />
            <span className="absolute -bottom-1 -right-1 flex size-5 items-center justify-center rounded-full border border-white/20 bg-surface-300 text-ink-muted opacity-0 transition group-hover/icon:opacity-100">
              <Pencil className="size-3" />
            </span>
          </button>
          <div className="min-w-0 flex-1">
            <h2 className="truncate text-base font-bold tracking-tight">{inst.name}</h2>
            <div className="mt-1 flex flex-wrap gap-1.5">
              <Badge tone="accent"><LoaderIcon loader={inst.loader} /> {inst.loader}</Badge>
              <Badge className="font-mono">{inst.version}</Badge>
            </div>
          </div>
          {runningIds.includes(inst.id) ? (
            <Button variant="danger" size="sm" onClick={onStop}>
              <Square className="fill-current" /> Stop
            </Button>
          ) : (
            <Button variant="primary" size="sm" onClick={onPlay} disabled={launching} className="shadow-glow">
              <Play /> {launching ? "Launching…" : "Play"}
            </Button>
          )}
          <button
            onClick={() => openFolder()}
            title={opening === "root" ? "Opening…" : `Open ${inst.name} folder in Explorer`}
            aria-label="Open instance folder"
            disabled={opening !== null}
            className="rounded-md p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink disabled:opacity-50"
          >
            <FolderOpen className={cn("size-4", opening === "root" && "animate-pulse")} />
          </button>
          <button onClick={() => select(undefined)} aria-label="Close" className="rounded-md p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink">
            <X className="size-4" />
          </button>
        </div>

        {/* Tabs — segmented pills with counts */}
        <div className="border-b border-white/10 px-3 py-2">
          <div className="flex gap-1 rounded-lg border border-white/10 bg-surface-50/60 p-1" role="tablist" aria-label="Instance sections">
            {TABS.map(({ id, label, icon: Icon }) => {
              const active = tab === id;
              const n = counts[id === "saves" ? "saves" : id];
              return (
                <button
                  key={id}
                  role="tab"
                  aria-selected={active}
                  onClick={() => setTab(id)}
                  className={cn(
                    "flex flex-1 items-center justify-center gap-1.5 rounded-md px-2 py-1.5 text-[13px] font-medium transition-all",
                    active ? "bg-surface-300 font-semibold text-ink shadow-1" : "text-ink-muted hover:text-ink",
                  )}
                >
                  <Icon className={cn("size-3.5", active && "text-accent-400")} /> {label}
                  {typeof n === "number" && (
                    <span className={cn("rounded-full px-1.5 py-px font-mono text-[10px]", active ? "bg-accent-500/20 text-accent-400" : "bg-surface-300/70 text-ink-faint")}>
                      {n}
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        {/* Body — installer stays mounted across refetches (no more
            reopen-every-install loop); only the list skeleton-swaps. */}
        <div className="flex-1 overflow-y-auto p-4">
          {tab === "settings" ? (
            draft && (
              <div className="space-y-4">
                <div>
                  <div className="flex justify-between">
                    <label className="label" htmlFor="ram">Memory (RAM)</label>
                    <span className="font-mono text-xs text-accent-400">{draft.ramMb} MB</span>
                  </div>
                  <input
                    id="ram"
                    type="range"
                    min={1024}
                    max={16384}
                    step={256}
                    value={draft.ramMb}
                    onChange={(e) => setDraft({ ...draft, ramMb: Number(e.target.value) })}
                    className="w-full accent-[#1ea86a]"
                  />
                  <div className="flex justify-between text-[11px] text-ink-faint"><span>1 GB</span><span>16 GB</span></div>
                </div>
                <div>
                  <label className="label" htmlFor="java">Java path override (blank = auto)</label>
                  <input id="java" className="input font-mono text-xs" placeholder="auto-detect" value={draft.javaPath} onChange={(e) => setDraft({ ...draft, javaPath: e.target.value })} />
                </div>
                <div>
                  <label className="label" htmlFor="jvm">Extra JVM args</label>
                  <input id="jvm" className="input font-mono text-xs" placeholder="-XX:+UseG1GC" value={draft.jvmArgs} onChange={(e) => setDraft({ ...draft, jvmArgs: e.target.value })} />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="label" htmlFor="rw">Width</label>
                    <input id="rw" type="number" className="input" value={draft.resW} onChange={(e) => setDraft({ ...draft, resW: Number(e.target.value) })} />
                  </div>
                  <div>
                    <label className="label" htmlFor="rh">Height</label>
                    <input id="rh" type="number" className="input" value={draft.resH} onChange={(e) => setDraft({ ...draft, resH: Number(e.target.value) })} />
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="primary" size="sm" className="flex-1" onClick={onSave}>Save settings</Button>
                  <Button variant="secondary" size="sm" onClick={() => openFolder()} disabled={opening !== null}>
                    <FolderOpen /> {opening ? "Opening…" : "Open folder"}
                  </Button>
                </div>
                <div className="border-t border-white/10 pt-3">
                  <div className="flex flex-wrap gap-2">
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={async () => {
                        try {
                          const { exportInstancePack } = await import("@/lib/backend");
                          const dest = await exportInstancePack(inst.id, inst.name);
                          if (dest) push({ kind: "success", title: "Exported .mrpack", body: dest });
                        } catch (e) {
                          push({ kind: "error", title: "Export failed", body: e instanceof Error ? e.message : String(e) });
                        }
                      }}
                    >
                      <Download /> Export .mrpack
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={async () => {
                        if (!confirm(`Delete “${inst.name}” forever?`)) return;
                        await remove(inst.id);
                        select(undefined);
                        push({ kind: "success", title: `Deleted “${inst.name}”` });
                      }}
                    >
                      <Trash2 /> Delete instance
                    </Button>
                  </div>
                  <p className="mt-1 font-mono text-[11px] text-ink-faint">{inst.path}</p>
                </div>
              </div>
            )
          ) : (
            <>
              {tab === "mods" && (
                <InstanceModInstall
                  instanceId={inst.id}
                  version={inst.version}
                  loader={inst.loader}
                  onInstalled={() => setFilesTick((t) => t + 1)}
                />
              )}
              {tab === "mods" && tyxenUpdate && (
                <div className="mb-3 flex flex-wrap items-center gap-2 rounded-lg border border-accent-500/40 bg-accent-500/10 px-3 py-2">
                  <span className="text-xs font-semibold">
                    Tyxen {tyxenUpdate.version} available
                    {tyxenUpdate.changelog ? ` — ${tyxenUpdate.changelog}` : ""}
                  </span>
                  <span className="ml-auto">
                    <Button variant="primary" size="sm" onClick={updateTyxenNow} disabled={updatingTyxen || checking}>
                      {updatingTyxen ? "Updating…" : `Update to ${tyxenUpdate.version}`}
                    </Button>
                  </span>
                </div>
              )}
              {tab === "mods" && (
                <div className="mb-3 flex flex-wrap items-center gap-2 rounded-lg border border-white/10 bg-surface-50/60 px-3 py-2">
                  <span className="text-xs font-semibold">
                    {updates === null
                      ? "Keep mods fresh"
                      : updates.length === 0
                        ? "All mods up to date"
                        : `${updates.length} update${updates.length === 1 ? "" : "s"} available`}
                  </span>
                  {updates !== null && updates.length > 0 && (
                    <span className="max-w-full truncate font-mono text-[11px] text-ink-faint">
                      {updates.slice(0, 3).map((u) => u.title).join(" · ")}
                      {updates.length > 3 ? ` +${updates.length - 3}` : ""}
                    </span>
                  )}
                  <span className="ml-auto flex gap-1.5">
                    <Button variant="secondary" size="sm" onClick={checkUpdates} disabled={checking || updatingAll}>
                      {checking ? "Checking…" : "Check for updates"}
                    </Button>
                    {updates !== null && updates.length > 0 && (
                      <Button variant="primary" size="sm" onClick={updateAll} disabled={updatingAll || checking}>
                        {updatingAll ? "Updating…" : `Update all (${updates.length})`}
                      </Button>
                    )}
                  </span>
                </div>
              )}
                <div className="mb-3 flex items-center justify-between gap-2">
                  <span className="font-mono text-[11px] text-ink-faint">
                    {(tab === "saves" ? "saves" : tab)} folder
                  </span>
                  {tab === "mods" && (
                    <input
                      value={modQuery}
                      onChange={(e) => setModQuery(e.target.value)}
                      placeholder="Search installed mods..."
                      spellCheck={false}
                      className="w-44 rounded-md border border-white/10 bg-black/30 px-2 py-1 font-mono text-[11px] text-ink outline-none placeholder:text-ink-faint focus:border-accent-500/60"
                    />
                  )}
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => openFolder(tab === "saves" ? "saves" : tab)}
                  disabled={opening !== null}
                  title={`Open ${tab} folder in Explorer`}
                >
                  <FolderOpen /> {opening ? "Opening…" : "Open folder"}
                </Button>
              </div>
              {loading ? (
                <div className="space-y-2">
                  {[0, 1, 2].map((i) => (
                    <Skeleton key={i} className="h-12 w-full" />
                  ))}
                </div>
              ) : files.length === 0 ? (
                <EmptyState
                  icon={tab === "saves" ? WorldIcon : tab === "screenshots" ? ShotsIcon : tab === "logs" ? LogsIcon : ModIcon}
                  title={
                    tab === "saves"
                      ? "No worlds yet"
                      : tab === "screenshots"
                        ? "No screenshots yet"
                        : tab === "logs"
                          ? "No logs yet"
                          : "No mods yet"
                  }
                  hint={
                    tab === "saves"
                      ? "Create a single-player world in game, or import a .mrpack with saves — worlds appear here automatically."
                      : tab === "mods"
                        ? "Use the installer above — mods land here."
                        : tab === "screenshots"
                          ? "Press F2 in game — screenshots appear here automatically."
                          : "Launch the game once — log files appear here automatically."
                  }
                />
              ) : tab === "screenshots" ? (
                <ShotsGallery
                  instanceId={inst.id}
                  files={files}
                  urls={shotUrls}
                  onUrls={(m) => setShotUrls((p) => ({ ...p, ...m }))}
                  lightbox={lightbox}
                  onLightbox={setLightbox}
                  onDelete={onDeleteFile}
                  onReveal={async (name) => {
                    try {
                      await api.revealInstanceFile(inst.id, "screenshots", name);
                    } catch (e) {
                      push({ kind: "error", title: "Couldn't reveal file", body: e instanceof Error ? e.message : String(e) });
                    }
                  }}
                />
              ) : (
                  <div className="space-y-1.5">
                    {files
                      .filter(
                        (f) =>
                          tab !== "mods" ||
                          modQuery.trim() === "" ||
                          f.name.toLowerCase().includes(modQuery.trim().toLowerCase()),
                      )
                      .map((f, i) => (
                    tab === "mods" && !f.isDir ? (
                      <ModRow
                        key={f.name}
                        file={f}
                        index={i}
                        instanceId={inst.id}
                        mcVersion={inst.version}
                        loader={inst.loader}
                        sweep={updates?.find((x) => x.file === f.name) ?? null}
                        onUpdated={() => setFilesTick((t) => t + 1)}
                        onDelete={() => onDeleteFile(f.name)}
                        onToggle={async () => {
                          try {
                            await api.toggleInstanceFile(inst.id, "mods", f.name);
                            setFilesTick((t) => t + 1);
                          } catch (e) {
                            push({ kind: "error", title: "Could not toggle mod", body: e instanceof Error ? e.message : String(e) });
                          }
                        }}
                      />
                    ) : (
                      <div key={f.name} className="flex items-center gap-3 rounded-lg border border-white/10 bg-surface-50/60 px-3 py-2">
                        <span className="flex size-9 shrink-0 items-center justify-center rounded-md bg-accent-500/12">
                          {f.isDir ? (
                            tab === "saves" ? (
                              <WorldIcon className="size-5 text-accent-400" />
                            ) : (
                              <FolderIcon className="size-5 text-accent-400" />
                            )
                          ) : tab === "logs" ? (
                            <LogsIcon className="size-5 text-accent-400" />
                          ) : (
                            <ModIcon className="size-5 text-accent-400" />
                          )}
                        </span>
                        <span className="min-w-0 flex-1">
                          <span className="block truncate text-[13px] font-semibold">{f.name}</span>
                          <span className="block font-mono text-[11px] text-ink-faint">
                            {f.isDir ? "Folder" : formatBytes(f.size)}
                          </span>
                        </span>
                        {f.isDir && <Badge>folder</Badge>}
                        <button onClick={() => onDeleteFile(f.name)} aria-label={`Delete ${f.name}`} className="rounded p-1 text-ink-faint transition hover:bg-red-500/10 hover:text-red-400">
                          <Trash2 className="size-3.5" />
                        </button>
                      </div>
                    )
                  ))}
                </div>
                )}
            </>
          )}
        </div>
      </motion.aside>
    </AnimatePresence>
  );
}

/** Pretty display name: strip extension + version tail (`sodium-… .jar` → `sodium`). */
function prettyModName(filename: string): string {
  return filename.replace(/\.(jar|zip)(\.disabled|\.off)?$/i, "").slice(0, 48);
}

const SHOT_RE = /\.(png|jpe?g|webp)$/i;

/**
 * Screenshots gallery — thumbnail grid (lazy data-URL loads, cached by
 * filename) + click-to-open lightbox with prev/next, reveal-in-Explorer
 * and delete. Non-image files fall back to plain rows below the grid.
 */
function ShotsGallery({
  instanceId,
  files,
  urls,
  onUrls,
  lightbox,
  onLightbox,
  onDelete,
  onReveal,
}: {
  instanceId: string;
  files: api.FileEntry[];
  urls: Record<string, string>;
  onUrls: (m: Record<string, string>) => void;
  lightbox: number | null;
  onLightbox: (i: number | null) => void;
  onDelete: (name: string) => void;
  onReveal: (name: string) => void;
}) {
  const shots = files
    .filter((f) => !f.isDir && SHOT_RE.test(f.name))
    .sort((a, b) => b.modified - a.modified);
  const others = files.filter((f) => f.isDir || !SHOT_RE.test(f.name));

  // Lazy-load missing thumbnails (concurrency-capped, never throws).
  useEffect(() => {
    let live = true;
    const missing = shots.filter((f) => !urls[`${instanceId}::${f.name}`]).slice(0, 24);
    if (missing.length === 0) return;
    (async () => {
      const got: Record<string, string> = {};
      let i = 0;
      async function worker() {
        while (i < missing.length) {
          const f = missing[i++];
          try {
            const { instanceFileData } = await import("@/lib/backend");
            got[`${instanceId}::${f.name}`] = await instanceFileData(instanceId, "screenshots", f.name);
          } catch {
            /* a failed thumb just keeps its placeholder */
          }
        }
      }
      await Promise.all(Array.from({ length: Math.min(4, missing.length) }, () => worker()));
      if (live && Object.keys(got).length > 0) onUrls(got);
    })().catch(() => {});
    return () => {
      live = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [instanceId, files.map((f) => f.name).join(",")]);

  const openIdx = lightbox != null && lightbox >= 0 && lightbox < shots.length ? lightbox : null;
  return (
    <>
      {shots.length > 0 && (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
          {shots.map((f, i) => {
            const src = urls[`${instanceId}::${f.name}`];
            return (
              <button
                key={f.name}
                onClick={() => onLightbox(i)}
                title={`${f.name} — click to view`}
                className="group relative aspect-video overflow-hidden rounded-lg border border-white/10 bg-surface-200/50 transition hover:border-accent-500/50"
              >
                {src ? (
                  <img src={src} alt={f.name} loading="lazy" className="size-full object-cover" />
                ) : (
                  <span className="flex size-full items-center justify-center">
                    <ShotsIcon className="size-6 text-ink-faint" />
                  </span>
                )}
                <span className="absolute inset-x-0 bottom-0 truncate bg-black/55 px-2 py-1 text-left font-mono text-[10px] text-ink opacity-0 backdrop-blur-sm transition group-hover:opacity-100">
                  {f.name}
                </span>
              </button>
            );
          })}
        </div>
      )}
      {others.length > 0 && (
        <div className={shots.length > 0 ? "mt-2 space-y-1.5" : "space-y-1.5"}>
          {others.map((f) => (
            <div key={f.name} className="flex items-center gap-3 rounded-lg border border-white/10 bg-surface-50/60 px-3 py-2">
              <span className="flex size-9 shrink-0 items-center justify-center rounded-md bg-accent-500/12">
                {f.isDir ? <FolderIcon className="size-5 text-accent-400" /> : <LogsIcon className="size-5 text-accent-400" />}
              </span>
              <span className="min-w-0 flex-1">
                <span className="block truncate text-[13px] font-semibold">{f.name}</span>
                <span className="block font-mono text-[11px] text-ink-faint">
                  {f.isDir ? "Folder" : formatBytes(f.size)}
                </span>
              </span>
              {f.isDir && <Badge>folder</Badge>}
              {!f.isDir && (
                <button onClick={() => onDelete(f.name)} aria-label={`Delete ${f.name}`} className="rounded p-1 text-ink-faint transition hover:bg-red-500/10 hover:text-red-400">
                  <Trash2 className="size-3.5" />
                </button>
              )}
            </div>
          ))}
        </div>
      )}
      {openIdx != null && (
        <ShotLightbox
          shots={shots}
          index={openIdx}
          src={urls[`${instanceId}::${shots[openIdx].name}`]}
          onIndex={onLightbox}
          onClose={() => onLightbox(null)}
          onDelete={(name) => {
            onDelete(name);
            const rest = shots.filter((s) => s.name !== name);
            onLightbox(rest.length === 0 ? null : Math.min(openIdx, rest.length - 1));
          }}
          onReveal={onReveal}
        />
      )}
    </>
  );
}

function ShotLightbox({
  shots,
  index,
  src,
  onIndex,
  onClose,
  onDelete,
  onReveal,
}: {
  shots: api.FileEntry[];
  index: number;
  src?: string;
  onIndex: (i: number) => void;
  onClose: () => void;
  onDelete: (name: string) => void;
  onReveal: (name: string) => void;
}) {
  const f = shots[index];
  useEffect(() => {
    const h = (e: KeyboardEvent) => {
      if (e.key === "ArrowRight") onIndex((index + 1) % shots.length);
      else if (e.key === "ArrowLeft") onIndex((index - 1 + shots.length) % shots.length);
    };
    window.addEventListener("keydown", h);
    return () => window.removeEventListener("keydown", h);
  }, [index, shots.length, onIndex]);
  if (!f) return null;
  const when = Number.isFinite(f.modified) && f.modified > 0 ? new Date(f.modified).toLocaleString() : "";
  return (
    <Modal open onClose={onClose} wide>
      <div className="mb-3 flex items-center gap-2">
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-bold">{f.name}</p>
          <p className="font-mono text-[11px] text-ink-faint">
            {formatBytes(f.size)}{when ? ` · ${when}` : ""}{shots.length > 1 ? ` · ${index + 1}/${shots.length}` : ""}
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={() => onReveal(f.name)}>
          <ExternalLink className="size-3.5" /> Show file
        </Button>
        <Button
          variant="secondary"
          size="sm"
          onClick={() => {
            if (confirm(`Delete “${f.name}” forever?`)) onDelete(f.name);
          }}
        >
          <Trash2 className="size-3.5" /> Delete
        </Button>
        <button onClick={onClose} aria-label="Close viewer" className="rounded-md border border-white/10 p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink">
          <X className="size-4" />
        </button>
      </div>
      <div className="relative flex min-h-[40vh] items-center justify-center overflow-hidden rounded-xl border border-white/10 bg-black/40">
        {src ? (
          <img src={src} alt={f.name} className="max-h-[62vh] w-full object-contain" />
        ) : (
          <span className="flex items-center gap-2 p-10 text-sm text-ink-faint">
            <ShotsIcon className="size-5" /> Couldn't load preview — use “Show file”.
          </span>
        )}
        {shots.length > 1 && (
          <>
            <button
              onClick={() => onIndex((index - 1 + shots.length) % shots.length)}
              aria-label="Previous screenshot"
              className="absolute left-2 rounded-full border border-white/10 bg-black/50 p-2 text-ink transition hover:bg-black/80"
            >
              <ChevronLeft className="size-5" />
            </button>
            <button
              onClick={() => onIndex((index + 1) % shots.length)}
              aria-label="Next screenshot"
              className="absolute right-2 rounded-full border border-white/10 bg-black/50 p-2 text-ink transition hover:bg-black/80"
            >
              <ChevronRight className="size-5" />
            </button>
          </>
        )}
      </div>
    </Modal>
  );
}

/**
 * Modrinth-style mod row: resolved logo (cached, strict-matched) or a
 * gradient fallback tile, title, size + source, update pill when the index
 * has a newer compatible release, enable toggle + delete. Icon + update
 * lookups are capped (first 30 rows) so big modpacks don't spam the API.
 */
function ModRow({
  file,
  index,
  instanceId,
  mcVersion,
  loader,
  sweep,
  onUpdated,
  onDelete,
  onToggle,
}: {
  file: api.FileEntry;
  index: number;
  instanceId: string;
  mcVersion: string;
  loader: string;
  /** Global sweep hit for this file (if the user ran Check) — so rows past
   * the per-row API cap still show accurate pills from one code path. */
  sweep?: import("@/lib/updates").ModUpdate | null;
  onUpdated: () => void;
  onDelete: () => void;
  onToggle: () => void;
}) {
  const [id, setId] = useState<ModIdentity | null>(null);
  const [latest, setLatest] = useState<{ version: string; published: number; ref: import("@/lib/modrinth").ProjectVersion } | null>(null);
  const [updating, setUpdating] = useState(false);
  const push = useToasts((s) => s.push);

  useEffect(() => {
    if (index >= 30) return;
    let live = true;
    resolveModIdentity(file.name).then((r) => {
      if (live) setId(r);
    });
    return () => {
      live = false;
    };
  }, [file.name, index]);

  // Latest compatible release for THIS instance (version + loader filtered).
  useEffect(() => {
    if (!id?.projectId || index >= 30) return;
    let live = true;
    (async () => {
      const { latestCompatibleVersion } = await import("@/lib/modrinth");
      const v = await latestCompatibleVersion(id.projectId, mcVersion, loader);
      if (!live || !v) return;
      setLatest({ version: v.version_number, published: Date.parse(v.date_published), ref: v });
    })().catch(() => {});
    return () => {
      live = false;
    };
  }, [id?.projectId, mcVersion, loader, index]);

  const disabled = /\.disabled$|\.off$/i.test(file.name);
  // Pinned version (exact) beats heuristics — falls back to timestamp+name
  // for mods installed before meta tracking existed. A global sweep hit
  // wins over the per-row check (same source of truth everywhere).
  const pinned = getModMeta(instanceId, file.name)?.versionNumber ?? "";
  const ownHit =
    !!latest &&
    (pinned
      ? pinned !== latest.version
      : Number.isFinite(latest.published) &&
        latest.published > file.modified + 60_000 &&
        !file.name.includes(latest.version));
  const hasUpdate = !!sweep || ownHit;
  const updateRef = sweep?.versionRef ?? latest?.ref ?? null;
  const updateLabel = sweep?.latest ?? latest?.version ?? "";

  const doUpdate = async () => {
    if (!updateRef || updating) return;
    if (disabled) {
      push({ kind: "info", title: "Re-enabling to update", body: `${file.name} is disabled — updating installs the enabled version.` });
    }
    setUpdating(true);
    try {
      const { installModWithDeps, deleteInstanceFile } = await import("@/lib/backend");
      const { markInstalledFor } = await import("@/lib/modrinth");
      const { setModMeta, clearModMeta } = await import("@/lib/mod-meta");
      const l = loader.toLowerCase();
      const paths = await installModWithDeps(instanceId, "mods", updateRef, {
        gameVersion: mcVersion,
        loader: ["fabric", "forge", "quilt", "neoforge"].includes(l) ? l : undefined,
      });
      const main = paths[0]?.split(/[\\/]/).pop() ?? updateRef.files[0]?.filename ?? file.name;
      markInstalledFor(instanceId, id?.projectId ?? sweep?.projectId ?? "", main);
      const pid = id?.projectId ?? sweep?.projectId ?? "";
      if (pid) setModMeta(instanceId, pid, updateRef, main);
      if (main.toLowerCase() !== file.name.toLowerCase()) {
        try {
          await deleteInstanceFile(instanceId, "mods", file.name);
          clearModMeta(instanceId, file.name);
        } catch {
          /* stale cleanup best-effort */
        }
      }
      push({ kind: "success", title: `Updated to ${updateLabel}`, body: file.name });
      onUpdated();
    } catch (e) {
      push({ kind: "error", title: "Update failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className={cn("group flex items-center gap-3 rounded-lg border border-white/10 bg-surface-50/60 px-3 py-2 transition-colors hover:border-accent-500/30", disabled && "opacity-60")}>
      {id?.icon_url ? (
        <img src={id.icon_url} alt="" className="size-10 shrink-0 rounded-lg border border-white/10 object-cover" loading="lazy" />
      ) : (
        <div className="flex size-10 shrink-0 items-center justify-center rounded-lg border border-accent-500/25 bg-gradient-to-b from-accent-500/20 to-accent-500/5">
          <ModIcon className="size-5 text-accent-400" />
        </div>
      )}
      <div className="min-w-0 flex-1">
        <p className="flex items-center gap-1.5 truncate text-[13px] font-semibold">
          <span className="truncate">{id?.title ?? prettyModName(file.name)}</span>
          {disabled && <Badge tone="warn" className="shrink-0">off</Badge>}
          {hasUpdate && <Badge tone="accent" className="shrink-0">Update</Badge>}
        </p>
        <p className="truncate font-mono text-[11px] text-ink-faint">
          {file.name} · {formatBytes(file.size)}
          {hasUpdate && updateLabel && <span> · Latest {updateLabel}</span>}
        </p>
      </div>
      {hasUpdate && (
        <button
          onClick={doUpdate}
          disabled={updating}
          title={updateLabel ? `Update to ${updateLabel}` : "Update"}
          className="shrink-0 rounded-md bg-gradient-to-b from-accent-400 to-accent-500 px-2.5 py-1 text-[11px] font-bold text-white shadow-2 transition hover:shadow-glow hover:brightness-110 active:scale-95 disabled:opacity-50"
        >
          {updating ? "Updating" : "Update"}
        </button>
      )}
      <button
        onClick={onToggle}
        title={disabled ? "Enable mod" : "Disable mod"}
        aria-label={disabled ? `Enable ${file.name}` : `Disable ${file.name}`}
        aria-pressed={!disabled}
        className="rounded-md border border-white/10 px-2 py-1 text-[11px] font-semibold text-ink-muted transition hover:border-accent-500/40 hover:text-ink"
      >
        {disabled ? "Enable" : "Disable"}
      </button>
      <button
        onClick={async () => {
          try {
            const { revealInstanceFile } = await import("@/lib/backend");
            await revealInstanceFile(instanceId, "mods", file.name);
          } catch (e) {
            push({ kind: "error", title: "Couldn't reveal file", body: e instanceof Error ? e.message : String(e) });
          }
        }}
        title="Show in Explorer"
        aria-label={`Show ${file.name} in Explorer`}
        className="rounded p-1 text-ink-faint transition hover:bg-surface-200 hover:text-ink"
      >
        <FolderOpen className="size-3.5" />
      </button>
      <button onClick={onDelete} aria-label={`Delete ${file.name}`} className="rounded p-1 text-ink-faint transition hover:bg-red-500/10 hover:text-red-400">
        <Trash2 className="size-3.5" />
      </button>
    </div>
  );
}
