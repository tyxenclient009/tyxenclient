import { AnimatePresence, motion } from "framer-motion";
import { Check, ChevronDown, Search } from "lucide-react";
import { useEffect, useId, useMemo, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { cn } from "@/lib/utils";

export interface DropdownOption {
  value: string;
  label: string;
  hint?: string;
}

interface DropdownProps {
  /** Applied to the trigger so <label htmlFor> keeps working. */
  id?: string;
  value: string;
  onChange: (v: string) => void;
  options: DropdownOption[] | string[];
  ariaLabel: string;
  placeholder?: string;
  /** Monospace labels (versions, hashes, paths). */
  mono?: boolean;
  /** Filter box on top — for long lists like MC versions. */
  searchable?: boolean;
  className?: string;
}

/**
 * Custom glass dropdown replacing every native <select>.
 *
 * Premium treatment: gradient-glass trigger with icon-chip chevron,
 * solid frosted menu with section header, staggered item entrance,
 * accent rail on hover/selected, keyboard-navigable. Renders in a
 * portal so it never gets clipped by modal scroll containers.
 */
export function Dropdown({
  id,
  value,
  onChange,
  options,
  ariaLabel,
  placeholder = "Select…",
  mono,
  searchable,
  className,
}: DropdownProps) {
  const items = useMemo<DropdownOption[]>(
    () => options.map((o) => (typeof o === "string" ? { value: o, label: o } : o)),
    [options],
  );
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const listboxId = useId();
  const [hi, setHi] = useState(0);
  const [pos, setPos] = useState({ top: 0, left: 0, width: 0, up: false });
  const trigger = useRef<HTMLButtonElement>(null);
  const menu = useRef<HTMLDivElement>(null);
  const search = useRef<HTMLInputElement>(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return items;
    return items.filter(
      (o) =>
        o.label.toLowerCase().includes(q) ||
        o.value.toLowerCase().includes(q) ||
        (o.hint ?? "").toLowerCase().includes(q),
    );
  }, [items, query]);

  const selected = items.find((o) => o.value === value);

  const measure = () => {
    const r = trigger.current?.getBoundingClientRect();
    if (!r) return;
    const menuH = Math.min(320, 96 + filtered.length * 38);
    const up = r.bottom + menuH + 8 > window.innerHeight && r.top - menuH - 8 > 0;
    setPos({ top: r.bottom + 6, left: r.left, width: Math.max(r.width, 200), up });
  };

  const doOpen = () => {
    setQuery("");
    setHi(Math.max(0, filtered.findIndex((o) => o.value === value)));
    measure();
    setOpen(true);
  };

  const doClose = (refocus = false) => {
    setOpen(false);
    if (refocus) trigger.current?.focus();
  };

  const pick = (v: string) => {
    onChange(v);
    setOpen(false);
    trigger.current?.focus();
  };

  // Outside-click + Escape + scroll/resize (menu is position:fixed).
  useEffect(() => {
    if (!open) return;
    const onDown = (e: PointerEvent) => {
      const t = e.target as Node;
      if (trigger.current?.contains(t) || menu.current?.contains(t)) return;
      setOpen(false);
    };
    const onScroll = (e: Event) => {
      if (menu.current?.contains(e.target as Node)) return;
      setOpen(false);
    };
    const onResize = () => setOpen(false);
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") doClose(true);
    };
    window.addEventListener("pointerdown", onDown);
    window.addEventListener("scroll", onScroll, true);
    window.addEventListener("resize", onResize);
    window.addEventListener("keydown", onKey);
    if (searchable) setTimeout(() => search.current?.focus(), 30);
    return () => {
      window.removeEventListener("pointerdown", onDown);
      window.removeEventListener("scroll", onScroll, true);
      window.removeEventListener("resize", onResize);
      window.removeEventListener("keydown", onKey);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, searchable]);

  // Keep highlighted option in view.
  useEffect(() => {
    if (!open) return;
    menu.current?.querySelector(`[data-idx="${hi}"]`)?.scrollIntoView({ block: "nearest" });
  }, [open, hi]);

  const onKeys = (e: React.KeyboardEvent) => {
    if (!open && (e.key === "ArrowDown" || e.key === "Enter" || e.key === " ")) {
      e.preventDefault();
      doOpen();
      return;
    }
    if (!open) return;
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setHi((h) => Math.min(h + 1, filtered.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setHi((h) => Math.max(h - 1, 0));
    } else if (e.key === "Enter") {
      e.preventDefault();
      const o = filtered[hi];
      if (o) pick(o.value);
    } else if (e.key === "Escape") {
      e.preventDefault();
      doClose(true);
    } else if (e.key === "Tab") {
      setOpen(false);
    }
  };

  return (
    <>
      <button
        ref={trigger}
        id={id}
        type="button"
        aria-label={ariaLabel}
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-controls={open ? listboxId : undefined}
        aria-activedescendant={open && filtered[hi] ? `${listboxId}-${hi}` : undefined}
        onClick={() => (open ? doClose() : doOpen())}
        onKeyDown={onKeys}
        className={cn(
          "group flex h-10 w-full cursor-pointer items-center gap-2 rounded-lg border border-white/10 bg-gradient-to-b from-white/[0.07] to-transparent bg-surface-100/60 px-3 text-left text-sm backdrop-blur-xl backdrop-saturate-150 transition-all duration-150",
          "hover:border-accent-500/40 hover:shadow-glow",
          "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50",
          open && "border-accent-500/60 shadow-glow",
          className,
        )}
      >
        <span className={cn("min-w-0 flex-1 truncate", selected ? "font-semibold text-ink" : "text-ink-faint", mono && "font-mono text-[13px]")}>
          {selected ? selected.label : placeholder}
        </span>
        {selected?.hint && (
          <span className="shrink-0 rounded-full border border-white/10 bg-surface-300/60 px-1.5 py-px font-mono text-[11px] text-ink-muted">
            {selected.hint}
          </span>
        )}
        <span
          className={cn(
            "flex size-6 shrink-0 items-center justify-center rounded-md bg-surface-300/80 transition-colors duration-150",
            open ? "bg-accent-500/20 text-accent-400" : "text-ink-faint group-hover:text-ink",
          )}
        >
          <ChevronDown className={cn("size-3.5 transition-transform duration-200", open && "rotate-180")} />
        </span>
      </button>

      {createPortal(
        <AnimatePresence>
          {open && (
            <motion.div
              ref={menu}
              id={listboxId}
              role="listbox"
              aria-label={ariaLabel}
              initial={{ opacity: 0, y: pos.up ? 6 : -6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: pos.up ? 6 : -6 }}
              transition={{ duration: 0.12, ease: "easeOut" }}
              onKeyDown={onKeys}
              style={
                pos.up
                  ? { left: pos.left, width: pos.width, bottom: window.innerHeight - (trigger.current?.getBoundingClientRect().top ?? 0) + 6 }
                  : { left: pos.left, width: pos.width, top: pos.top }
              }
              className="fixed z-[120] overflow-hidden rounded-xl border border-white/15 bg-surface-200/90 shadow-3 backdrop-blur-2xl backdrop-saturate-150"
            >
              {/* Accent hairline on top */}
              <div className="hero-gradient-bar h-0.5 w-full opacity-70" aria-hidden />
              <p className="px-3.5 pb-1 pt-2.5 text-[10px] font-bold uppercase tracking-[0.14em] text-ink-faint">
                {ariaLabel}
              </p>
              {searchable && (
                <div className="relative px-2.5 pb-1.5">
                  <Search className="pointer-events-none absolute left-5 top-1/2 size-3.5 -translate-y-[calc(50%+3px)] text-ink-faint" />
                  <input
                    ref={search}
                    value={query}
                    onChange={(e) => {
                      setQuery(e.target.value);
                      setHi(0);
                    }}
                    onKeyDown={onKeys}
                    placeholder="Type to filter…"
                    aria-label={`Filter ${ariaLabel}`}
                    className="input h-9 rounded-lg pl-8 text-[13px]"
                  />
                </div>
              )}
              <div className="max-h-[248px] overflow-y-auto p-1.5" role="presentation">
                {filtered.length === 0 && (
                  <p className="px-3 py-4 text-center text-[13px] text-ink-faint">No matches.</p>
                )}
                {filtered.map((o, i) => {
                  const active = o.value === value;
                  const hot = i === hi;
                  // Plain button on purpose: the old per-item staggered
                  // entrance replayed on every keystroke / list change,
                  // which flickered like a double-click in long menus.
                  // The menu container keeps its spring — items just appear.
                  return (
                    <button
                      key={o.value}
                      id={`${listboxId}-${i}`}
                      data-idx={i}
                      role="option"
                      aria-selected={active}
                      onMouseEnter={() => setHi(i)}
                      onClick={() => pick(o.value)}
                      className={cn(
                        "relative flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-left text-sm transition-colors duration-100",
                        active ? "bg-accent-500/15 text-ink" : hot ? "bg-white/[0.06] text-ink" : "text-ink-muted",
                      )}
                    >
                      {/* Accent rail */}
                      <span
                        className={cn(
                          "absolute left-1 h-5 w-[3px] rounded-full bg-accent-400 transition-all duration-150",
                          active || hot ? "opacity-100" : "opacity-0",
                          active ? "h-6" : "h-4",
                        )}
                      />
                      <span className={cn("min-w-0 flex-1 truncate", active && "font-semibold", mono && "font-mono text-[13px]")}>
                        {o.label}
                      </span>
                      {o.hint && (
                        <span className="shrink-0 rounded-full border border-white/10 bg-surface-300/60 px-1.5 py-px font-mono text-[11px] text-ink-muted">
                          {o.hint}
                        </span>
                      )}
                      {active && <Check className="size-4 shrink-0 text-accent-400" strokeWidth={3} />}
                    </button>
                  );
                })}
              </div>
            </motion.div>
          )}
        </AnimatePresence>,
        document.body,
      )}
    </>
  );
}
