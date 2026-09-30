import { useCallback, useEffect, useState } from "react";
import { MegaphoneIcon } from "@/components/icons/brand";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/States";
import { cn } from "@/lib/utils";

interface Announcement {
  id: number;
  title: string;
  body?: string;
  date?: string;
}

import { tyxenFetch } from "@/lib/tyxen-http";

/** Team announcements — posted from the Tyxen Admin app. */
export function AnnouncementsPage() {
  const [items, setItems] = useState<Announcement[] | null>(null);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const withTimeout = async (p: Promise<any>, ms: number, label: string): Promise<any> => {
        let timer: ReturnType<typeof setTimeout>;
        const timeout = new Promise<never>((_, reject) => {
          timer = setTimeout(() => reject(new Error(`${label} timed out`)), ms);
        });
        try {
          return await Promise.race([p, timeout]);
        } finally {
          clearTimeout(timer!);
        }
      };
      const [teamSettled, mcSettled] = await Promise.allSettled([
        withTimeout(tyxenFetch("/api/tyxen/announcements"), 8000, "Team news"),
        withTimeout(
          fetch("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").then((r) => {
            if (!r.ok) throw new Error(`HTTP ${r.status}`);
            return r.json();
          }),
          8000,
          "Mojang news",
        ),
      ]);
      const official: Announcement[] = [];
      if (mcSettled.status === "fulfilled") {
        const mc = mcSettled.value as { latest?: { release?: string; snapshot?: string } };
        if (mc?.latest?.release) {
          official.push({
            id: -1,
            title: `Minecraft ${mc.latest.release} is out!`,
            body: "Latest official release. Create an instance to play it with Tyxen Launcher.",
            date: "Official",
          });
        }
        if (mc?.latest?.snapshot && mc.latest.snapshot !== mc.latest.release) {
          official.push({
            id: -2,
            title: `Snapshot ${mc.latest.snapshot} available`,
            body: "Test the newest Minecraft features early. Snapshots may be unstable.",
            date: "Official",
          });
        }
      }
      let team: Announcement[] = [];
      if (teamSettled.status === "fulfilled" && teamSettled.value.ok) {
        const data = (await teamSettled.value.json()) as { announcements?: Announcement[] };
        team = data.announcements ?? [];
      }
      if (teamSettled.status === "rejected" && mcSettled.status === "rejected") throw new Error("offline");
      setItems([...official, ...team]);
    } catch {
      setItems(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div className="mx-auto flex w-full max-w-[720px] flex-col gap-3 p-5">
      <div className="flex items-center gap-3">
        <span className="flex size-10 items-center justify-center rounded-xl bg-accent-500/12">
          <MegaphoneIcon className="size-5 text-accent-400" />
        </span>
        <div className="min-w-0 flex-1">
          <h1 className="truncate text-lg font-bold tracking-tight">Announcements</h1>
          <p className="truncate text-xs text-ink-faint">News & updates from the Tyxen team</p>
        </div>
        <Button variant="secondary" size="sm" onClick={load} disabled={loading}>
          {loading ? "Refreshing…" : "Refresh"}
        </Button>
      </div>
      {items === null ? (
        <EmptyState
          icon={MegaphoneIcon}
          title="Couldn't reach the update server"
          hint="Check your connection — announcements appear here automatically."
        />
      ) : items.length === 0 ? (
        <EmptyState icon={MegaphoneIcon} title="No announcements yet" hint="When the team posts news, it'll show up here." />
      ) : (
        <div className="flex flex-col gap-2.5">
          {items.map((a) => (
            <div
              key={a.id}
              className={cn(
                "rounded-xl border border-white/10 bg-surface-100/60 px-4 py-3 shadow-1 backdrop-blur-xl",
              )}
            >
              <div className="flex items-baseline justify-between gap-2">
                <span className="text-[14px] font-bold tracking-tight">{a.title}</span>
                {a.date && <span className="shrink-0 font-mono text-[11px] text-ink-faint">{a.date}</span>}
              </div>
              {a.body && <p className="mt-1 text-[13px] leading-relaxed text-ink-muted">{a.body}</p>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
