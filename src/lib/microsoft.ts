/**
 * Microsoft → Xbox → XSTS → Minecraft auth.
 *
 * Flow (device code — best for a launcher, no localhost redirect server):
 *   1. POST login.microsoftonline.com/.../devicecode
 *   2. User opens microsoft.com/link, enters code (we show it + auto-open)
 *   3. Poll /token until access_token
 *   4. XboxLive auth → XSTS authorize → MC loginWithXbox → profile
 *
 * DESKTOP: every HTTP step runs in Rust (reqwest) via Tauri commands —
 * WebView fetch hits CORS/network quirks ("Failed to fetch") on some
 * machines, native HTTP has none. BROWSER preview keeps the fetch copies.
 *
 * NEEDS: the launcher's built-in Azure app registration (locked, global).
 * This module never takes a user-supplied ID — without the built-in one,
 * `needClientId` throws and the UI guides the user to offline mode.
 */
import { isTauri } from "./tauri";

const MS = "https://login.microsoftonline.com/consumers/oauth2/v2.0";
const SCOPE = "XboxLive.signin offline_access";

export interface DeviceCode {
  device_code: string;
  user_code: string;
  verification_uri: string;
  /** Deep link with the code pre-filled (when Microsoft returns it). */
  verification_uri_complete?: string;
  expires_in: number;
  interval: number;
  message: string;
}

/** Best URL to open for the user (pre-filled link when available). */
export function deviceVerifyUrl(dc: DeviceCode): string {
  return dc.verification_uri_complete?.trim() || dc.verification_uri;
}

/** True while Microsoft says "user hasn't approved yet" — not an error. */
export function isPendingApproval(msg: string): boolean {
  return /authorization_pending/i.test(msg);
}

/** True when Microsoft asks us to slow down polling. */
export function isSlowDown(msg: string): boolean {
  return /slow_down/i.test(msg);
}

/** True when the code timed out and the user must start over. */
export function isCodeExpired(msg: string): boolean {
  return /expired_token|code expired/i.test(msg);
}

