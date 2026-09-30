import { useEffect, useMemo, useRef, useState } from "react";
import {
  ArrowDownToLine,
  Check,
  ChevronDown,
  Copy,
  Loader2,
  Maximize2,
  Minus,
  Minimize2,
  Pause,
  Play,
  Search,
  Square,
  Trash2,
  X,
} from "lucide-react";
import { TyxLogo } from "@/components/icons/brand";
import { onAssetsProgress, onGameLog, onJavaDl, type AssetsProgress } from "@/lib/backend";
import { initTheme } from "@/stores/app";
import { cn } from "@/lib/utils";
import "@/styles/globals.css";

interface LogLine {
  stream: "sys" | "stdout" | "stderr";
  line: string;
  at: number;
}

type Filter = "all" | "stdout" | "stderr" | "sys";

const FILTERS: { id: Filter; label: string }[] = [
  { id: "all", label: "All" },
  { id: "stdout", label: "Out" },
  { id: "stderr", label: "Errors" },
  { id: "sys", label: "System" },
];

function ts(at: number): string {
  const d = new Date(at);
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

/** Derive a headline status from the stream (running / exited / failed). */
function deriveStatus(lines: LogLine[]): { tone: "idle" | "run" | "ok" | "err"; label: string } {
  if (lines.length === 0) return { tone: "idle", label: "Waiting" };
  for (let i = lines.length - 1; i >= 0; i--) {
    const l = lines[i].line;
    if (/process exited:\s*exit code:\s*0\b/i.test(l) || /process exited:\s*code 0/i.test(l)) {
      return { tone: "ok", label: "Exited 0" };
    }
    const m = /process exited:\s*(.+)/i.exec(l);
    if (m) return { tone: "err", label: `Exited · ${m[1].trim().slice(0, 24)}` };
    if (/spawn failed|cannot run Java|cannot build launch|game download failed|Error: Could not create|fatal exception/i.test(l)) {
      return { tone: "err", label: "Failed" };
    }
  }
  return { tone: "run", label: "Running" };
}

const STATUS_STYLE: Record<string, string> = {
  idle: "bg-surface-300 text-ink-muted",
  run: "bg-accent-500/15 text-accent-400",
  ok: "bg-accent-500/15 text-accent-400",
  err: "bg-red-500/15 text-red-400",
};

type McLevel = "INFO" | "WARN" | "ERROR" | "DEBUG" | "TRACE" | "FATAL";

interface RichLine {
  key: string;
  stream: LogLine["stream"];
  at: number;
  kind: "tyx" | "mc" | "stack" | "raw";
  level?: McLevel;
  thread?: string;
  text: string;
}

const LEVEL_CHIP: Record<string, string> = {
  ERROR: "bg-red-500/15 text-red-400",
  FATAL: "bg-red-500/20 text-red-300 font-bold",
  WARN: "bg-amber-500/15 text-amber-400",
  INFO: "bg-surface-300/80 text-ink-muted",
  DEBUG: "bg-transparent text-ink-faint",
  TRACE: "bg-transparent text-ink-faint",
};

/**
 * Collapse multi-line log4j XML walls into single readable rows:
 * `<log4j:Event level …><Message>CDATA</Message></Event>` →
 * `[ERROR] message`. Tag wrappers are dropped, stack frames dimmed.
 */
function prettifyLines(lines: LogLine[]): RichLine[] {
  const out: RichLine[] = [];
  let level: McLevel | undefined;
  let thread: string | undefined;
  let inStack = false;
  lines.forEach((l, i) => {
    const key = `${l.at}-${i}`;
    const s = l.line;
    if (/^\[tyx\]|^\[java\]|^\[dl\]|^\[LWJGL\]|^\[Auth\]|^\[Client\]/.test(s)) {
      out.push({ key, stream: l.stream, at: l.at, kind: "tyx", text: s });
      return;
    }
    const ev = /<log4j:Event[^>]*level="([A-Z]+)"[^>]*thread="([^"]*)"/.exec(s);
    if (ev) {
      level = ev[1] as McLevel;
      thread = ev[2];
      return; // drop tag line
    }
    if (/<\/log4j:Event/.test(s)) {
      level = undefined;
      thread = undefined;
      return; // drop tag line
    }
    const msg = /<log4j:Message><!\[CDATA\[([\s\S]*)\]\]><\/log4j:Message>/.exec(s);
    if (msg) {
      const text = msg[1].replace(/\\n/g, "\n").trim();
      if (text) out.push({ key, stream: l.stream, at: l.at, kind: "mc", level, thread, text });
      return;
    }
    if (/<log4j:Throwable/.test(s)) {
      inStack = true;
      return; // drop wrapper
    }
    if (/\]\]>/.test(s)) {
      inStack = false;
      return; // drop wrapper
    }
    if (/^\s*<\/?log4j:/.test(s)) return; // drop stray tags
    if (inStack || /^\s*at\s|Caused by:|^\s*\.\.\. \d+ more|^#\s/.test(s)) {
      if (s.trim()) out.push({ key, stream: l.stream, at: l.at, kind: "stack", level, text: s });
      return;
    }
    out.push({ key, stream: l.stream, at: l.at, kind: "raw", text: s });
  });
  return out;
}

