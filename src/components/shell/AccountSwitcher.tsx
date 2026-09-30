import { useState } from "react";
import { ArrowLeft, Check, ExternalLink, Loader2, LogOut, Plus, X } from "lucide-react";
import { Popover, PopoverClose, PopoverPanel, PopoverTrigger } from "@/components/ui/Popover";
import { Button } from "@/components/ui/Button";
import { GuestIcon, MicrosoftLogo, TerminalIcon } from "@/components/icons/brand";
import { useAccounts } from "@/stores/accounts";
import { useToasts } from "@/stores/app";
import { AZURE_CLIENT_ID, MS_REDIRECT_URI } from "@/stores/settings";
import { cn } from "@/lib/utils";

export type AuthMethod = "offline" | "microsoft";
type View = "menu" | "choose" | AuthMethod;

/**
 * Account switcher — ONE Radix popover, multiple views.
 *   menu      (w-80)  account rows, or the two method cards when signed out
 *   choose    (w-96)  compact method picker (from "Add account")
 *   offline   (w-96)  username → Start
 *   microsoft (w-96)  setup guide → device-code steps
 *
 * Everything renders in a portal at an explicit width — never constrained
 * by the 220px sidebar, never bleeding nav text through.
 */
export function AccountSwitcher({ compact = false }: { compact?: boolean }) {
  const active = useAccounts((s) => s.active());
  const [open, setOpen] = useState(false);
  const [view, setView] = useState<View>("menu");

  const close = () => setOpen(false);
  const onOpenChange = (o: boolean) => {
    setOpen(o);
    if (!o) setTimeout(() => setView("menu"), 150); // reset after close fade
  };

  const goDetail = (m: AuthMethod) => setView(m);
  const wide = view !== "menu";

  return (
    <Popover open={open} onOpenChange={onOpenChange}>
      <PopoverTrigger>
        <button
          title={active ? `${active.name} (${active.kind})` : "Sign in"}
          className={cn(
            "flex h-12 w-full items-center gap-3 rounded-md px-3 text-left transition-colors duration-100",
            "hover:bg-surface-200/70 active:scale-[0.99]",
            "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50",
            compact && "justify-center px-0",
          )}
        >
          {active ? (
            <>
              <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-accent-500/20 text-sm font-bold text-accent-400">
                {active.name.slice(0, 1).toUpperCase()}
              </span>
              {!compact && (
                <span className="flex min-w-0 flex-col items-start leading-tight">
                  <span className="max-w-full truncate text-[13px] font-semibold text-ink">{active.name}</span>
                  <span className="text-[11px] text-ink-faint">{active.kind === "microsoft" ? "Microsoft" : "Offline"}</span>
                </span>
              )}
            </>
          ) : (
            <>
              <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-surface-300 text-ink-muted">
                <GuestIcon />
              </span>
              {!compact && (
                <span className="flex flex-col items-start leading-tight">
                  <span className="whitespace-nowrap text-[13px] font-semibold text-ink">Sign in</span>
                  <span className="whitespace-nowrap text-[11px] text-ink-faint">Offline or Microsoft</span>
                </span>
              )}
            </>
          )}
        </button>
      </PopoverTrigger>

      <PopoverPanel side="top" align="start" sideOffset={12} className={cn(wide && "w-96", "max-h-[80vh] overflow-y-auto")}>
        {view === "menu" && <MenuView onDetail={goDetail} onChoose={() => setView("choose")} onDone={close} />}
        {view === "choose" && <ChooseView onDetail={goDetail} onBack={() => setView("menu")} />}
        {view === "offline" && <OfflineView onBack={() => setView("menu")} onDone={close} />}
        {view === "microsoft" && <MicrosoftView onBack={() => setView("menu")} onDone={close} />}
      </PopoverPanel>
    </Popover>
  );
}

/** Header row per spec: back chevron + title + close, border-bottom. */
function PanelHeader({ title, onBack }: { title: string; onBack?: () => void }) {
  return (
    <div className="-mx-4 -mt-4 mb-3 flex items-center gap-1 border-b border-white/10 px-3 py-2.5">
      {onBack ? (
        <button
          onClick={onBack}
          aria-label="Back"
          className="rounded-md p-1.5 text-ink-muted transition hover:bg-surface-200/70 hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50"
        >
          <ArrowLeft className="size-4" />
        </button>
      ) : (
        <span className="w-7" />
      )}
      <p className="flex-1 truncate text-center text-sm font-bold">{title}</p>
      <PopoverClose asChild>
        <button
          aria-label="Close"
          className="rounded-md p-1.5 text-ink-muted transition hover:bg-surface-200/70 hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50"
        >
          <X className="size-4" />
        </button>
      </PopoverClose>
    </div>
  );
}

