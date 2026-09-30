import { useEffect, useMemo, useState } from "react";
import { Globe, Loader2, Play, Plus, RefreshCw, Trash2 } from "lucide-react";
import { FadeUp, Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Dropdown } from "@/components/ui/Dropdown";
import { ServerIcon } from "@/components/icons/brand";
import { useInstances } from "@/stores/instances";
import { useServers, type GameServer } from "@/stores/servers";
import { useToasts } from "@/stores/app";
import { playInstance } from "@/lib/play";
import { dropPingCache, flagEmoji, pingServer, pingTone, type ServerStatus } from "@/lib/server-status";
import { timeAgo, cn } from "@/lib/utils";

/**
 * Home → Servers: saved + recently-joined game servers with instant
 * direct join (Modrinth-style). Joining launches the chosen instance with
 * `--server <host> --port <port>` — straight into the server, no menu.
 * Recents are captured automatically from game logs.
 */
export function ServersSection() {
  const instances = useInstances((s) => s.instances);
  const saved = useServers((s) => s.saved);
  const recent = useServers((s) => s.recent);

  const defaultInstanceId = useMemo(() => {
    const sorted = [...instances].sort((a, b) => (b.lastPlayed ?? 0) - (a.lastPlayed ?? 0));
    return sorted[0]?.id ?? "";
  }, [instances]);

  if (instances.length === 0) return null;

  return (
    <FadeUp delay={0.1} className="mt-6">
      <div className="flex items-end justify-between">
        <div>
          <h2 className="flex items-center gap-2 text-lg font-bold tracking-tight">
            <Globe className="size-5 text-accent-400" /> Servers
          </h2>
          <p className="mt-0.5 text-[13px] text-ink-muted">
            Join straight in — no main menu. Recents appear automatically.
          </p>
        </div>
      </div>

      <Card className="mt-3">
        <AddServerForm />
        {saved.length === 0 && recent.length === 0 ? (
          <p className="mt-3 rounded-lg border border-dashed border-white/10 px-3 py-4 text-center text-[13px] text-ink-faint">
            No servers yet — join one in-game and it lands here, or save one above.
          </p>
        ) : (
          <div className="mt-3 space-y-1.5">
            {saved.map((s) => (
              <ServerRow key={s.id} server={s} pinned defaultInstanceId={defaultInstanceId} />
            ))}
            {recent
              .filter((r) => !saved.some((s) => s.id === r.id))
              .map((s) => (
                <ServerRow key={s.id} server={s} defaultInstanceId={defaultInstanceId} />
              ))}
          </div>
        )}
      </Card>
    </FadeUp>
  );
}

