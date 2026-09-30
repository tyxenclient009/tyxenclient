/** Detached game-console window — one per app, focused if already open. */
import { isTauri } from "./tauri";

const LABEL = "game-console";

/**
 * Opens (or focuses) the console window. NEVER fails silently — every
 * failure throws a human-readable error so Play problems stay visible
 * instead of looking like "nothing happened".
 */
export async function openConsoleWindow(): Promise<void> {
  if (!isTauri) {
    // Browser preview: popup tab running the same ?console route.
    const popup = window.open(`${location.pathname}?console`, "_blank", "width=960,height=640");
    if (!popup) throw new Error("Popup blocked — allow popups for this site to open the console.");
    return;
  }
  let mod: typeof import("@tauri-apps/api/webviewWindow");
  try {
    mod = await import("@tauri-apps/api/webviewWindow");
  } catch (e) {
    throw new Error(`Window API unavailable: ${e instanceof Error ? e.message : String(e)}`);
  }
  const { WebviewWindow } = mod;
  let existing: Awaited<ReturnType<typeof WebviewWindow.getByLabel>>;
  try {
    existing = await WebviewWindow.getByLabel(LABEL);
  } catch (e) {
    throw new Error(`Console lookup failed (missing window permission?): ${e instanceof Error ? e.message : String(e)}`);
  }
  if (existing) {
    await existing.unminimize().catch(() => {});
    await existing.setFocus().catch(() => {});
    return;
  }
  let win: InstanceType<typeof WebviewWindow>;
  try {
    win = new WebviewWindow(LABEL, {
      url: "index.html?console",
      title: "Game Console — Tyx Launcher",
      width: 960,
      height: 640,
      minWidth: 560,
      minHeight: 380,
      center: true,
      resizable: true,
      // Frameless by design (same as the main window): the React header
      // owns min/max/close + drag. Native decorations would double-chrome it.
      decorations: false,
      transparent: false,
      shadow: true,
      closable: true,
      minimizable: true,
      maximizable: true,
      alwaysOnTop: false,
      skipTaskbar: false,
      focus: true,
    });
  } catch (e) {
    throw new Error(`Console creation denied: ${e instanceof Error ? e.message : String(e)}`);
  }
  // Creation is async under the hood — surface backend rejections as a toast
  // instead of dropping them into the devtools void.
  win.once("tauri://error", async (e: unknown) => {
    const { useToasts } = await import("@/stores/app");
    useToasts.getState().push({
      kind: "error",
      title: "Console window failed",
      body: typeof e === "string" ? e : JSON.stringify(e).slice(0, 200),
    });
  });
}