export interface McProfile {
  id: string;
  name: string;
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

function needClientId(clientId: string): string {
  const id = clientId.trim();
  if (!id) {
    throw new Error(
      "Microsoft login is unavailable in this build. Use offline mode for testing.",
    );
  }
  return id;
}

async function cmd<T>(name: string, args: Record<string, unknown>): Promise<T> {
  const { invoke } = await import("@tauri-apps/api/core");
  return invoke<T>(name, args);
}

function netError(e: unknown, step: string): Error {
  const msg = e instanceof Error ? e.message : String(e);
  // Azure misconfiguration is the #1 public-release failure — surface it
  // with the fix, not a raw HTTP code.
  if (/invalid_client|unauthorized_client|AADSTS700016|AADSTS90002/i.test(msg)) {
    return new Error(
      `${step}: Microsoft login is temporarily unavailable. Update the launcher or use offline mode. (${msg.slice(0, 160)})`,
    );
  }
  // Tauri wraps command rejections plainly — surface them with step context.
  if (/failed to fetch|networkerror|load failed|timed out/i.test(msg)) {
    return new Error(`${step}: couldn't reach Microsoft — check connection, firewall or VPN. (${msg})`);
  }
  return new Error(`${step}: ${msg}`);
}

export async function deviceStart(clientId: string): Promise<DeviceCode> {
  const id = needClientId(clientId);
  if (!isTauri) return deviceStartFetch(id);
  try {
    // NOTE: Tauri matches args by exact Rust param name (snake_case).
    return await cmd<DeviceCode>("ms_device_start", { client_id: id });
  } catch (e) {
    throw netError(e, "Microsoft login");
  }
}

export async function devicePoll(clientId: string, dc: DeviceCode): Promise<{ access_token: string; refresh_token: string }> {
  const id = needClientId(clientId);
  if (!isTauri) return devicePollFetch(id, dc);
  try {
    return await cmd<{ access_token: string; refresh_token: string }>("ms_device_poll", {
      client_id: id,
      device_code: dc.device_code,
    });
  } catch (e) {
    throw netError(e, "Microsoft login");
  }
}

/** Full chain: MS access token → MC profile (throws with step context). */
export async function loginWithXbox(msAccess: string, refreshToken = ""): Promise<McProfile> {
  if (!isTauri) return loginWithXboxFetch(msAccess);
  try {
    return await cmd<McProfile>("ms_login_with_xbox", { ms_access: msAccess, refresh_token: refreshToken });
  } catch (e) {
    throw netError(e, "Xbox/Minecraft login");
  }
}

/* ── Automatic browser login (Modrinth-style, no code copy) ───────────────
   1. `authBegin` binds a localhost callback + returns the MS login URL.
   2. Frontend opens the URL in the system browser (normal MS sign-in).
   3. `authWait` resolves when the user finishes → finished MC profile.
   Desktop only (needs the Rust localhost listener). */

export interface AuthBegin {
  authUrl: string;
  redirectUri: string;
}

export async function authBegin(clientId: string): Promise<AuthBegin> {
  const id = needClientId(clientId);
  if (!isTauri) throw new Error("Automatic login needs the desktop app — use device-code or offline mode in the browser preview.");
  try {
    return await cmd<AuthBegin>("ms_auth_begin", { client_id: id });
  } catch (e) {
    throw netError(e, "Microsoft login");
  }
}

export async function authWait(): Promise<McProfile> {
  if (!isTauri) throw new Error("Automatic login needs the desktop app.");
  try {
    return await cmd<McProfile>("ms_auth_wait", {});
  } catch (e) {
    throw netError(e, "Microsoft login");
  }
}

export async function authCancel(): Promise<void> {
  if (!isTauri) return;
  try {
    await cmd<unknown>("ms_auth_cancel", {});
  } catch {
    /* best-effort */
  }
}

/**
 * One-shot Modrinth-style login: open the browser, wait for the callback,
 * return the finished profile. `onBrowserUrl` opens the URL (defaults to
 * the OS browser via the shell plugin). Throws with friendly step context.
 */
export async function loginAutomatic(
  clientId: string,
  onBrowserUrl?: (url: string) => Promise<void> | void,
): Promise<McProfile> {
  const { authUrl } = await authBegin(clientId);
  if (onBrowserUrl) {
    await onBrowserUrl(authUrl);
  } else {
    try {
      const { open } = await import("@tauri-apps/plugin-shell");
      await open(authUrl);
    } catch {
      window.open(authUrl, "_blank", "noopener");
    }
  }
  return authWait();
}

/* ── Automatic login (Modrinth-style, embedded window) ─────────────────────
    One call: an embedded sign-in window opens, the user types email +
    password, the redirect is captured automatically and the finished
    profile is returned. Throws quietly on user-cancel. */

export async function autoLogin(clientId: string): Promise<McProfile> {
  const id = needClientId(clientId);
  if (!isTauri) throw new Error("Microsoft login needs the desktop app — use offline mode in the browser preview.");
  try {
    return await cmd<McProfile>("ms_auto_login", { client_id: id });
  } catch (e) {
    throw netError(e, "Microsoft login");
  }
}

/* ── Paste-code flow (official launcher app ID) ────────────────────────────
    Device-code flow is disabled on Mojang's registration and custom app IDs
    are rejected by Minecraft Services (HTTP 403 "Invalid app registration"),
    so login goes through the system browser + pasted redirect:
    1. Frontend opens `buildAuthUrl` in the OS browser (normal MS sign-in).
    2. Microsoft lands on the desktop.srf redirect with `?code=...`.
    3. User pastes that page address (or just the code) into the launcher.
    4. `exchangeCode` swaps it for tokens and runs the Xbox chain. */

export function buildAuthUrl(clientId: string, redirectUri: string): string {
  const id = needClientId(clientId);
  const q = new URLSearchParams({
    client_id: id,
    response_type: "code",
    redirect_uri: redirectUri,
    scope: SCOPE,
    prompt: "select_account",
  });
  return `${MS}/authorize?${q.toString()}`;
}

/** Accept the full redirect URL or a raw code — return the code. */
export function extractCode(pasted: string): string {
  const v = pasted.trim();
  if (!v) throw new Error("Paste the page address from the browser first.");
  // Full URL with ?code=...
  const m = v.match(/[?&]code=([^&]+)/);
  if (m) return decodeURIComponent(m[1]);
  // Raw code (no URL wrapper).
  if (/^[A-Za-z0-9._\-~]+$/.test(v) && v.length > 10) return v;
  throw new Error("Couldn't find a code in that paste — copy the full page address after signing in.");
}

export async function exchangeCode(clientId: string, pasted: string, redirectUri: string): Promise<McProfile> {
  const id = needClientId(clientId);
  const code = extractCode(pasted);
  if (!isTauri) throw new Error("Microsoft login needs the desktop app — use offline mode in the browser preview.");
  try {
    return await cmd<McProfile>("ms_auth_exchange", { client_id: id, code, redirect_uri: redirectUri });
  } catch (e) {
    throw netError(e, "Microsoft login");
  }
}

/** Refresh an expired Microsoft session via stored refresh_token. */
export async function refreshMicrosoft(clientId: string, refreshToken: string): Promise<{ access_token: string; refresh_token: string }> {
  const id = needClientId(clientId);
  if (!refreshToken) throw new Error("No refresh token — sign in again.");
  if (!isTauri) throw new Error("Token refresh needs the desktop app.");
  try {
    return await cmd<{ access_token: string; refresh_token: string }>("ms_refresh", {
      client_id: id,
      refresh_token: refreshToken,
    });
  } catch (e) {
    throw netError(e, "Session refresh");
  }
}

/* ── Browser-preview fetch copies (npm run dev only) ─────────────────────── */

async function deviceStartFetch(id: string): Promise<DeviceCode> {
  let res: Response;
  try {
    res = await fetch(`${MS}/devicecode`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ client_id: id, scope: SCOPE }),
    });
  } catch (e) {
    throw new Error(`Microsoft unreachable from the browser preview — use the desktop app. (${e instanceof Error ? e.message : String(e)})`);
  }
  if (!res.ok) throw new Error(`Microsoft devicecode failed (${res.status})`);
  return (await res.json()) as DeviceCode;
}

