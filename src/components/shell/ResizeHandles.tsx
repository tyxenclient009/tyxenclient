import { useEffect, useState } from "react";
import { isTauri } from "@/lib/tauri";

/**
 * Custom resize handles — 8 invisible edge/corner zones for our frameless
 * window (`resizable: false` in tauri.conf, so the OS draws NO frame and NO
 * accent border at all). Each zone starts an OS resize drag in its direction.
 *
 * Zones sit above content (z-[60]) but below popovers/menus; 6px edges are
 * grabbable yet visually nonexistent. Disabled while maximized (nothing to
 * resize) — the parent hides us via the `hidden` prop.
 *
 * NOTE: Tauri window API is dynamically imported so `npm run dev` browser
 * preview never bundles/loads native window code.
 */
type Dir =
  | "East"
  | "North"
  | "NorthEast"
  | "NorthWest"
  | "South"
  | "SouthEast"
  | "SouthWest"
  | "West";

const ZONES: { dir: Dir; className: string; cursor: string }[] = [
  { dir: "North", className: "left-3 right-3 top-0 h-1.5", cursor: "ns-resize" },
  { dir: "South", className: "bottom-0 left-3 right-3 h-1.5", cursor: "ns-resize" },
  { dir: "West", className: "bottom-3 left-0 top-3 w-1.5", cursor: "ew-resize" },
  { dir: "East", className: "bottom-3 right-0 top-3 w-1.5", cursor: "ew-resize" },
  { dir: "NorthWest", className: "left-0 top-0 size-3.5", cursor: "nwse-resize" },
  { dir: "NorthEast", className: "right-0 top-0 size-3.5", cursor: "nesw-resize" },
  { dir: "SouthWest", className: "bottom-0 left-0 size-3.5", cursor: "nesw-resize" },
  { dir: "SouthEast", className: "bottom-0 right-0 size-3.5", cursor: "nwse-resize" },
];

export function ResizeHandles() {
  // No resize targets while maximized.
  const [maximized, setMaximized] = useState(false);

  useEffect(() => {
    if (!isTauri) return;
    let cancelled = false;
    let unlisten: (() => void) | undefined;
    import("@tauri-apps/api/window")
      .then(({ getCurrentWindow }) => {
        if (cancelled) return;
        const win = getCurrentWindow();
        win.isMaximized().then(setMaximized).catch(() => {});
        win
          .onResized(() => win.isMaximized().then(setMaximized).catch(() => {}))
          .then((u) => {
            unlisten = u;
          })
          .catch(() => {});
      })
      .catch(() => {});
    return () => {
      cancelled = true;
      unlisten?.();
    };
  }, []);

  if (!isTauri || maximized) return null;

  const begin = (e: React.PointerEvent, dir: Dir) => {
    // Left-button drags only; don't steal clicks from anything else.
    if (e.button !== 0 || !isTauri) return;
    e.preventDefault();
    e.stopPropagation();
    import("@tauri-apps/api/window")
      .then(({ getCurrentWindow }) => getCurrentWindow().startResizeDragging(dir))
      .catch(() => {});
  };

  return (
    <>
      {ZONES.map(({ dir, className, cursor }) => (
        <div
          key={dir}
          aria-hidden
          className={`absolute z-[60] touch-none select-none ${className}`}
          style={{ cursor }}
          onPointerDown={(e) => begin(e, dir)}
        />
      ))}
    </>
  );
}
