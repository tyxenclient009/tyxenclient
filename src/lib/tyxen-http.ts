/** Fetch helper for Tyxen backend URLs (plain http://). Bypasses WebView mixed-content blocking via the Tauri HTTP plugin. */

export const TYXEN_API = "http://api.tyxen.space:11789";

export async function tyxenFetch(path: string, init?: RequestInit, timeoutMs = 8000): Promise<Response> {
  const url = path.startsWith("http") ? path : TYXEN_API + path;
  try {
    const { fetch: pluginFetch } = await import("@tauri-apps/plugin-http");
    const ctrl = new AbortController();
    const t = setTimeout(() => ctrl.abort(), timeoutMs);
    try {
      const res = await pluginFetch(url, { ...(init ?? {}), signal: ctrl.signal });
      clearTimeout(t);
      return res as Response;
    } catch (e) {
      clearTimeout(t);
      throw e;
    }
  } catch {
    const ctrl = new AbortController();
    const t = setTimeout(() => ctrl.abort(), timeoutMs);
    try {
      const res = await fetch(url, { ...(init ?? {}), signal: ctrl.signal });
      clearTimeout(t);
      return res;
    } catch (e) {
      clearTimeout(t);
      throw e;
    }
  }
}
