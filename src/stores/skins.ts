import { create } from "zustand";
import { persist } from "zustand/middleware";
import * as lib from "@/lib/skins";

export type { SkinMeta } from "@/lib/skins";

export interface SkinSelection {
  skinId?: string;
  /** Built-in default skin id (when no library skin is picked). */
  defaultSkinId?: string;
  /** Premium accounts: prefer the Mojang account skin over the library. */
  useAccountSkin?: boolean;
}

interface SkinsState {
  skins: lib.SkinMeta[];
  /** Per-account picks (account uuid → selection). */
  activeByAccount: Record<string, SkinSelection>;
  loaded: boolean;
  refresh: () => Promise<void>;
  addSkin: (file: File) => Promise<lib.SkinMeta>;
  removeSkin: (id: string) => Promise<void>;
  setSkinModel: (id: string, model: lib.SkinModel) => Promise<void>;
  setSelection: (accountId: string, patch: Partial<SkinSelection>) => void;
  selection: (accountId: string) => SkinSelection;
}

export const useSkins = create<SkinsState>()(
  persist(
    (set, get) => ({
      skins: [],
      activeByAccount: {},
      loaded: false,
      refresh: async () => {
        const skins = await lib.listSkins();
        // Drop selections pointing at deleted files.
        const sids = new Set(skins.map((s) => s.id));
        const activeByAccount: Record<string, SkinSelection> = {};
        for (const [acc, sel] of Object.entries(get().activeByAccount)) {
          activeByAccount[acc] = {
            ...sel,
            skinId: sel.skinId && sids.has(sel.skinId) ? sel.skinId : undefined,
          };
        }
        set({ skins, activeByAccount, loaded: true });
      },
      addSkin: async (file) => {
        const meta = await lib.addSkinFile(file);
        set((s) => ({ skins: [meta, ...s.skins] }));
        return meta;
      },
      removeSkin: async (id) => {
        const meta = get().skins.find((s) => s.id === id);
        if (meta) await lib.deleteSkin(meta).catch(() => {});
        set((s) => ({
          skins: s.skins.filter((x) => x.id !== id),
          activeByAccount: Object.fromEntries(
            Object.entries(s.activeByAccount).map(([acc, sel]) => [
              acc,
              sel.skinId === id ? { ...sel, skinId: undefined } : sel,
            ]),
          ),
        }));
      },
      setSkinModel: async (id, model) => {
        const meta = get().skins.find((s) => s.id === id);
        if (!meta) return;
        await lib.updateSkinModel(meta, model).catch(() => {});
        set((s) => ({ skins: s.skins.map((x) => (x.id === id ? { ...x, model } : x)) }));
      },
      setSelection: (accountId, patch) =>
        set((s) => ({
          activeByAccount: { ...s.activeByAccount, [accountId]: { ...s.activeByAccount[accountId], ...patch } },
        })),
      selection: (accountId) => get().activeByAccount[accountId] ?? {},
    }),
    {
      name: "tyx:skins",
      partialize: (s) => ({ skins: s.skins, activeByAccount: s.activeByAccount }),
    },
  ),
);
