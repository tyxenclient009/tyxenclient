import { create } from "zustand";
import { onGameLog } from "@/lib/backend";
import { extractServer, useServers } from "@/stores/servers";

export interface LogLine {
  stream: "sys" | "stdout" | "stderr";
  line: string;
  at: number;
  /** Owning instance when the backend tagged the line (multi-launch). */
  instanceId?: string;
}

interface LaunchState {
  /** True while ANY game runs (global Stop-all visibility). */
  running: boolean;
  /** Instance id of the running game (legacy single-view; newest first). */
  runningInstanceId: string | null;
  /** Every running instance id (Modrinth-style multi-launch). */
  runningIds: string[];
  open: boolean;
  lines: LogLine[];
  setRunning: (r: boolean) => void;
  setRunningInstance: (id: string | null) => void;
  markRunning: (id: string) => void;
  markStopped: (id: string) => void;
  isRunning: (id: string) => boolean;
  setOpen: (o: boolean) => void;
  push: (l: Omit<LogLine, "at">) => void;
  clear: () => void;
}

export const useLaunch = create<LaunchState>((set, get) => ({
  running: false,
  runningInstanceId: null,
  runningIds: [],
  open: false,
  lines: [],
  setRunning: (running) => set({ running }),
  setRunningInstance: (runningInstanceId) => set({ runningInstanceId }),
  markRunning: (id) =>
    set((s) => ({
      runningIds: s.runningIds.includes(id) ? s.runningIds : [...s.runningIds, id],
      running: true,
      runningInstanceId: id,
    })),
  markStopped: (id) =>
    set((s) => {
      const runningIds = s.runningIds.filter((x) => x !== id);
      return {
        runningIds,
        running: runningIds.length > 0,
        runningInstanceId: runningIds.length > 0 ? runningIds[runningIds.length - 1] : null,
      };
    }),
  isRunning: (id) => get().runningIds.includes(id),
  setOpen: (open) => set({ open }),
  push: (l) => set((s) => ({ lines: [...s.lines.slice(-800), { ...l, at: Date.now() }] })),
  clear: () => set({ lines: [] }),
}));

let subscribed = false;
let unlistenFn: (() => void) | null = null;

/** Subscribe once (AppShell) — funnels backend events into the store. */
export async function initLaunchListener() {
  if (subscribed) return () => unlistenFn?.();
  subscribed = true;
  const unlisten = await onGameLog(({ stream, line, instanceId }) => {
    const s = useLaunch.getState();
    s.push({ stream: stream as LogLine["stream"], line, instanceId });
    // Tagged payloads drive per-instance state directly (multi-launch).
    // Untagged lines (browser sim) fall back to the pid-regex path.
    const started = /\[tyx\] pid \d+ instance (\S+)/.exec(line);
    const id = instanceId || (started ? started[1] : undefined);
    if (started && id) {
      s.markRunning(id);
      return;
    }
    if (/process exited|spawn failed|cannot run Java|launch-aborted|stopped by user/i.test(line)) {
      if (id) s.markStopped(id);
      else if (s.runningIds.length <= 1) {
        // Only clear global state when we can't attribute the exit.
        // With 2+ games running an untagged line must not kill the indicator.
        const last = s.runningIds[s.runningIds.length - 1];
        if (last) s.markStopped(last);
        else {
          s.setRunning(false);
          s.setRunningInstance(null);
        }
      }
    }
    // Auto-capture joined servers for Home → Servers (instant rejoin).
    const srv = extractServer(line);
    if (srv) {
      try {
        useServers.getState().recordRecent(srv.host, srv.port);
    } catch {
      /* never break logging */
      }
    }
  });
  unlistenFn = unlisten;
  return unlisten;
}

/** For HMR/tests: detach the game-log listener so re-subscribes work. */
export function disposeLaunchListener() {
  try {
    unlistenFn?.();
  } catch {
    /* ignore */
  }
  unlistenFn = null;
  subscribed = false;
}