async function devicePollFetch(id: string, dc: DeviceCode): Promise<{ access_token: string; refresh_token: string }> {
  let res: Response;
  try {
    res = await fetch(`${MS}/token`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        grant_type: "urn:ietf:params:oauth:grant-type:device_code",
        client_id: id,
        device_code: dc.device_code,
      }),
    });
  } catch (e) {
    throw new Error(`Microsoft unreachable from the browser preview. (${e instanceof Error ? e.message : String(e)})`);
  }
  const j = (await res.json()) as Record<string, string>;
  if (!res.ok) {
    // Expected while waiting: authorization_pending / slow_down / expired_token
    throw new Error(j.error ?? `token poll failed (${res.status})`);
  }
  return { access_token: j.access_token, refresh_token: j.refresh_token };
}

async function postJson<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`${url} → ${res.status} ${text.slice(0, 160)}`);
  }
  return (await res.json()) as T;
}

/** Full chain: MS access token → MC profile (throws with step context). */
async function loginWithXboxFetch(msAccess: string): Promise<McProfile> {
  // 1. Xbox Live
  const xbl = await postJson<{ Token: string; DisplayClaims: { xui: { uhs: string }[] } }>(
    "https://user.auth.xboxlive.com/user/authenticate",
    {
      Properties: { AuthMethod: "RPS", SiteName: "user.auth.xboxlive.com", RpsTicket: `d=${msAccess}` },
      RelyingParty: "http://auth.xboxlive.com",
      TokenType: "JWT",
    },
  );
  const uhs = xbl.DisplayClaims.xui[0].uhs;

  // 2. XSTS
  const xsts = await postJson<{ Token: string }>("https://xsts.auth.xboxlive.com/xsts/authorize", {
    Properties: { SandboxId: "RETAIL", UserTokens: [xbl.Token] },
    RelyingParty: "rp://api.minecraftservices.com/",
    TokenType: "JWT",
  });

  // 3. Minecraft
  const mc = await postJson<{ access_token: string; expires_in: number }>(
    "https://api.minecraftservices.com/authentication/login_with_xbox",
    { identityToken: `XBL3.0 x=${uhs};${xsts.Token}` },
  );

  // 4. Profile (also proves game ownership — 404 = no copy of MC)
  const profRes = await fetch("https://api.minecraftservices.com/minecraft/profile", {
    headers: { Authorization: `Bearer ${mc.access_token}` },
  });
  if (profRes.status === 404) throw new Error("No Minecraft copy on this account (404).");
  if (!profRes.ok) throw new Error(`Profile fetch failed (${profRes.status})`);
  const prof = (await profRes.json()) as { id: string; name: string };

  return {
    id: prof.id.replace(/-/g, ""),
    name: prof.name,
    accessToken: mc.access_token,
    refreshToken: "",
    expiresAt: Date.now() + mc.expires_in * 1000,
  };
}

/* ── Owned skins (Skins page — desktop via Rust, no CORS pain) ──── */

export interface OwnedSkin {
  id: string;
  state: string;
  url: string;
  variant: "CLASSIC" | "SLIM";
  alias?: string;
}

export interface OwnedProfile {
  id: string;
  name: string;
  skins: OwnedSkin[];
}

/** Everything Microsoft owns on this account (active flags in `state`). */
export async function mcProfile(mcAccessToken: string): Promise<OwnedProfile> {
  if (!mcAccessToken) throw new Error("No Minecraft session — sign in again.");
  if (!isTauri) throw new Error("Skin library needs the desktop app.");
  try {
    return await cmd<OwnedProfile>("ms_profile", { mc_access: mcAccessToken });
  } catch (e) {
    throw netError(e, "Skin library");
  }
}

/** Equip an owned skin (by Mojang skin id + variant). Returns the profile. */
export async function equipSkin(
  mcAccessToken: string,
  skinId: string,
  variant: "CLASSIC" | "SLIM",
): Promise<OwnedProfile> {
  if (!isTauri) throw new Error("Skin equip needs the desktop app.");
  try {
    return await cmd<OwnedProfile>("ms_equip_skin", {
      mc_access: mcAccessToken,
      skin_id: skinId,
      variant,
    });
  } catch (e) {
    throw netError(e, "Skin change");
  }
}