function AddServerForm() {
  const addSaved = useServers((s) => s.addSaved);
  const push = useToasts((s) => s.push);
  const [name, setName] = useState("");
  const [address, setAddress] = useState("");
  const submit = () => {
    try {
      const s = addSaved(name, address);
      push({ kind: "success", title: `Saved ${s.name}`, body: s.id });
      setName("");
      setAddress("");
    } catch (e) {
      push({ kind: "error", title: "Can't save server", body: e instanceof Error ? e.message : String(e) });
    }
  };
  return (
    <div className="flex flex-wrap gap-2">
      <input
        className="input h-9 min-w-[140px] flex-1 text-[13px]"
          placeholder="Server name"
        value={name}
        maxLength={40}
        onChange={(e) => setName(e.target.value)}
        aria-label="Server name"
      />
      <input
        className="input h-9 min-w-[180px] flex-[2] font-mono text-[13px]"
        placeholder="Address — play.example.net or host:25565"
        value={address}
        onChange={(e) => setAddress(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && submit()}
        aria-label="Server address"
      />
      <Button variant="secondary" size="sm" className="h-9" onClick={submit}>
        <Plus /> Save
      </Button>
    </div>
  );
}

function ServerRow({
  server,
  pinned,
  defaultInstanceId,
}: {
  server: GameServer;
  pinned?: boolean;
  defaultInstanceId: string;
}) {
  const instances = useInstances((s) => s.instances);
  const removeSaved = useServers((s) => s.removeSaved);
  const push = useToasts((s) => s.push);
  const [instanceId, setInstanceId] = useState(defaultInstanceId);
  const [joining, setJoining] = useState(false);
  const [status, setStatus] = useState<ServerStatus | null>(null);
  const [pinging, setPinging] = useState(false);
  // Instances load async — sync the default once they arrive.
  useEffect(() => {
    if (!instanceId && defaultInstanceId) setInstanceId(defaultInstanceId);
  }, [defaultInstanceId, instanceId]);
  const inst = instances.find((i) => i.id === (instanceId || defaultInstanceId));

  const refresh = async () => {
    setPinging(true);
    try {
      dropPingCache(server.host, server.port);
      setStatus(await pingServer(server.host, server.port));
    } finally {
      setPinging(false);
    }
  };

  useEffect(() => {
    let live = true;
    setStatus(null);
    pingServer(server.host, server.port)
      .then((s) => live && setStatus(s))
      .catch(() => {});
    return () => {
      live = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [server.id]);

  const join = async () => {
    const target = instanceId || defaultInstanceId;
    if (!target) {
      push({ kind: "error", title: "No instance", body: "Create an instance first." });
      return;
    }
    setJoining(true);
    try {
      await playInstance(target, { server: server.host, port: server.port });
      push({ kind: "success", title: `Joining ${server.host}`, body: `via ${inst?.name ?? "instance"} — straight in.` });
    } catch (e) {
      push({ kind: "error", title: "Join failed", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setJoining(false);
    }
  };

  return (
    <div className="group flex flex-wrap items-center gap-2.5 rounded-lg border border-white/10 bg-surface-50/60 px-3 py-2 transition-colors hover:border-accent-500/30">
      <span className="flex size-9 shrink-0 items-center justify-center overflow-hidden rounded-lg bg-accent-500/15">
        {status?.favicon ? (
          <img src={status.favicon} alt="" className="size-9 object-cover" loading="lazy" />
        ) : (
          <ServerIcon className="size-5 text-accent-400" />
        )}
      </span>
      <div className="min-w-0 flex-1 basis-40">
        <p className="flex items-center gap-1.5 truncate text-[13px] font-semibold">
          <span
            title={status ? (status.online ? `Online · ${status.pingMs}ms` : "Offline / unreachable") : "Pinging…"}
            className={cn(
              "size-2 shrink-0 rounded-full",
              !status ? "animate-pulse bg-ink-faint" : status.online ? "bg-accent-400 shadow-glow" : "bg-red-400/70",
            )}
          />
          <span className="truncate">{server.name}</span>
          {pinned ? (
            <span className="rounded-full bg-accent-500/15 px-1.5 py-px text-[10px] font-bold uppercase tracking-wider text-accent-400">saved</span>
          ) : (
            <span className="rounded-full bg-surface-300/70 px-1.5 py-px text-[10px] font-bold uppercase tracking-wider text-ink-faint">recent</span>
          )}
        </p>
        <p className="truncate font-mono text-[11px] text-ink-faint">
          {server.host}:{server.port}
          {server.lastJoined > 0 && <span> · {timeAgo(server.lastJoined)}</span>}
        </p>
        {status && (
          <p className="mt-0.5 flex flex-wrap items-center gap-x-2 gap-y-0.5 truncate text-[11px] text-ink-muted">
            {status.online ? (
              <>
                <span className={cn("font-mono font-semibold", pingTone(status.pingMs, true))}>
                  {status.pingMs}ms
                </span>
                <span title={status.ip || undefined}>
                  {flagEmoji(status.countryCode)} {status.city || status.country || "Unknown"}
                </span>
                <span className="font-mono">
                  {status.playersOnline}/{status.playersMax}
                </span>
                {status.version && <span className="font-mono text-ink-faint">{status.version}</span>}
              </>
            ) : (
              <span className="text-ink-faint">Offline — will retry on join</span>
            )}
          </p>
        )}
        {status?.online && status.motd && (
          <p className="truncate text-[11px] italic text-ink-faint" title={status.motd}>
            {status.motd.slice(0, 80)}
          </p>
        )}
      </div>
      <Dropdown
        ariaLabel={`Instance for ${server.name}`}
        value={instanceId || defaultInstanceId}
        onChange={setInstanceId}
        options={instances.map((i) => ({ value: i.id, label: i.name, hint: i.version }))}
        className={cn("h-8 w-auto min-w-[150px] text-xs")}
      />
      <Button variant="primary" size="sm" disabled={joining} onClick={join}>
        {joining ? <Loader2 className="animate-spin" /> : <Play />} {joining ? "Joining…" : "Join"}
      </Button>
      <button
        onClick={refresh}
        disabled={pinging}
        title="Re-ping server"
        aria-label={`Re-ping ${server.name}`}
        className="rounded p-1.5 text-ink-faint transition hover:bg-surface-200 hover:text-ink disabled:opacity-50"
      >
        <RefreshCw className={cn("size-3.5", pinging && "animate-spin")} />
      </button>
      {pinned && (
        <button
          onClick={() => {
            removeSaved(server.id);
            push({ kind: "info", title: `Removed ${server.name}` });
          }}
          aria-label={`Remove ${server.name}`}
          className="rounded p-1 text-ink-faint opacity-0 transition hover:bg-red-500/10 hover:text-red-400 focus-visible:opacity-100 group-hover:opacity-100"
        >
          <Trash2 className="size-3.5" />
        </button>
      )}
    </div>
  );
}
