/** Shared local-pack import: progress bar → install → instant list update → open drawer. */
import { importModpackFile, onAssetsProgress } from "./backend";
import { useInstances } from "@/stores/instances";
import { useToasts } from "@/stores/app";
import { useModpackProgress } from "@/stores/modpackProgress";

export async function importPackFromPath(path: string): Promise<void> {
  const push = useToasts.getState().push;
  const progress = useModpackProgress.getState();
  const name = path.split(/[\\/]/).pop() ?? "pack.mrpack";
  progress.start(name);
  push({ kind: "info", title: `Importing ${name}`, body: "Creating an isolated instance. Watch the progress bar above." });
  // Live % from the Rust backend (tyx://assets). Browser preview has no
  // events — the bar simply stays at 0% until the instant local finish.
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
    const inst = await importModpackFile(path);
    // Optimistic insert FIRST so the grid shows instantly — then refresh
    // from disk to reconcile. The user never needs a manual refresh.
    const st = useInstances.getState();
    const exists = st.instances.some((i) => i.id === inst.id);
    if (!exists) {
      useInstances.setState((s) => ({ instances: [inst, ...s.instances] }));
    }
    await st.refresh().catch(() => {});
    useInstances.getState().select(inst.id);
    useModpackProgress.getState().finish();
    push({
      kind: "success",
      title: `Imported ${inst.name}`,
      body: `${inst.version} · ${inst.loader} — ready to play. Open Worlds to see your saves.`,
    });
  } catch (e) {
    useModpackProgress.getState().finish();
    push({ kind: "error", title: "Modpack import failed", body: e instanceof Error ? e.message : String(e) });
  } finally {
    try {
      unlisten();
    } catch {
      /* ignore */
    }
  }
}