/* ── menu ──────────────────────────────────────────────────────────────────── */

function MenuView({
  onDetail,
  onChoose,
  onDone,
}: {
  onDetail: (m: AuthMethod) => void;
  onChoose: () => void;
  onDone: () => void;
}) {
  const accounts = useAccounts((s) => s.accounts);
  const active = useAccounts((s) => s.active());
  const switchTo = useAccounts((s) => s.switchTo);
  const remove = useAccounts((s) => s.remove);
  const push = useToasts((s) => s.push);

  // Signed out → the two method cards live right here (no extra hop).
  if (accounts.length === 0) {
    return (
      <div>
        <PanelHeader title="Sign in" />
        <div className="flex flex-col gap-3">
          <button
            onClick={() => onDetail("offline")}
            className="rounded-lg border border-white/10 p-3 text-left transition-all duration-150 hover:border-accent-500/40 hover:shadow-2 active:scale-[0.99] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50"
          >
            <span className="flex items-center gap-2">
              <span className="flex size-7 items-center justify-center rounded-md bg-surface-300 text-ink-muted">
                <TerminalIcon />
              </span>
              <span className="text-sm font-semibold">Offline</span>
              <span className="ml-auto rounded-full bg-surface-300 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider text-ink-muted">
                dev / test
              </span>
            </span>
            <span className="mt-1.5 block text-xs leading-snug text-ink-muted">
              Instant username, no password. Single-player + offline servers only.
            </span>
          </button>

          <button
            onClick={() => onDetail("microsoft")}
            className="rounded-lg border border-white/10 p-3 text-left transition-all duration-150 hover:border-accent-500/40 hover:shadow-2 active:scale-[0.99] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50"
          >
            <span className="flex items-center gap-2">
              <MicrosoftLogo />
              <span className="whitespace-nowrap text-sm font-semibold">Login with Microsoft</span>
            </span>
            <span className="mt-1.5 block text-xs leading-snug text-ink-muted">
              Real account with game ownership. Needed for online servers.
            </span>
          </button>
        </div>
      </div>
    );
  }

  // Signed in → rows + add.
  return (
    <div>
      <PanelHeader title={`Accounts (${accounts.length})`} />
      <div className="flex flex-col gap-1">
        {accounts.map((a) => {
          const isActive = active?.id === a.id;
          return (
            <div
              key={a.id}
              className={cn(
                "group flex items-center gap-2 rounded-md px-2 py-1.5 transition-colors duration-100",
                isActive ? "bg-surface-200/80" : "hover:bg-surface-200/50",
              )}
            >
              <button
                onClick={() => {
                  switchTo(a.id);
                  onDone();
                }}
                className="flex min-w-0 flex-1 items-center gap-2 rounded text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50"
              >
                <span
                  className={cn(
                    "flex size-7 shrink-0 items-center justify-center rounded-full text-xs font-bold",
                    a.kind === "microsoft" ? "bg-accent-500/20 text-accent-400" : "bg-surface-300 text-ink-muted",
                  )}
                >
                  {a.name.slice(0, 1).toUpperCase()}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-[13px] font-semibold">{a.name}</span>
                  <span className="block text-[11px] text-ink-faint">{a.kind === "microsoft" ? "Microsoft" : "Offline"}</span>
                </span>
                {isActive && <Check className="size-3.5 shrink-0 text-accent-400" />}
              </button>
              <button
                aria-label={`Remove ${a.name}`}
                onClick={() => {
                  remove(a.id);
                  push({ kind: "info", title: `Removed ${a.name}` });
                }}
                className="rounded p-1.5 text-ink-faint opacity-0 transition hover:bg-red-500/10 hover:text-red-400 focus-visible:opacity-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50 group-hover:opacity-100"
              >
                <LogOut className="size-3.5" />
              </button>
            </div>
          );
        })}
      </div>
      <Button variant="secondary" size="sm" className="mt-3 w-full" onClick={onChoose}>
        <Plus /> <span className="whitespace-nowrap">Add account</span>
      </Button>
    </div>
  );
}

/* ── choose (from "Add account") ───────────────────────────────────────────── */

