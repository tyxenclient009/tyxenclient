/** Shared "press Play" pipeline used by Home cards + detail drawer. */
import { launchGame } from "./backend";
import { useAccounts } from "@/stores/accounts";
import { useInstances } from "@/stores/instances";
import { useLaunch } from "@/stores/launch";
import { useServers } from "@/stores/servers";
import { useSettings, AZURE_CLIENT_ID } from "@/stores/settings";
import { useToasts } from "@/stores/app";

export async function playInstance(instanceId: string, opts?: { server?: string; port?: number }): Promise<void> {
  const inst = useInstances.getState().instances.find((i) => i.id === instanceId);
  if (!inst) throw new Error("Instance not found");
  let account = useAccounts.getState().active();
  if (!account) throw new Error("No account — sign in (sidebar) first");
  // Auto-refresh expired Microsoft sessions (Modrinth-app parity: never
  // fail a launch with a stale token when a refresh_token exists).
  // 5-minute early-refresh buffer matches accounts.isSessionValid().
  if (account.kind === "microsoft" && (!account.expiresAt || account.expiresAt < Date.now() + 5 * 60_000) && account.refreshToken) {
    try {
      const { refreshMicrosoft, loginWithXbox } = await import("@/lib/microsoft");
      const tok = await refreshMicrosoft(AZURE_CLIENT_ID, account.refreshToken);
      const prof = await loginWithXbox(tok.access_token, tok.refresh_token);
      useAccounts.getState().updateTokens(account.id, prof.accessToken, tok.refresh_token || account.refreshToken, prof.expiresAt);
      account = useAccounts.getState().active() ?? account;
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      useToasts.getState().push({
        kind: "error",
        title: "Microsoft session expired — sign in again",
        body: `${msg} — remove this account in the sidebar, then choose Login with Microsoft again.`,
      });
      throw e;
    }
  }
  // Expired Microsoft session with no refresh token (e.g. accounts added
  // by an old build): launching now would boot with a dead token and die
  // at "Invalid session". Fail loudly instead so the user re-signs in.
  if (account.kind === "microsoft" && !useAccounts.getState().isSessionValid()) {
    const err = new Error(
      "Microsoft session expired and cannot be refreshed — remove this account in the sidebar, then choose Login with Microsoft again.",
    );
    useToasts.getState().push({
      kind: "error",
      title: "Microsoft session expired — sign in again",
      body: err.message,
    });
    throw err;
  }
  const global = useSettings.getState();
  const launch = useLaunch.getState();

  // This SAME instance already running (or still launching)? Don't pile
  // another backend thread on — point at the running game instead of a
  // misleading "Launching…" toast plus a console error per press.
  // Other instances may run in parallel (Modrinth-style multi-launch).
  if (launch.isRunning(inst.id)) {
    useToasts.getState().push({
      kind: "info",
      title: `${inst.name} is already running`,
      body: "Stop it first (its Stop button, or the console) before pressing Play on it again. Other instances can still launch.",
    });
    // Don't reopen / clear the console — the running game already owns it.
    return;
  }

  launch.clear();
  launch.setOpen(true);
  // NOTE: running state is NOT set here — the backend's "[tyx] pid …"
  // line flips it only after the game process actually spawns.
  // Offline skin: the account's library skin is woven into the instance as
  // a resource pack (or removed when none is selected). Non-fatal — Play
  // continues with the default Steve/Alex look on any failure.
  if (account.kind === "offline") {
    try {
      const skins = await import("@/lib/skins");
      const store = (await import("@/stores/skins")).useSkins.getState();
      const sel = store.selection(account.id);
      const entry = store.skins.find((s) => s.id === sel.skinId);
      if (entry) {
        await skins.applyOfflineSkin(inst.id, inst.version, await skins.loadSkinBytes(entry), entry.model);
      } else if (sel.defaultSkinId) {
        const { getDefaultSkin } = await import("@/lib/default-skins");
        const def = getDefaultSkin(sel.defaultSkinId);
        if (def) {
          // Bundled vanilla skins live as local files (e.g. /skins/vanilla/steve.png) — fetch bytes for the pack.
          const res = await fetch(def.make());
          if (!res.ok) throw new Error(`Couldn't load ${def.name} texture`);
          await skins.applyOfflineSkin(inst.id, inst.version, new Uint8Array(await res.arrayBuffer()), def.model);
        } else {
          await skins.clearOfflineSkin(inst.id);
        }
      } else {
        await skins.clearOfflineSkin(inst.id);
      }
    } catch {
      /* default skin it is */
    }
  }
  // Detached log window (own OS window, survives navigation). Non-fatal if
  // it fails — but NEVER silent: the user must see why "nothing happened".
  try {
    const m = await import("@/lib/console-window");
    await m.openConsoleWindow();
  } catch (e) {
    useToasts.getState().push({
      kind: "error",
      title: "Console didn't open",
      body: `${e instanceof Error ? e.message : String(e)} — launch continues, watch Home → Console.`,
    });
  }
  // Auto-heal known-crashing mod builds (e.g. ImmediatelyFast 1.14.2 on
  // 1.21.11 → MixinShaderManager crash). Updates when online, disables
  // when offline. Best-effort — Play continues on any failure.
  try {
    const { quarantineIncompatibleMods } = await import("@/lib/incompat-mods");
    await quarantineIncompatibleMods(inst.id, inst.version, inst.loader, (kind, title, body) =>
      useToasts.getState().push({ kind, title, body }),
    );
  } catch {
    /* never block Play */
  }
  // Tyx Client auto-inject (Lunar parity): Fabric instances on a supported
  // MC version get the companion mod + Fabric API before boot. Best-effort —
  // Play continues on any failure. The Fabric *loader* itself installs for
  // real now (src-tauri/src/loader.rs merges the KnotClient profile during
  // asset ensure), so the jars below actually boot.
  try {
    const { ensureTyxMod } = await import("@/lib/tyxmod");
    await ensureTyxMod(inst.id, inst.version, inst.loader, (kind, title, body) =>
      useToasts.getState().push({ kind, title, body }),
    );
  } catch {
    /* never block Play */
  }
  try {
    await launchGame({
      instanceId: inst.id,
      username: account.name,
      uuid: account.id,
      token: account.accessToken ?? "0",
      ramMb: inst.settings.ramMb || global.defaultRamMb,
      javaPath: inst.settings.javaPath || global.javaPath,
      jvmArgs: inst.settings.jvmArgs,
      resW: inst.settings.resW,
      resH: inst.settings.resH,
      server: opts?.server?.trim() || undefined,
      port: opts?.port || undefined,
      // Saved servers ride along so the in-game Multiplayer list is
      // always fresh (merged, hand-added rows preserved).
      servers: useServers.getState().saved.map((s) => ({ name: s.name, host: s.host, port: s.port })),
    });
    // Immediate proof the button worked — backend progress (Java install,
    // game download, JVM boot) streams into the Console window and can take
    // minutes on first launch. Without this toast a slow first launch looks
    // exactly like a dead button.
    useToasts.getState().push({
      kind: "info",
      title: `Launching ${inst.name} as ${account.name}`,
      body: "Follow progress in the Console window.",
    });
    // Completion / failure arrives via game-log events → listener flips running.
    void useInstances.getState().refresh().catch(() => {});
  } catch (e) {
    launch.markStopped(inst.id);
    useToasts.getState().push({
      kind: "error",
      title: "Launch failed",
      body: e instanceof Error ? e.message : String(e),
    });
    throw e;
  }
}