/**
 * Standalone game-console window (`index.html?console`) — the "beautiful
 * log" view. Glass header with live status pill, segmented stream filters,
 * text search, timestamped color-railed lines, download progress card,
 * footer stats + jump-to-bottom. Own window + JS context, subscribes to
 * `tyx://game-log` directly.
 */
function fmtMB(bytes: number): string {
  return `${(bytes / 1_048_576).toFixed(1)} MB`;
}

function fmtSpeed(bps: number): string {
  if (!bps || bps <= 0) return "…";
  const mb = bps / 1_048_576;
  return mb >= 10 ? `${mb.toFixed(0)} MB/s` : `${mb.toFixed(1)} MB/s`;
}

function fmtETA(secs: number | undefined): string {
  if (secs == null || !Number.isFinite(secs) || secs >= Number.MAX_SAFE_INTEGER / 2 || secs > 99 * 3600) return "…";
  if (secs <= 0) return "00:00";
  const s = Math.round(secs);
  return `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;
}

function fmtElapsed(ms: number): string {
  const s = Math.max(0, Math.floor(ms / 1000));
  return `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;
}

const STAGE_LABEL: Record<string, string> = {
  manifest: "Contacting Mojang…",
  loader: "Installing mod loader…",
  client: "Downloading client…",
  libraries: "Downloading libraries…",
  natives: "Downloading natives…",
  config: "Downloading configs…",
  assets: "Downloading assets…",
};