function ChooseView({ onDetail, onBack }: { onDetail: (m: AuthMethod) => void; onBack: () => void }) {
  const row =
    "flex w-full items-center gap-3 rounded-lg border border-white/10 p-3 text-left transition-all duration-150 hover:border-accent-500/40 hover:shadow-2 active:scale-[0.99] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50";
  return (
    <div>
      <PanelHeader title="Add account" onBack={onBack} />
      <div className="flex flex-col gap-2">
        <button onClick={() => onDetail("offline")} className={row}>
          <span className="flex size-9 shrink-0 items-center justify-center rounded-md bg-surface-300 text-ink-muted">
            <TerminalIcon className="size-5" />
          </span>
          <span className="min-w-0 flex-1">
            <span className="block text-sm font-semibold">Offline</span>
            <span className="block truncate text-xs text-ink-muted">Instant username, test servers</span>
          </span>
        </button>
        <button onClick={() => onDetail("microsoft")} className={row}>
          <span className="flex size-9 shrink-0 items-center justify-center rounded-md bg-surface-300">
            <MicrosoftLogo className="size-5" />
          </span>
          <span className="min-w-0 flex-1">
            <span className="block whitespace-nowrap text-sm font-semibold">Login with Microsoft</span>
            <span className="block truncate text-xs text-ink-muted">Real account, online servers</span>
          </span>
        </button>
      </div>
    </div>
  );
}

/* ── offline detail ────────────────────────────────────────────────────────── */

