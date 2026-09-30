import { create } from "zustand";
import { persist } from "zustand/middleware";
import { offlineUuid } from "@/lib/backend";

export interface Account {
  id: string; // uuid (no dashes)
  name: string;
  kind: "offline" | "microsoft";
  accessToken?: string;
  refreshToken?: string;
  expiresAt?: number;
}

interface AccountState {
  accounts: Account[];
  activeId?: string;
  active: () => Account | undefined;
  /** True when the active Microsoft session is usable (or offline). */
  isSessionValid: () => boolean;
  addOffline: (name: string) => Promise<Account>;
  addMicrosoft: (a: Account) => void;
  updateTokens: (id: string, accessToken: string, refreshToken: string, expiresAt: number) => void;
  switchTo: (id: string) => void;
  remove: (id: string) => void;
}

export const useAccounts = create<AccountState>()(
  persist(
    (set, get) => ({
      accounts: [],
      activeId: undefined,
      active: () => get().accounts.find((a) => a.id === get().activeId) ?? get().accounts[0],
      isSessionValid: () => {
        const a = get().accounts.find((x) => x.id === get().activeId) ?? get().accounts[0];
        if (!a) return false;
        if (a.kind === "offline") return true;
        // 5-minute grace buffer — the launcher refreshes before this trips.
        return (a.expiresAt ?? 0) > Date.now() + 5 * 60_000;
      },
      addOffline: async (name) => {
        const clean = name.trim();
        if (!/^\w{3,16}$/.test(clean)) throw new Error("Username must be 3–16 letters/numbers/_");
        const id = (await offlineUuid(clean)).replace(/-/g, "");
        const acc: Account = { id, name: clean, kind: "offline" };
        set((s) => ({
          accounts: [...s.accounts.filter((a) => a.id !== id), acc],
          activeId: id,
        }));
        return acc;
      },
      addMicrosoft: (a) =>
        set((s) => ({
          accounts: [...s.accounts.filter((x) => x.id !== a.id), a],
          activeId: a.id,
        })),
      updateTokens: (id, accessToken, refreshToken, expiresAt) =>
        set((s) => ({
          accounts: s.accounts.map((a) =>
            a.id === id ? { ...a, accessToken, refreshToken, expiresAt } : a,
          ),
        })),
      switchTo: (activeId) => set({ activeId }),
      remove: (id) =>
        set((s) => ({
          accounts: s.accounts.filter((a) => a.id !== id),
          activeId: s.activeId === id ? s.accounts.find((a) => a.id !== id)?.id : s.activeId,
        })),
    }),
    { name: "tyx:accounts" },
  ),
);
