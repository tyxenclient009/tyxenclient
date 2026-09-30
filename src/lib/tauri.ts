/**
 * Tauri interop helpers that degrade gracefully to the browser.
 *
 * WHY: `npm run dev` (plain Vite) must work for UI iteration even before
 * `cargo` is installed. Every helper below no-ops in the browser with a
 * console warning instead of throwing.
 */
export const isTauri =
  typeof window !== "undefined" &&
  ("__TAURI__" in window || "__TAURI_INTERNALS__" in window);

export async function windowMinimize() {
  if (!isTauri) return;
  const { getCurrentWindow } = await import("@tauri-apps/api/window");
  await getCurrentWindow().minimize();
}

export async function windowToggleMaximize() {
  if (!isTauri) return;
  const { getCurrentWindow } = await import("@tauri-apps/api/window");
  const win = getCurrentWindow();
  (await win.isMaximized()) ? await win.unmaximize() : await win.maximize();
}

export async function windowClose() {
  if (!isTauri) {
    window.close();
    return;
  }
  const { getCurrentWindow } = await import("@tauri-apps/api/window");
  await getCurrentWindow().close();
}

/** Thin wrapper around a Tauri command with a browser fallback. */
export async function invokeOr<T>(cmd: string, args: unknown, fallback: T): Promise<T> {
  if (!isTauri) {
    console.warn(`[browser-stub] invoke("${cmd}") → using fallback`, args);
    return fallback;
  }
  const { invoke } = await import("@tauri-apps/api/core");
  return invoke<T>(cmd, args as Record<string, unknown>);
}