function OfflineView({ onBack, onDone }: { onBack: () => void; onDone: () => void }) {
  const addOffline = useAccounts((s) => s.addOffline);
  const push = useToasts((s) => s.push);
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    setBusy(true);
    try {
      const acc = await addOffline(name);
      push({
        kind: "success",
        title: `Signed in as ${acc.name}`,
        body: "Offline mode — servers with online-mode=true will reject you.",
      });
      onDone();
    } catch (e) {
      push({ kind: "error", title: "Invalid username", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div>
      <PanelHeader title="Play offline" onBack={onBack} />
      <label className="label" htmlFor="sw-name">
        Username (3–16 letters, numbers, _)
      </label>
      <input
        id="sw-name"
        className="input h-11 text-[15px]"
        placeholder="Steve_123"
        value={name}
        maxLength={16}
        autoFocus
        onChange={(e) => setName(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && submit()}
      />
      <Button variant="primary" size="lg" className="mt-3 w-full" onClick={submit} disabled={busy}>
        {busy ? <Loader2 className="animate-spin" /> : null}
        <span className="whitespace-nowrap">{busy ? "Starting…" : "Start playing offline"}</span>
      </Button>
    </div>
  );
}

/* ── microsoft detail ──────────────────────────────────────────────────────── */

function MicrosoftView({ onBack, onDone }: { onBack: () => void; onDone: () => void }) {
  const addMicrosoft = useAccounts((s) => s.addMicrosoft);
  const push = useToasts((s) => s.push);
  const [mode, setMode] = useState<"auto" | "paste">("auto");
  const [pasted, setPasted] = useState("");
  const [busy, setBusy] = useState(false);
  const [autoBusy, setAutoBusy] = useState(false);
  const [browserOpened, setBrowserOpened] = useState(false);

  const startAuto = async () => {
    if (autoBusy) return;
    setAutoBusy(true);
    try {
      const { autoLogin } = await import("@/lib/microsoft");
      const prof = await autoLogin(AZURE_CLIENT_ID);
      addMicrosoft({
        id: prof.id,
        name: prof.name,
        kind: "microsoft",
        accessToken: prof.accessToken,
        refreshToken: prof.refreshToken,
        expiresAt: prof.expiresAt,
      });
      push({
        kind: "success",
        title: `Signed in as ${prof.name}`,
        body: "Microsoft account verified + game ownership confirmed.",
      });
      onDone();
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      if (/cancelled|timed out/i.test(msg)) return; // user closed the window — stay quiet
      push({ kind: "error", title: "Login failed", body: friendlyAuthError(msg) });
    } finally {
      setAutoBusy(false);
    }
  };

  const openBrowser = async () => {
    try {
      const { buildAuthUrl } = await import("@/lib/microsoft");
      const url = buildAuthUrl(AZURE_CLIENT_ID, MS_REDIRECT_URI);
      try {
        const { open } = await import("@tauri-apps/plugin-shell");
        await open(url);
      } catch {
        const popup = window.open(url, "_blank", "noopener");
        if (!popup) {
          push({
            kind: "info",
            title: "Popup blocked",
            body: "Allow popups so Microsoft login can open, then retry.",
          });
          return;
        }
      }
      setBrowserOpened(true);
    } catch (e) {
      push({ kind: "error", title: "Microsoft login failed", body: friendlyAuthError(e instanceof Error ? e.message : String(e)) });
    }
  };

  const submit = async () => {
    if (busy) return;
    setBusy(true);
    try {
      const { exchangeCode } = await import("@/lib/microsoft");
      const prof = await exchangeCode(AZURE_CLIENT_ID, pasted, MS_REDIRECT_URI);
      addMicrosoft({
        id: prof.id,
        name: prof.name,
        kind: "microsoft",
        accessToken: prof.accessToken,
        refreshToken: prof.refreshToken,
        expiresAt: prof.expiresAt,
      });
      push({
        kind: "success",
        title: `Signed in as ${prof.name}`,
        body: "Microsoft account verified + game ownership confirmed.",
      });
      onDone();
    } catch (e) {
      push({ kind: "error", title: "Login failed", body: friendlyAuthError(e instanceof Error ? e.message : String(e)) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div>
      <PanelHeader title="Login with Microsoft" onBack={onBack} />
      <div className="flex items-center gap-2.5">
        <MicrosoftLogo className="size-6" />
        <p className="whitespace-nowrap text-sm text-ink-muted">Sign in with your Microsoft account.</p>
      </div>

      <div className="mt-4 space-y-4">
        {mode === "auto" ? (
          <>
            <Button variant="primary" size="lg" className="w-full" onClick={startAuto} disabled={autoBusy}>
              {autoBusy ? <Loader2 className="animate-spin" /> : <MicrosoftLogo />}
              <span className="whitespace-nowrap">{autoBusy ? "Waiting for sign-in…" : "Login with Microsoft"}</span>
            </Button>
            {autoBusy ? (
              <p className="text-center text-[13px] text-ink-muted">
                A sign-in window opened — enter your email and password there. You will be signed in here automatically.
              </p>
            ) : (
              <button
                onClick={() => setMode("paste")}
                className="w-full rounded py-1 text-center text-[13px] text-ink-faint transition hover:text-ink"
              >
                Having trouble? Paste the address instead
              </button>
            )}
          </>
        ) : (
          <>
            <StepRow n="1" title="Sign in in your browser" body="Use the Microsoft account that owns Minecraft.">
              <Button variant="secondary" size="sm" onClick={openBrowser}>
                <ExternalLink /> <span className="whitespace-nowrap">Open Microsoft login</span>
              </Button>
            </StepRow>
            <StepRow n="2" title="Paste the page address" body="After signing in, copy the full address of the page you land on and paste it below.">
              <textarea
                value={pasted}
                onChange={(e) => setPasted(e.target.value)}
                rows={3}
                spellCheck={false}
                placeholder="https://login.live.com/oauth20_desktop.srf?code=..."
                aria-label="Pasted Microsoft redirect address"
                className="input h-auto min-h-[76px] resize-y py-2 font-mono text-xs leading-relaxed"
              />
            </StepRow>
            <StepRow n="3" title="Finish signing in" body="We exchange the code and verify game ownership.">
              <Button variant="primary" size="lg" className="w-full" onClick={submit} disabled={busy || pasted.trim().length === 0}>
                {busy ? <Loader2 className="animate-spin" /> : <MicrosoftLogo />}
                <span className="whitespace-nowrap">{busy ? "Verifying…" : "Sign in"}</span>
              </Button>
              {!browserOpened && (
                <p className="mt-2 text-center text-[13px] text-ink-faint">Open the Microsoft login first (step 1).</p>
              )}
            </StepRow>
            <button
              onClick={() => setMode("auto")}
              className="w-full rounded py-1 text-center text-[13px] text-ink-faint transition hover:text-ink"
            >
              ← Back to automatic login
            </button>
          </>
        )}
      </div>
    </div>
  );
}

/** Map raw Xbox/Microsoft errors to actionable user text (public-release UX). */
export function friendlyAuthError(msg: string): string {
  if (/No Minecraft copy|404/i.test(msg)) return "This Microsoft account owns no copy of Minecraft (404). Buy it or use offline mode.";
  if (/XErr 2148916233/i.test(msg)) return "No Xbox profile on this account — create one at xbox.com once, then retry.";
  if (/XErr 2148916238/i.test(msg)) return "Child/family account blocked by Xbox safety settings — an adult must approve at account.xbox.com/settings.";
  if (/Invalid app registration|aka\.ms\/AppRegInfo/i.test(msg)) return "Mojang hasn't authorized this launcher's app yet — new apps need Mojang's approval (see https://aka.ms/AppRegInfo). Use offline mode until the approval comes through.";
  if (/invalid_client|unauthorized_client|AADSTS/i.test(msg)) return "Microsoft login is temporarily unavailable — update the launcher or use offline mode.";
  if (/Session expired/i.test(msg)) return msg;
  return msg;
}

function StepRow({
  n,
  title,
  body,
  children,
}: {
  n: string;
  title: string;
  body: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex gap-3">
      <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-accent-500/15 font-mono text-[13px] font-bold text-accent-400">
        {n}
      </span>
      <div className="min-w-0 flex-1">
        <p className="whitespace-nowrap text-sm font-semibold">{title}</p>
        <p className="mb-2 text-[13px] text-ink-muted">{body}</p>
        {children}
      </div>
    </div>
  );
}
