/**
 * Live server status for the list (Feather-style: ping + place + players).
 * Rust speaks ServerListPing over TCP (no MC copy needed) and geolocates
 * the resolved IP. Results cache 60s per host:port so rows don't hammer.
 */
import { invokeOr, isTauri } from "./tauri";

export interface ServerStatus {
  online: boolean;
  motd: string;
  version: string;
  playersOnline: number;
  playersMax: number;
  pingMs: number;
  ip: string;
  country: string;
  countryCode: string;
  city: string;
  favicon: string;
}

const OFFLINE: ServerStatus = {
  online: false,
  motd: "",
  version: "",
  playersOnline: 0,
  playersMax: 0,
  pingMs: 0,
  ip: "",
  country: "",
  countryCode: "",
  city: "",
  favicon: "",
};

const cache = new Map<string, { at: number; data: ServerStatus }>();
const TTL = 60_000;

export async function pingServer(host: string, port: number): Promise<ServerStatus> {
  const key = `${host.toLowerCase()}:${port}`;
  const hit = cache.get(key);
  if (hit && Date.now() - hit.at < TTL) return hit.data;
  if (!isTauri) return OFFLINE; // browser can't open raw TCP
  const data = await invokeOr<ServerStatus>("server_ping", { host, port }, OFFLINE);
  cache.set(key, { at: Date.now(), data });
  return data;
}

export function dropPingCache(host: string, port: number) {
  cache.delete(`${host.toLowerCase()}:${port}`);
}

/** 🇩🇪 from "DE" (regional indicators). Empty code → globe. */
export function flagEmoji(countryCode: string): string {
  const cc = countryCode.trim().toUpperCase();
  if (!/^[A-Z]{2}$/.test(cc)) return "🌐";
  return String.fromCodePoint(...[...cc].map((c) => 0x1f1e6 + c.charCodeAt(0) - 65));
}

/** Ping text color class. */
export function pingTone(ms: number, online: boolean): string {
  if (!online) return "text-ink-faint";
  if (ms < 80) return "text-accent-400";
  if (ms < 200) return "text-amber-400";
  return "text-red-400";
}
