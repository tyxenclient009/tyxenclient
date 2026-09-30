import { motion } from "framer-motion";
import { useNav, useSidebar } from "@/stores/app";
import { usePalette } from "@/stores/palette";
import { AccountSwitcher } from "./AccountSwitcher";
import { TyxLogo } from "@/components/icons/brand";
import {
  CollapseIcon,
  ExpandIcon,
  ExploreIcon,
  GearIcon,
  HomeIcon,
  MegaphoneIcon,
  InstancesIcon,
  SearchIcon,
  SkinIcon,
} from "@/components/icons/brand";
import { cn } from "@/lib/utils";

const items = [
  { id: "home" as const, label: "Home", Icon: HomeIcon, hint: "Recent instances & quick play" },
  { id: "instances" as const, label: "Instances", Icon: InstancesIcon, hint: "Isolated .minecraft profiles" },
  { id: "browse" as const, label: "Browse", Icon: ExploreIcon, hint: "Mods, packs & shaders from the community index" },
  { id: "skins" as const, label: "Skins", Icon: SkinIcon, hint: "Player skins & capes for every account" },
  { id: "announcements" as const, label: "Announcements", Icon: MegaphoneIcon, hint: "News & updates from the Tyxen team" },
  { id: "settings" as const, label: "Settings", Icon: GearIcon, hint: "RAM, Java, updater & accounts" },
];

/**
 * Left rail: brand + nav + quick-switch + account switcher.
 *
 * Collapse model — in-flow, never an overlay:
 *   expanded  w-[220px] — icon + label + active pill
 *   collapsed w-[68px]  — icon rail, labels hidden, `title` tooltips
 * The toggle lives at the TOP-RIGHT of the rail header (upper side), so it
 * never covers nav content. `main` flex-grows to fill the freed space.
 */
export function Sidebar() {
  const { route, go } = useNav();
  const collapsed = useSidebar((s) => s.collapsed);
  const toggle = useSidebar((s) => s.toggle);

  return (
    <aside
      data-collapsed={collapsed ? "true" : "false"}
      className={cn(
        "flex shrink-0 flex-col gap-1 overflow-hidden rounded-xl border border-white/10 bg-surface-100/50 shadow-1 backdrop-blur-xl backdrop-saturate-150 transition-[width] duration-200 ease-out",
        collapsed ? "w-[68px] p-2" : "w-[220px] p-3",
      )}
    >
      {/* Header row: brand + collapse toggle pinned to the upper-right */}
      <div className={cn("flex items-center gap-2 pb-2", collapsed ? "flex-col px-0" : "px-1")}>
        <div className={cn("flex min-w-0 items-center gap-2", collapsed && "justify-center")}>
          <TyxLogo className="size-6 shrink-0 rounded-md" />
          {!collapsed && (
            <span className="truncate text-[13px] font-bold tracking-tight">Tyxen Launcher</span>
          )}
        </div>
        {!collapsed && <div className="flex-1" />}
        <button
          onClick={toggle}
          title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
          aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
          aria-expanded={!collapsed}
          className={cn(
            "flex size-7 shrink-0 items-center justify-center rounded-md text-ink-faint transition",
            "hover:bg-surface-200/80 hover:text-ink active:scale-95",
            "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50",
            collapsed && "mt-1",
          )}
        >
          {collapsed ? <ExpandIcon className="size-4" /> : <CollapseIcon className="size-4" />}
        </button>
      </div>

      <nav className="flex flex-col gap-1" aria-label="Primary">
        {items.map(({ id, label, Icon, hint }) => {
          const isActive = route === id;
          return (
            <button
              key={id}
              onClick={() => go(id)}
              title={collapsed ? `${label} — ${hint}` : hint}
              aria-label={label}
              aria-current={isActive ? "page" : undefined}
              className={cn(
                "group relative flex h-10 items-center gap-3 rounded-lg text-sm font-medium transition-all duration-150 active:scale-[0.98]",
                "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50",
                collapsed ? "justify-center px-0" : "px-3",
                isActive
                  ? "bg-surface-200/80 text-ink shadow-1"
                  : "text-ink-muted hover:bg-surface-200/50 hover:text-ink hover:font-semibold",
              )}
            >
              <Icon
                className={cn(
                  "size-[18px] shrink-0 transition-all duration-150 group-hover:scale-110",
                  isActive ? "text-accent-400" : "text-ink-faint group-hover:text-accent-400",
                )}
              />
              {!collapsed && (
                <>
                  <span className="truncate transition-all group-hover:font-semibold">{label}</span>
                  {isActive ? (
                    <motion.span
                      layoutId="nav-active-pill"
                      className="ml-auto h-5 w-1 shrink-0 rounded-full bg-accent-500 shadow-glow"
                      transition={{ type: "spring", stiffness: 500, damping: 38 }}
                    />
                  ) : (
                    <span className="ml-auto h-5 w-1 shrink-0" />
                  )}
                </>
              )}
              {collapsed && isActive && (
                <motion.span
                  layoutId="nav-active-pill"
                  className="absolute right-1 h-5 w-1 shrink-0 rounded-full bg-accent-500 shadow-glow"
                  transition={{ type: "spring", stiffness: 500, damping: 38 }}
                />
              )}
            </button>
          );
        })}
      </nav>

      <div className="flex-1" />

      {/* Quick switch — icon-only in collapsed mode */}
      <button
        onClick={() => usePalette.getState().setOpen(true)}
        title="Quick switch (Ctrl K)"
        className={cn(
          "flex items-center gap-2 rounded-md py-1.5 text-left text-[10px] font-semibold uppercase tracking-widest text-ink-faint transition",
          "hover:bg-surface-200/50 hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50",
          collapsed ? "justify-center px-0" : "px-2",
        )}
      >
        <SearchIcon className="size-3.5" />
        {!collapsed && (
          <>
            <span className="flex-1">Quick switch</span>
            <kbd className="rounded border border-white/10 bg-surface-200/60 px-1 py-px font-mono text-[9px] normal-case tracking-normal">
              Ctrl K
            </kbd>
          </>
        )}
      </button>

      <AccountSwitcher compact={collapsed} />
    </aside>
  );
}
