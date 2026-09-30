import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface GameServer {
  id: string; // host:port (lowercased)
  name: string;
  host: string;
  port: number;
  lastJoined: number;
}

interface ServerState {
  /** Pinned by the user ( survives, editable). */
  saved: GameServer[];
  /** Auto-captured from game logs (max 8, most-recent first). */
  recent: GameServer[];
  addSaved: (name: string, address: string) => GameServer;
  removeSaved: (id: string) => void;
  recordRecent: (host: string, port: number) => void;
}

const MAX_RECENT = 8;

/** `play.example.net`, `play.example.net:25565`, spaces tolerated. */
export function parseServerAddress(address: string): { host: string; port: number } {
  const clean = address.trim().toLowerCase();
  if (!clean) throw new Error("Enter a server address.");
  const m = /^([a-z0-9.\-_]+)(?::(\d{1,5}))?$/.exec(clean);
  if (!m) throw new Error("Address looks wrong — use host or host:port.");
  const port = m[2] ? Number(m[2]) : 25565;
  if (port < 1 || port > 65535) throw new Error("Port must be 1–65535.");
  return { host: m[1], port };
}

/**
 * Pull `Connecting to <host>, <port>` out of a raw game-log line.
 * Handles both plain lines and log4j XML blobs (`…CDATA[Connecting to …]]`).
 */
export function extractServer(line: string): { host: string; port: number } | null {
  const m = /Connecting to\s+([A-Za-z0-9.\-_]+)\s*,\s*(\d{1,5})/.exec(line);
  if (!m) return null;
  const port = Number(m[2]);
  if (port < 1 || port > 65535) return null;
  return { host: m[1].toLowerCase(), port };
}

export const useServers = create<ServerState>()(
  persist(
    (set, get) => ({
      saved: [],
      recent: [],
      addSaved: (name, address) => {
        const cleanName = name.trim().slice(0, 40) || "My server";
        const { host, port } = parseServerAddress(address);
        const id = `${host}:${port}`;
        const entry: GameServer = { id, name: cleanName, host, port, lastJoined: 0 };
        set((s) => ({
          saved: [entry, ...s.saved.filter((x) => x.id !== id)].slice(0, 32),
        }));
        return entry;
      },
      removeSaved: (id) => set((s) => ({ saved: s.saved.filter((x) => x.id !== id) })),
      recordRecent: (host, port) => {
        const id = `${host}:${port}`;
        const prev = get();
        const existing = prev.recent.find((x) => x.id === id) ?? prev.saved.find((x) => x.id === id);
        const entry: GameServer = {
          id,
          name: existing?.name ?? host,
          host,
          port,
          lastJoined: Date.now(),
        };
        // Also refresh the saved row's timestamp so sorts stay truthful.
        set((s) => ({
          recent: [entry, ...s.recent.filter((x) => x.id !== id)].slice(0, MAX_RECENT),
          saved: s.saved.map((x) => (x.id === id ? { ...x, lastJoined: entry.lastJoined } : x)),
        }));
      },
    }),
    { name: "tyx:servers" },
  ),
);
