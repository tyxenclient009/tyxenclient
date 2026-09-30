import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { TitleBar } from "./TitleBar";
import { Sidebar } from "./Sidebar";
import { Toasts } from "@/components/ui/Toasts";
import { CommandPalette } from "./CommandPalette";
import { ResizeHandles } from "./ResizeHandles";
import { InstanceDetail } from "@/components/dialogs/InstanceDetail";
import { ModpackProgressBanner } from "@/components/modpack/ModpackProgress";
import { ModpackIcon } from "@/components/icons/brand";
import { PageErrorBoundary } from "@/components/ui/ErrorBoundary";
import { useNav, useToasts } from "@/stores/app";
import { useInstances } from "@/stores/instances";
import { initLaunchListener } from "@/stores/launch";
import { isTauri } from "@/lib/tauri";
import { importPackFromPath } from "@/lib/import-pack";
import { cn } from "@/lib/utils";
import { HomePage } from "@/pages/Home";
import { InstancesPage } from "@/pages/Instances";
import { BrowsePage } from "@/pages/Browse";
import { SkinsPage } from "@/pages/Skins";
import { AnnouncementsPage } from "@/pages/Announcements";
import { SettingsPage } from "@/pages/Settings";

/** App shell: title bar + sidebar + page slot + global overlays. */
export function AppShell() {
  const route = useNav((s) => s.route);
  const refresh = useInstances((s) => s.refresh);
  // Maximized/fullscreen windows must be SQUARE: rounded panel corners leave
  // gaps where the OS rect frame (accent/white line) peeks through.
  const [maximized, setMaximized] = useState(false);
  // Keep-alive tabs: every visited page stays mounted (hidden when inactive)
  // so switching tabs never remounts, never replays FadeUp entrances, never
  // reflashes skeletons and never loses scroll or form state. Pages mount
  // lazily on first visit — boot still only mounts Home.
  const [visited, setVisited] = useState<string[]>(["home"]);
  useEffect(() => {
    setVisited((v) => (v.includes(route) ? v : [...v, route]));
  }, [route]);
  // .mrpack drag-drop overlay (desktop only).
  const [dropping, setDropping] = useState(false);
  const dragDepth = useRef(0);

  useEffect(() => {
    initLaunchListener().catch(() => {});
    refresh().catch(() => {});
    if (!isTauri) return;
    let unlisten: (() => void) | undefined;
    let cancelled = false;
    import("@tauri-apps/api/window")
      .then(({ getCurrentWindow }) => {
        if (cancelled) return;
        const win = getCurrentWindow();
        win.isMaximized().then((m) => {
          if (!cancelled) setMaximized(m);
        }).catch(() => {});
        win.onResized(() => {
          win.isMaximized().then((m) => {
            if (!cancelled) setMaximized(m);
          }).catch(() => {});
        }).then((u) => {
          unlisten = u;
        }).catch(() => {});
      })
      .catch(() => {});
    return () => {
      cancelled = true;
      unlisten?.();
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // .mrpack drag-drop anywhere onto the window → import as new instance.
  useEffect(() => {
    if (!isTauri) return;
    const unlisteners: (() => void)[] = [];
    let cancelled = false;
    import("@tauri-apps/api/event")
      .then(({ listen }) => {
        if (cancelled) return;
        listen("tauri://drag-enter", () => {
          dragDepth.current += 1;
          setDropping(true);
        }).then((u) => unlisteners.push(u)).catch(() => {});
        listen("tauri://drag-leave", () => {
          dragDepth.current = Math.max(0, dragDepth.current - 1);
          if (dragDepth.current === 0) setDropping(false);
        }).then((u) => unlisteners.push(u)).catch(() => {});
        listen<{ paths?: string[] }>("tauri://drag-drop", (e) => {
          dragDepth.current = 0;
          setDropping(false);
          const paths = e.payload?.paths ?? [];
          const packs = paths.filter((p) => p.toLowerCase().endsWith(".mrpack"));
          if (paths.length > 0 && packs.length === 0) {
            useToasts.getState().push({ kind: "info", title: "Only .mrpack files can be dropped", body: "Drag a modpack file to install it." });
            return;
          }
          packs.forEach((p) => {
            importPackFromPath(p).catch(() => {});
          });
        }).then((u) => unlisteners.push(u)).catch(() => {});
      })
      .catch(() => {});
    return () => {
      cancelled = true;
      unlisteners.forEach((u) => u());
    };
  }, []);

  return (
      <div
      data-maximized={maximized ? "true" : "false"}
      // NOTE: no `shadow-*` here — box-shadow on a rounded container over a
      // transparent page rasterizes a white fringe along the curve
      // (Chromium). DWM draws the real window shadow (shadow:true).
      className={cn(
        "relative flex h-full flex-col overflow-hidden border border-transparent bg-surface-50",
        maximized ? "rounded-none" : "rounded-xl",
      )}
    >
      <ResizeHandles />
      <TitleBar />
      <ModpackProgressBanner />
      <div className={cn("flex min-h-0 flex-1", maximized ? "gap-0 p-0" : "gap-3 p-3")}>
        <Sidebar />
        <main className="panel min-w-0 flex-1 overflow-y-auto p-6 pb-10">
          <PageErrorBoundary resetKey={route}>
            {/* No route animation and no key={route} wrapper: the old opacity
                fade + per-page FadeUp replay + a full remount stacked into a
                double-blink on every tab click. Visited pages stay mounted
                (hidden when inactive) so tabs switch instantly with zero
                replay. resetKey only clears a crashed page on navigation —
                it does not remount healthy pages. */}
            {visited.includes("home") && (
              <div hidden={route !== "home"}>
                <HomePage />
              </div>
            )}
            {visited.includes("instances") && (
              <div hidden={route !== "instances"}>
                <InstancesPage />
              </div>
            )}
            {visited.includes("browse") && (
              <div hidden={route !== "browse"}>
                <BrowsePage />
              </div>
            )}
            {visited.includes("skins") && (
              <div hidden={route !== "skins"}>
                <SkinsPage />
              </div>
            )}
            {visited.includes("announcements") && (
              <div hidden={route !== "announcements"}>
                <AnnouncementsPage />
              </div>
            )}
            {visited.includes("settings") && (
              <div hidden={route !== "settings"}>
                <SettingsPage />
              </div>
            )}
          </PageErrorBoundary>
        </main>
      </div>

      {/* Global overlays (game logs live in their own OS window now) */}
      <InstanceDetail />
      <CommandPalette />
      <Toasts />

      {/* .mrpack drop overlay */}
      <AnimatePresence>
        {dropping && (
          <motion.div
            className="pointer-events-none absolute inset-0 z-[120] flex items-center justify-center rounded-xl border-2 border-dashed border-accent-400/70 bg-black/50 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <div className="panel-raised flex flex-col items-center px-8 py-6 text-center">
              <span className="flex size-12 items-center justify-center rounded-xl bg-accent-500/15">
                <ModpackIcon className="size-7 text-accent-400" />
              </span>
              <p className="mt-2 text-base font-bold">Drop .mrpack file here to install</p>
              <p className="mt-0.5 text-[13px] text-ink-muted">A new isolated instance is created automatically with live progress.</p>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
