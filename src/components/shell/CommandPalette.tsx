import { AnimatePresence, motion } from "framer-motion";
import { Boxes, Compass, Home, Layers, Search, Settings, Shirt } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useNav } from "@/stores/app";
import { useInstances } from "@/stores/instances";
import { usePalette } from "@/stores/palette";
import { playInstance } from "@/lib/play";
import { useScrollLock } from "@/lib/scroll-lock";

const ACTIONS = [
  { id: "home", label: "Go to Home", icon: Home },
  { id: "instances", label: "Go to Instances", icon: Layers },
  { id: "browse", label: "Go to Browse", icon: Compass },
  { id: "skins", label: "Go to Skins", icon: Shirt },
  { id: "settings", label: "Go to Settings", icon: Settings },
] as const;

/** Cmd/Ctrl+K palette — pages, instances (Enter = play), browse shortcut. */
export function CommandPalette() {
  const open = usePalette((s) => s.open);
  const setOpen = usePalette((s) => s.setOpen);
  const [q, setQ] = useState("");
  const go = useNav((s) => s.go);
  const instances = useInstances((s) => s.instances);
  useScrollLock(open);

  useEffect(() => {
    const h = (e: KeyboardEvent) => {
      // Ctrl+K / Cmd+K — guard e.key variants (some layouts send 'K').
      if ((e.metaKey || e.ctrlKey) && (e.key === "k" || e.key === "K" || e.code === "KeyK")) {
        e.preventDefault();
        usePalette.getState().toggle();
        setQ("");
      }
      if (e.key === "Escape") setOpen(false);
    };
    window.addEventListener("keydown", h);
    return () => window.removeEventListener("keydown", h);
  }, [setOpen]);

  const rows = useMemo(() => {
    const needle = q.toLowerCase();
    const acts = ACTIONS.filter((a) => a.label.toLowerCase().includes(needle)).map((a) => ({
      key: `go-${a.id}`,
      icon: a.icon,
      title: a.label,
      hint: "page",
      run: () => go(a.id),
    }));
    const insts = instances
      .filter((i) => i.name.toLowerCase().includes(needle))
      .slice(0, 6)
      .map((i) => ({
        key: `play-${i.id}`,
        icon: Boxes,
        title: `Play ${i.name}`,
        hint: `${i.version} · ${i.loader}`,
        run: () => playInstance(i.id).catch(() => {}),
      }));
    return [...acts, ...insts].slice(0, 10);
  }, [q, instances, go]);

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 z-[90] flex items-start justify-center bg-black/50 pt-32 backdrop-blur-sm"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={() => setOpen(false)}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.97, y: -8 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.97, y: -8 }}
            onClick={(e) => e.stopPropagation()}
            className="panel-raised w-full max-w-xl overflow-hidden p-0"
          >
            <div className="flex items-center gap-2 border-b px-4">
              <Search className="size-4 text-ink-faint" />
              <input
                autoFocus
                className="h-12 flex-1 bg-transparent text-[15px] outline-none placeholder:text-ink-faint"
                placeholder="Search pages, instances… (Enter plays)"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && rows[0]) {
                    rows[0].run();
                    setOpen(false);
                  }
                }}
              />
              <kbd className="rounded border px-1.5 py-0.5 font-mono text-[10px] text-ink-faint">ESC</kbd>
            </div>
            <div className="max-h-72 overflow-y-auto p-2">
              {rows.length === 0 && <p className="px-3 py-6 text-center text-sm text-ink-faint">No matches.</p>}
              {rows.map((r) => (
                <button
                  key={r.key}
                  onClick={() => {
                    r.run();
                    setOpen(false);
                  }}
                  className="flex w-full items-center gap-3 rounded-md px-3 py-2.5 text-left text-sm hover:bg-surface-200/70"
                >
                  <r.icon className="size-4 shrink-0 text-ink-faint" />
                  <span className="min-w-0 flex-1 truncate font-medium">{r.title}</span>
                  <span className="shrink-0 whitespace-nowrap text-xs text-ink-faint">{r.hint}</span>
                </button>
              ))}
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