export function ConsoleWindow() {
  const [lines, setLines] = useState<LogLine[]>([]);
  const [filter, setFilter] = useState<Filter>("all");
  const [query, setQuery] = useState("");
  const [stick, setStick] = useState(true);
  const [assets, setAssets] = useState<AssetsProgress | null>(null);
  const [javaDl, setJavaDl] = useState<{ major: number; downloaded: number; total: number } | null>(null);
  const [copied, setCopied] = useState(false);
  const [now, setNow] = useState(() => Date.now());
  const bottom = useRef<HTMLDivElement>(null);
  const body = useRef<HTMLDivElement>(null);
  const dlStart = useRef<number | null>(null);
  const samples = useRef<{ t: number; b: number }[]>([]);

  useEffect(() => {
    initTheme();
    let unlisten: (() => void) | undefined;
    let unlistenAssets: (() => void) | undefined;
    let unlistenJava: (() => void) | undefined;
    onGameLog(({ stream, line }) => {
      setLines((l) => [...l.slice(-2000), { stream: stream as LogLine["stream"], line, at: Date.now() }]);
    }).then((u) => (unlisten = u));
    onAssetsProgress((p) => {
      if (p.stage === "done") {
        setAssets(null);
        return;
      }
      setAssets(p);
      if (dlStart.current == null) dlStart.current = Date.now();
      const t = Date.now();
      samples.current.push({ t, b: p.done_bytes });
      // Keep a 10 s window for the fallback speed calc.
      while (samples.current.length > 2 && t - samples.current[0].t > 10_000) samples.current.shift();
      if (samples.current.length > 60) samples.current.shift();
    }).then((u) => (unlistenAssets = u));
    onJavaDl((p) => setJavaDl(p.downloaded >= p.total && p.total > 0 ? null : p)).then((u) => (unlistenJava = u));
    return () => {
      unlisten?.();
      unlistenAssets?.();
      unlistenJava?.();
    };
  }, []);

  // Tick for elapsed + fallback ETA while downloads run.
  useEffect(() => {
    if (!assets && !javaDl) return;
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, [!!assets, !!javaDl]);

  useEffect(() => {
    if (stick) bottom.current?.scrollIntoView({ behavior: "auto" });
  }, [lines.length, stick]);

  const q = query.trim().toLowerCase();
  const rich = useMemo(() => prettifyLines(lines), [lines]);
  const shown = useMemo(
    () =>
      rich.filter(
        (l) =>
          (filter === "all" || l.stream === filter) &&
          (!q ||
            l.text.toLowerCase().includes(q) ||
            (l.level ?? "").toLowerCase().includes(q) ||
            (l.thread ?? "").toLowerCase().includes(q)),
      ),
    [rich, filter, q],
  );
  const errors = lines.filter((l) => l.stream === "stderr").length;
  const status = deriveStatus(lines);
  // Games running? Derived from the stream itself (this window has its own
  // store copy): pid lines add, terminal lines remove — a count, so one
  // game exiting doesn't hide the others still running.
  const runningCount = useMemo(() => {
    let n = 0;
    for (const l of lines) {
      if (/\[tyx\] pid \d+ instance \S+/.test(l.line)) n++;
      else if (/process exited|spawn failed|cannot run Java|launch-aborted|stopped by user/i.test(l.line)) n = Math.max(0, n - 1);
    }
    return n;
  }, [lines]);
  const gameRunning = runningCount > 0;
  const [stopping, setStopping] = useState(false);
  const [maximized, setMaximized] = useState(false);

  // Track maximize state (icon swap), same pattern as the main TitleBar.
  useEffect(() => {
    let unlisten: (() => void) | undefined;
    import("@tauri-apps/api/window")
      .then(({ getCurrentWindow }) => {
        const win = getCurrentWindow();
        win.isMaximized().then(setMaximized).catch(() => {});
        win
          .onResized(() => {
            win.isMaximized().then(setMaximized).catch(() => {});
          })
          .then((u) => (unlisten = u))
          .catch(() => {});
      })
      .catch(() => {});
    return () => unlisten?.();
  }, []);

  const stopGameUi = async () => {
    setStopping(true);
    try {
      const { stopGame } = await import("@/lib/backend");
      // No instance selection in this global window — Stop halts every
      // running game (each reports its own exit line below).
      const stopped = await stopGame(null);
      setLines((l) => [...l.slice(-2000), { stream: "sys", line: `[tyx] stop requested: ${stopped}`, at: Date.now() }]);
    } catch (e) {
      // No toast system in this window — say it in the log itself.
      setLines((l) => [...l.slice(-2000), { stream: "sys", line: `[tyx] stop failed: ${e instanceof Error ? e.message : String(e)}`, at: Date.now() }]);
    } finally {
      setStopping(false);
    }
  };
  const hasTotal = !!assets && assets.total_bytes > 1;
  const pct = hasTotal ? Math.min(100, (assets!.done_bytes / Math.max(1, assets!.total_bytes)) * 100) : 0;
  // Speed: prefer backend rolling speed, fall back to local 3 s window.
  const fallbackBps = useMemo(() => {
    const s = samples.current;
    if (!assets || s.length < 2) return 0;
    const head = s[s.length - 1];
    const tail = s.find((x) => head.t - x.t <= 3000) ?? s[0];
    const dt = (head.t - tail.t) / 1000;
    if (dt < 1) return 0;
    return Math.max(0, (head.b - tail.b) / dt);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [assets?.done_bytes, now]);
  const speedBps = assets?.speed_bps && assets.speed_bps > 0 ? assets.speed_bps : fallbackBps;
  const etaSecs = assets?.eta_secs != null && assets.eta_secs < Number.MAX_SAFE_INTEGER / 2 && assets.eta_secs <= 99 * 3600
    ? assets.eta_secs
    : speedBps > 0 && hasTotal
      ? Math.max(0, (assets!.total_bytes - assets!.done_bytes) / speedBps)
      : undefined;
  const elapsed = dlStart.current != null && assets ? now - dlStart.current : 0;
  const javaPct = javaDl && javaDl.total > 0 ? Math.min(100, (javaDl.downloaded / javaDl.total) * 100) : 0;

  const close = async () => {
    try {
      const { getCurrentWindow } = await import("@tauri-apps/api/window");
      await getCurrentWindow().close();
    } catch {
      window.close();
    }
  };

  const minimize = async () => {
    try {
      const { getCurrentWindow } = await import("@tauri-apps/api/window");
      await getCurrentWindow().minimize();
    } catch {
      /* browser preview: nothing to minimize */
    }
  };

  const toggleMaximize = async () => {
    try {
      const { getCurrentWindow } = await import("@tauri-apps/api/window");
      const win = getCurrentWindow();
      await win.toggleMaximize();
      setMaximized(await win.isMaximized().catch(() => false));
    } catch {
      /* browser preview: nothing to maximize */
    }
  };

  const copyAll = () => {
    const text = shown.map((l) => `[${ts(l.at)}] [${l.stream}]${l.level ? ` [${l.level}]` : ""} ${l.text}`).join("\n");
    navigator.clipboard
      .writeText(text)
      .then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1500);
      })
      .catch(() => {});
  };

  const jumpDown = () => {
    setStick(true);
    bottom.current?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <div className="flex h-screen w-screen flex-col overflow-hidden bg-surface-50">
      {/* Accent hairline */}
      <div className="hero-gradient-bar h-0.5 w-full shrink-0 opacity-80" aria-hidden />

      {/* Header — frameless window chrome: drag anywhere except controls */}
      <header
        data-tauri-drag-region="deep"
        className="flex shrink-0 select-none flex-wrap items-center gap-2 border-b border-white/10 bg-surface-100/60 px-3 py-2 backdrop-blur-xl"
      >
        <TyxLogo className="size-5 rounded" />
        <span className="text-[13px] font-bold tracking-tight">Game console</span>
        <span className={cn("flex items-center gap-1.5 rounded-full px-2 py-0.5 text-[11px] font-bold", STATUS_STYLE[status.tone])}>
          <span className={cn("size-1.5 rounded-full bg-current", status.tone === "run" && "animate-pulse")} />
          {status.label}
        </span>
        <span className="font-mono text-[11px] text-ink-faint">
          {shown.length}/{lines.length} lines
          {errors > 0 && <span className="font-semibold text-red-400"> · {errors} errors</span>}
        </span>

        {/* Stream filter pills */}
        <div className="mx-1 flex gap-0.5 rounded-lg border border-white/10 bg-surface-50/60 p-0.5" role="tablist" aria-label="Stream filter">
          {FILTERS.map((f) => (
            <button
              key={f.id}
              role="tab"
              aria-selected={filter === f.id}
              onClick={() => setFilter(f.id)}
              className={cn(
                "rounded-md px-2.5 py-1 text-xs font-semibold transition-all",
                filter === f.id ? "bg-surface-300 text-ink shadow-1" : "text-ink-faint hover:text-ink",
              )}
            >
              {f.label}
              {f.id === "stderr" && errors > 0 && (
                <span className="ml-1 rounded-full bg-red-500/20 px-1 font-mono text-[10px] text-red-400">{errors}</span>
              )}
            </button>
          ))}
        </div>

        {/* Text search */}
        <div className="relative hidden min-w-[140px] flex-1 sm:block">
          <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-ink-faint" />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Filter lines…"
            aria-label="Filter log lines"
            className="input h-8 rounded-lg pl-8 font-mono text-xs"
          />
        </div>

        <div className="flex-1 sm:hidden" />

        {/* Window controls — explicitly outside the drag region */}
        <div
          className="flex items-center gap-1"
          data-tauri-drag-region="false"
          onPointerDown={(e) => e.stopPropagation()}
          onDoubleClick={(e) => e.stopPropagation()}
        >
        {gameRunning && (
          <button
            onClick={stopGameUi}
            disabled={stopping}
            title="Stop all running games"
            className="flex items-center gap-1.5 rounded-lg border border-red-500/40 bg-red-500/10 px-2.5 py-1.5 text-xs font-bold text-red-400 transition hover:bg-red-500/20 disabled:opacity-60"
          >
            {stopping ? <Loader2 className="size-4 animate-spin" /> : <Square className="size-4 fill-current" />}
            Stop all{runningCount > 1 ? ` (${runningCount})` : ""}
          </button>
        )}
        <button
          onClick={() => setStick((s) => !s)}
          title={stick ? "Pause auto-scroll" : "Resume auto-scroll"}
          className={cn(
            "rounded-lg border border-white/10 p-1.5 transition",
            stick ? "bg-accent-500/15 text-accent-400" : "text-ink-faint hover:bg-surface-200 hover:text-ink",
          )}
        >
          {stick ? <Pause className="size-4" /> : <Play className="size-4" />}
        </button>
        <button
          onClick={copyAll}
          title="Copy visible lines"
          className="rounded-lg border border-white/10 p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink"
        >
          {copied ? <Check className="size-4 text-accent-400" /> : <Copy className="size-4" />}
        </button>
        <button
          onClick={() => setLines([])}
          title="Clear"
          className="rounded-lg border border-white/10 p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink"
        >
          <Trash2 className="size-4" />
        </button>
        <button
          onClick={minimize}
          title="Minimize console"
          aria-label="Minimize console"
          className="rounded-lg border border-white/10 p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink"
        >
          <Minus className="size-4" />
        </button>
        <button
          onClick={toggleMaximize}
          title={maximized ? "Restore console" : "Maximize console"}
          aria-label={maximized ? "Restore console" : "Maximize console"}
          className="rounded-lg border border-white/10 p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink"
        >
          {maximized ? <Minimize2 className="size-4" /> : <Maximize2 className="size-4" />}
        </button>
        <button
          onClick={close}
          title="Close console"
          className="rounded-lg border border-white/10 p-1.5 text-ink-faint transition hover:border-red-500/50 hover:bg-red-500 hover:text-white"
        >
          <X className="size-4" />
        </button>
        </div>
      </header>

      {/* Body */}
      <div ref={body} className="min-h-0 flex-1 overflow-y-auto px-3 py-2 font-mono text-xs leading-relaxed">
        {javaDl && (
          <div className="panel sticky top-0 z-10 mb-2 p-3">
            <div className="flex items-center justify-between font-sans text-[12px]">
              <span className="font-semibold">Downloading Java {javaDl.major} runtime · {javaPct.toFixed(0)}%</span>
              <span className="font-mono font-bold text-accent-400">
                {fmtMB(javaDl.downloaded)}/{javaDl.total > 0 ? fmtMB(javaDl.total) : "…"}
              </span>
            </div>
            <div className="mt-1.5 h-1.5 overflow-hidden rounded-full bg-surface-300">
              <div
                className="hero-gradient-bar h-full rounded-full transition-all duration-300"
                style={{ width: `${Math.max(javaPct, 2)}%` }}
              />
            </div>
            <p className="mt-1 font-sans text-[11px] text-ink-faint">One-time install — game download starts next.</p>
          </div>
        )}
        {assets && (
          <div className="panel sticky top-0 z-10 mb-2 p-3">
            <div className="flex items-center justify-between gap-2 font-sans text-[12px]">
              <span className="truncate font-semibold">
                {STAGE_LABEL[assets.stage] ?? `Downloading ${assets.stage}…`} · {assets.done_files}/{assets.total_files} files
              </span>
              <span className="shrink-0 font-mono font-bold text-accent-400">
                {hasTotal ? `${Math.round(pct)}%` : "…"}
              </span>
            </div>
            {hasTotal ? (
              <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-surface-300">
                <div
                  className="hero-gradient-bar h-full rounded-full transition-all duration-300"
                  style={{ width: `${Math.max(pct, 2)}%` }}
                />
              </div>
            ) : (
              <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-surface-300">
                <div className="hero-gradient-bar h-full w-1/3 animate-pulse rounded-full" />
              </div>
            )}
            <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-0.5 font-sans text-[11px] text-ink-faint">
              {hasTotal ? (
                <span className="font-mono font-semibold text-ink">
                  {fmtMB(assets.done_bytes)}/{fmtMB(assets.total_bytes)}
                </span>
              ) : (
                <span>Contacting Mojang, counting files…</span>
              )}
              <span className="font-mono">⚡ {fmtSpeed(speedBps)}</span>
              <span className="font-mono">⏳ ETA {fmtETA(etaSecs)}</span>
              {elapsed > 0 && <span className="font-mono">⏱ {fmtElapsed(elapsed)} elapsed</span>}
            </div>
            <p className="mt-1 truncate text-[11px] text-ink-faint" title={assets.current}>
              {assets.current || "working…"}
            </p>
          </div>
        )}

        {shown.length === 0 && (
          <div className="flex h-full flex-col items-center justify-center gap-3 text-ink-faint">
            <div className="panel flex size-14 items-center justify-center">
              <ArrowDownToLine className="size-6 text-accent-400" />
            </div>
            <p className="font-sans text-sm">
              {lines.length === 0 ? "Waiting for game output… press Play in the launcher." : "No lines match this filter."}
            </p>
          </div>
        )}

        {shown.map((l) => {
          const rail =
            l.stream === "stderr" || l.level === "ERROR" || l.level === "FATAL"
              ? "bg-red-400"
              : l.level === "WARN"
                ? "bg-amber-400"
                : l.stream === "sys" || l.kind === "tyx"
                  ? "bg-accent-400"
                  : "bg-surface-400";
          const hl = l.kind === "tyx" && /starting real client|pid \d+|auto-joining|game files ready|downloading game files/.test(l.text);
          const showThread = (l.level === "WARN" || l.level === "ERROR" || l.level === "FATAL") && l.thread;
          return (
            <div
              key={l.key}
              className={cn(
                "group flex gap-2.5 rounded px-2 py-[3px] transition-colors hover:bg-surface-200/50",
                hl && "bg-accent-500/10 hover:bg-accent-500/15",
              )}
            >
              <span className="hidden select-none font-mono text-[11px] leading-[1.7] text-ink-faint/70 sm:inline">
                {ts(l.at)}
              </span>
              <span className={cn("mt-[3px] h-4 w-[3px] shrink-0 rounded-full", rail)} aria-hidden />
              <div className="min-w-0 flex-1">
                {l.kind === "mc" && l.level ? (
                  <p className="flex flex-wrap items-baseline gap-x-2">
                    <span className={cn("shrink-0 rounded px-1 py-px font-mono text-[10px] font-bold tracking-wide", LEVEL_CHIP[l.level] ?? LEVEL_CHIP.INFO)}>
                      {l.level}
                    </span>
                    {showThread && (
                      <span className="shrink-0 font-mono text-[10px] text-ink-faint">{l.thread}</span>
                    )}
                    <span
                      className={cn(
                        "min-w-0 flex-1 whitespace-pre-wrap break-all",
                        l.level === "ERROR" || l.level === "FATAL" ? "text-red-300" : "text-ink-muted",
                      )}
                    >
                      {l.text}
                    </span>
                  </p>
                ) : (
                  <p
                    className={cn(
                      "whitespace-pre-wrap break-all",
                      l.kind === "stack"
                        ? "pl-1 text-[11px] text-ink-faint"
                        : l.stream === "stderr"
                          ? "text-red-400"
                          : l.kind === "tyx"
                            ? "text-accent-300"
                            : "text-ink-muted",
                    )}
                  >
                    {l.text}
                  </p>
                )}
              </div>
            </div>
          );
        })}
        <div ref={bottom} />
      </div>

      {/* Footer */}
      <footer className="flex shrink-0 items-center gap-2 border-t border-white/10 bg-surface-100/60 px-3 py-1.5 font-mono text-[11px] text-ink-faint backdrop-blur-xl">
        <ChevronDown className={cn("size-3.5", stick ? "text-accent-400" : "")} aria-hidden />
        <span>{stick ? "following tail" : "paused — scroll to explore"}</span>
        <span className="ml-auto hidden sm:inline">
          {lines.length > 0 && (
            <>session {ts(lines[0].at)} → {ts(lines[lines.length - 1].at)}</>
          )}
        </span>
        {!stick && lines.length > 0 && (
          <button
            onClick={jumpDown}
            className="flex items-center gap-1 rounded-md border border-accent-500/40 bg-accent-500/10 px-2 py-0.5 font-sans text-[11px] font-semibold text-accent-400 transition hover:bg-accent-500/20"
          >
            <ArrowDownToLine className="size-3" /> Latest
          </button>
        )}
      </footer>
    </div>
  );
}
