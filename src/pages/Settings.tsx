import { useState } from "react";
import { Check, Coffee, Cpu, FolderOpen, Loader2, Moon, Monitor, RefreshCw, Sun } from "lucide-react";
import { FadeUp, Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { useSettings } from "@/stores/settings";
import { useTheme, useToasts } from "@/stores/app";
import { detectJava, ensureJava, onJavaDl, type JavaInstall } from "@/lib/backend";
import { isTauri } from "@/lib/tauri";
import type { ThemeMode } from "@/styles/tokens";

/**
 * Settings — theme, global Java (detect + override), downloads, storage,
 * launcher updates. Everything persisted.
 */
export function SettingsPage() {
  const { mode, setMode } = useTheme();
  const s = useSettings();
  const push = useToasts((p) => p.push);
  const [javas, setJavas] = useState<JavaInstall[] | null>(null);
  const [scanning, setScanning] = useState(false);
  const [updating, setUpdating] = useState(false);
  const [dlMajor, setDlMajor] = useState<number | null>(null);
  const [dlPct, setDlPct] = useState(0);

  const themes: { id: ThemeMode; label: string; icon: typeof Moon }[] = [
    { id: "dark", label: "Dark", icon: Moon },
    { id: "light", label: "Light", icon: Sun },
    { id: "system", label: "System", icon: Monitor },
  ];

  const scan = async () => {
    setScanning(true);
    try {
      const list = await detectJava();
      setJavas(list);
      push({ kind: list.length ? "success" : "info", title: list.length ? `${list.length} Java runtime(s) found` : "No Java found", body: list.length ? undefined : "Install Temurin 17/21 or point to a java binary below." });
    } catch (e) {
      push({ kind: "error", title: "Java scan failed", body: String(e) });
    } finally {
      setScanning(false);
    }
  };

  const autoInstall = async (major: number) => {
    setDlMajor(major);
    setDlPct(0);
    const unlisten = await onJavaDl((p) => {
      if (p.major === major && p.total > 0) setDlPct(Math.round((p.downloaded / p.total) * 100));
    });
    try {
      const j = await ensureJava(major);
      const list = await detectJava();
      setJavas(list);
      push({ kind: "success", title: `Java ${major} ready`, body: j.path });
    } catch (e) {
      push({ kind: "error", title: `Java ${major} install failed`, body: e instanceof Error ? e.message : String(e) });
    } finally {
      unlisten();
      setDlMajor(null);
    }
  };

  const checkUpdates = async () => {
    setUpdating(true);
    try {
      if (!isTauri) {
        push({ kind: "info", title: "Updater needs the desktop app", body: "Run via `npm run tauri:dev` to check for launcher updates." });
        return;
      }
      const { check } = await import("@tauri-apps/plugin-updater");
      const update = await check();
      if (update) push({ kind: "success", title: `Update available: v${update.version}`, body: "Restart to install (dialog follows)." });
      else push({ kind: "success", title: "You're on the latest version" });
    } catch (e) {
      const msg = String(e);
      // Updater is compiled in but not configured (no pubkey/endpoint yet) —
      // say so plainly instead of surfacing plugin internals.
      if (/updater|signature|active/i.test(msg)) {
        push({ kind: "info", title: "Auto-update not configured", body: "This build has no update feed — grab the newest setup installer to update." });
      } else {
        push({ kind: "error", title: "Update check failed", body: msg });
      }
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl">
      <FadeUp>
        <h1 className="text-2xl font-bold tracking-tight">Settings</h1>
        <p className="mt-1 text-sm text-ink-muted">Global defaults — instances can override RAM, Java, JVM args.</p>
      </FadeUp>

      <FadeUp delay={0.05} className="mt-6 space-y-4">
        <Card>
          <h2 className="text-[15px] font-semibold">Appearance</h2>
          <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-3">
            {themes.map(({ id, label, icon: Icon }) => (
              <button
                key={id}
                onClick={() => setMode(id)}
                className={`flex h-16 flex-col items-center justify-center gap-1.5 rounded-md border text-[13px] font-medium transition active:scale-[0.98] ${mode === id ? "border-accent-500/50 bg-accent-500/10 text-ink shadow-1" : "bg-surface-50 text-ink-muted hover:text-ink"}`}
              >
                <Icon className="size-5" /> {label}
              </button>
            ))}
          </div>
        </Card>

        <Card>
          <h2 className="flex items-center gap-2 text-[15px] font-semibold"><Coffee className="size-4" /> Java runtime</h2>
          <p className="mt-0.5 text-[13px] text-ink-muted">Minecraft 1.20+ needs Java 17, 1.21+ needs Java 21.</p>
          <div className="mt-3 flex flex-wrap gap-2">
            <input className="input min-w-0 flex-1 font-mono text-xs" placeholder="Auto-detect (recommended — leave blank)" value={s.javaPath} onChange={(e) => s.set({ javaPath: e.target.value })} />
            <Button variant="secondary" size="sm" className="shrink-0" onClick={scan} disabled={scanning}>
              {scanning ? <Loader2 className="animate-spin" /> : <RefreshCw />} {scanning ? "Scanning…" : "Detect"}
            </Button>
          </div>
          <div className="mt-3 flex items-center gap-2 text-[13px]">
            <span className="shrink-0 text-ink-muted">No Java? Auto-install:</span>
            {[17, 21].map((m) => (
              <Button
                key={m}
                variant="secondary"
                size="sm"
                disabled={dlMajor !== null}
                onClick={() => autoInstall(m)}
              >
                {dlMajor === m ? <Loader2 className="animate-spin" /> : null}
                <span className="whitespace-nowrap">
                  {dlMajor === m ? `Java ${m}… ${dlPct}%` : `Temurin ${m}`}
                </span>
              </Button>
            ))}
          </div>
          {dlMajor !== null && (
            <div className="mt-2 h-1.5 overflow-hidden rounded-full bg-surface-300">
              <div
                className="h-full rounded-full bg-accent-500 transition-all duration-200"
                style={{ width: `${Math.max(dlPct, 2)}%` }}
              />
            </div>
          )}
          {javas && (
            <div className="mt-2 space-y-1.5">
              {javas.map((j) => (
                <button
                  key={j.path}
                  onClick={() => {
                    s.set({ javaPath: j.path });
                    push({ kind: "success", title: "Java selected", body: j.path });
                  }}
                  className="flex w-full items-center gap-2 rounded-md border bg-surface-50 px-3 py-2 text-left text-xs hover:border-accent-500/40"
                >
                  {s.javaPath === j.path && <Check className="size-3.5 text-accent-400" />}
                  <span className="min-w-0 flex-1 truncate font-mono">{j.path}</span>
                  <span className="shrink-0 text-ink-faint">{j.source}</span>
                </button>
              ))}
              {javas.length === 0 && <p className="text-[13px] text-ink-faint">No runtimes detected. Install <span className="font-mono">Temurin 21</span> from adoptium.net.</p>}
            </div>
          )}
        </Card>

        <Card>
          <h2 className="flex items-center gap-2 text-[15px] font-semibold"><Cpu className="size-4" /> Performance & downloads</h2>
          <div className="mt-3 space-y-3">
            <div>
              <div className="flex justify-between">
                <label className="label" htmlFor="gram">Default RAM for new instances</label>
                <span className="font-mono text-xs text-accent-400">{s.defaultRamMb} MB</span>
              </div>
              <input id="gram" type="range" min={1024} max={16384} step={256} value={s.defaultRamMb} onChange={(e) => s.set({ defaultRamMb: Number(e.target.value) })} className="w-full accent-[#1ea86a]" />
            </div>
            <div>
              <label className="label" htmlFor="conc">Parallel downloads: {s.concurrency}</label>
              <input id="conc" type="range" min={1} max={16} value={s.concurrency} onChange={(e) => s.set({ concurrency: Number(e.target.value) })} className="w-full accent-[#1ea86a]" />
            </div>
          </div>
        </Card>

        <Card>
          <h2 className="flex items-center gap-2 text-[15px] font-semibold"><FolderOpen className="size-4" /> Launcher</h2>
          <div className="mt-2 space-y-2 font-mono text-xs">
            <Row label="version" value={`v${__APP_VERSION__}`} />
            <Row label="backend" value={isTauri ? "Tauri v2 · disk store live" : "browser preview · localStorage store"} />
            <Row label="storage" value={s.storagePath || (isTauri ? "%APPDATA%/<app>/tyx/instances" : "localStorage (preview)")} />
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-3">
            <Button variant="secondary" size="sm" onClick={checkUpdates} disabled={updating}>
              {updating ? <Loader2 className="animate-spin" /> : <RefreshCw />} Check for updates
            </Button>
            <span className="text-xs text-ink-faint">Changes save automatically</span>
          </div>
        </Card>
      </FadeUp>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-4">
      <span className="font-sans text-ink-muted">{label}</span>
      <span className="truncate text-ink">{value}</span>
    </div>
  );
}
