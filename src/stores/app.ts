import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { ThemeMode } from "@/styles/tokens";

type NavRoute = "home" | "instances" | "browse" | "skins" | "announcements" | "settings";

interface ThemeState {
  mode: ThemeMode;
  /** Effective resolved theme (system → dark/light based on OS). */
  resolved: "dark" | "light";
  setMode: (mode: ThemeMode) => void;
  setResolved: (r: "dark" | "light") => void;
}

function applyTheme(resolved: "dark" | "light") {
  document.documentElement.classList.toggle("dark", resolved === "dark");
}

function resolveSystem(): "dark" | "light" {
  return window.matchMedia?.("(prefers-color-scheme: light)").matches ? "light" : "dark";
}

export const useTheme = create<ThemeState>()(
  persist(
    (set, get) => ({
      mode: "dark",
      resolved: "dark",
      setMode: (mode) => {
        const resolved = mode === "system" ? resolveSystem() : mode;
        applyTheme(resolved);
        set({ mode, resolved });
      },
      setResolved: (resolved) => {
        // Only auto-follow OS when the user chose "system".
        if (get().mode === "system") {
          applyTheme(resolved);
          set({ resolved });
        }
      },
    }),
    { name: "tyx:theme" },
  ),
);

/** Apply persisted theme before first paint (called from main.tsx). */
export function initTheme() {
  const { mode } = useTheme.getState();
  const resolved = mode === "system" ? resolveSystem() : mode;
  useTheme.setState({ resolved });
  applyTheme(resolved);
  window.matchMedia?.("(prefers-color-scheme: light)").addEventListener?.("change", (e) => {
    useTheme.getState().setResolved(e.matches ? "light" : "dark");
  });
}

interface NavState {
  route: NavRoute;
  go: (r: NavRoute) => void;
}

export const useNav = create<NavState>((set) => ({
  route: "home",
  go: (route) => set({ route }),
}));

interface SidebarState {
  collapsed: boolean;
  toggle: () => void;
  setCollapsed: (v: boolean) => void;
}

/** Left-rail collapse — persisted, in-flow (never overlays content). */
export const useSidebar = create<SidebarState>()(
  persist(
    (set) => ({
      collapsed: false,
      toggle: () => set((s) => ({ collapsed: !s.collapsed })),
      setCollapsed: (collapsed) => set({ collapsed }),
    }),
    { name: "tyx:sidebar" },
  ),
);

export type ToastKind = "success" | "error" | "info";
export interface Toast {
  id: number;
  kind: ToastKind;
  title: string;
  body?: string;
}

let toastId = 1;

interface ToastState {
  toasts: Toast[];
  push: (t: Omit<Toast, "id">) => void;
  dismiss: (id: number) => void;
}

export const useToasts = create<ToastState>((set) => ({
  toasts: [],
  push: (t) => {
    const id = toastId++;
    set((s) => ({ toasts: [...s.toasts, { ...t, id }] }));
    // Auto-dismiss after 4s (errors linger a bit longer)
    setTimeout(() => {
      set((s) => ({ toasts: s.toasts.filter((x) => x.id !== id) }));
    }, t.kind === "error" ? 6000 : 4000);
  },
  dismiss: (id) => set((s) => ({ toasts: s.toasts.filter((x) => x.id !== id) })),
}));
