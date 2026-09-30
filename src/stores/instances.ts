import { create } from "zustand";
import * as api from "@/lib/backend";

interface InstanceState {
  instances: api.Instance[];
  loading: boolean;
  error?: string;
  selectedId?: string;
  refresh: () => Promise<void>;
  create: (input: { name: string; version: string; loader: string; loaderVersion?: string; icon: string }) => Promise<api.Instance>;
  remove: (id: string) => Promise<void>;
  saveSettings: (id: string, s: api.InstanceSettings) => Promise<void>;
  setIcon: (id: string, icon: string) => Promise<void>;
  select: (id?: string) => void;
}

export const useInstances = create<InstanceState>((set, get) => ({
  instances: [],
  loading: true,
  selectedId: undefined,
  refresh: async () => {
    // First load shows skeletons; every later refresh is silent so tab
    // navigation never flashes loading placeholders over existing data.
    const first = get().instances.length === 0;
    if (first) set({ loading: true, error: undefined });
    try {
      const instances = await api.listInstances();
      set({ instances, loading: false, error: undefined });
    } catch (e) {
      set({ error: e instanceof Error ? e.message : String(e), loading: false });
    }
  },
  create: async (input) => {
    const inst = await api.createInstance(input);
    set({ instances: [inst, ...get().instances], selectedId: inst.id });
    return inst;
  },
  remove: async (id) => {
    await api.deleteInstance(id);
    set({ instances: get().instances.filter((i) => i.id !== id) });
    // Never leak per-instance maps — stale badges/pins could resurface
    // on ID reuse.
    try {
      const { clearInstalledFor } = await import("@/lib/modrinth");
      clearInstalledFor(id);
    } catch {
      /* ignore */
    }
    try {
      const { clearAllModMeta } = await import("@/lib/mod-meta");
      clearAllModMeta(id);
    } catch {
      /* ignore */
    }
  },
  saveSettings: async (id, settings) => {
    const updated = await api.updateInstanceSettings(id, settings);
    set({ instances: get().instances.map((i) => (i.id === id ? updated : i)) });
  },
  setIcon: async (id, icon) => {
    const updated = await api.setInstanceIcon(id, icon);
    set({ instances: get().instances.map((i) => (i.id === id ? updated : i)) });
  },
  select: (selectedId) => set({ selectedId }),
}));

export const ICON_CHOICES = ["pickaxe", "sword", "castle", "dragon", "wheat", "cog", "flask", "galaxy"];
export const LOADERS = ["Vanilla", "Fabric", "Forge", "Quilt", "NeoForge"] as const;
