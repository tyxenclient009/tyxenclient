/**
 * Background scroll lock for overlays (modals, drawers, palettes).
 *
 * The app shell never scrolls `body` — the scroll container is AppShell's
 * `<main>`. So locking `body` alone does nothing; instead we toggle a
 * `tyx-modal-open` class on <html> and CSS forces every scroll container
 * shut (see globals.css). Ref-counted: nested overlays (dialog over a
 * drawer) only unlock when the last one closes.
 */
import { useEffect } from "react";

let depth = 0;

function sync() {
  if (typeof document === "undefined") return;
  document.documentElement.classList.toggle("tyx-modal-open", depth > 0);
}

/** Hold the lock until the returned releaser runs (idempotent). */
export function lockScroll(): () => void {
  depth += 1;
  sync();
  let released = false;
  return () => {
    if (released) return;
    released = true;
    depth = Math.max(0, depth - 1);
    sync();
  };
}

/** React helper — locks the background while `open` is true. */
export function useScrollLock(open: boolean) {
  useEffect(() => {
    if (!open) return;
    return lockScroll();
  }, [open ]);
}
