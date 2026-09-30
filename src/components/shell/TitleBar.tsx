import { useEffect, useState } from "react";
import { Minus, Square, X, Copy } from "lucide-react";
import { TyxLogo } from "@/components/icons/brand";
import { windowClose, windowMinimize, windowToggleMaximize, isTauri } from "@/lib/tauri";
import { cn } from "@/lib/utils";

/**
 * Custom borderless title bar.
 *
 * Drag semantics (Tauri `drag.js`): a BARE `data-tauri-drag-region` only
 * drags on direct clicks of that exact element — clicks on children (title
 * text!) do nothing. `="deep"` makes the whole subtree draggable, while
 * `="false"` on the controls keeps button clicks working. Double-click to
 * maximize is handled natively by the same script — do NOT add a React
 * onDoubleClick (it would toggle twice = net zero).
 */
export function TitleBar() {
  const [maximized, setMaximized] = useState(false);

  useEffect(() => {
    if (!isTauri) return;
    let cancelled = false;
    let unlisten: (() => void) | undefined;
    import("@tauri-apps/api/window").then(({ getCurrentWindow }) => {
      if (cancelled) return;
      const win = getCurrentWindow();
      win.isMaximized().then(setMaximized).catch(() => {});
      win
        .onResized(() => {
          win.isMaximized().then(setMaximized).catch(() => {});
        })
        .then((u) => {
          unlisten = u;
        })
        .catch(() => {});
    }).catch(() => {});
    return () => {
      cancelled = true;
      unlisten?.();
    };
  }, []);

  const toggle = async () => {
    await windowToggleMaximize();
    setMaximized((m) => !m);
  };

  const btn =
    "flex h-8 w-11 items-center justify-center text-ink-muted transition-colors hover:bg-surface-200/80 hover:text-ink";

  return (
    <header
      data-tauri-drag-region="deep"
      className="flex h-11 shrink-0 select-none items-center gap-2 overflow-hidden border-b border-white/10 bg-surface-100/50 pl-4 pr-1 backdrop-blur-xl backdrop-saturate-150"
    >
      {/* App mark + name */}
      <div className="flex items-center gap-2.5">
        <TyxLogo className="size-5 rounded-[5px]" />
        <span className="text-[13px] font-semibold tracking-tight">Tyxen Launcher</span>
        {!isTauri && (
          <span className="rounded-full border px-2 py-px text-[10px] font-semibold uppercase tracking-wider text-ink-faint">
            browser preview
          </span>
        )}
      </div>

      <div className="flex-1" />

      {/* Window controls — explicitly outside the drag region */}
      <div
        className="flex items-center"
        data-tauri-drag-region="false"
        onPointerDown={(e) => e.stopPropagation()}
        onDoubleClick={(e) => e.stopPropagation()}
      >
        <button className={cn(btn, "rounded-md")} onClick={windowMinimize} aria-label="Minimize">
          <Minus className="size-4" />
        </button>
        <button className={cn(btn, "rounded-md")} onClick={toggle} aria-label="Maximize / restore">
          {maximized ? <Copy className="size-3.5" /> : <Square className="size-3.5" />}
        </button>
        <button
          className={cn(btn, "rounded-md hover:!bg-red-500 hover:!text-white")}
          onClick={windowClose}
          aria-label="Close"
        >
          <X className="size-4" />
        </button>
      </div>
    </header>
  );
}
