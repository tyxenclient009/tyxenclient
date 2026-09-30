import { create } from "zustand";

/**
 * Global modpack (.mrpack) install progress.
 * Single active install at a time — visible on every page so the user
 * always sees live % while files download (no silent waits).
 * All labels are plain English.
 */
interface ModpackProgressState {
  active: boolean;
  /** Display name of the pack being installed. */
  name: string;
  /** 0–100 */
  pct: number;
  /** Backend stage: modpack / client / libraries / assets / done */
  stage: string;
  /** Current file label from the backend. */
  current: string;
  doneFiles: number;
  totalFiles: number;
  start: (name: string) => void;
  update: (p: { pct: number; stage: string; current: string; doneFiles: number; totalFiles: number }) => void;
  finish: () => void;
}

export const useModpackProgress = create<ModpackProgressState>((set) => ({
  active: false,
  name: "",
  pct: 0,
  stage: "",
  current: "",
  doneFiles: 0,
  totalFiles: 0,
  start: (name) =>
    set({ active: true, name, pct: 0, stage: "starting", current: "Preparing install", doneFiles: 0, totalFiles: 0 }),
  update: (p) => set({ active: true, ...p }),
  finish: () =>
    set({ active: false, name: "", pct: 0, stage: "", current: "", doneFiles: 0, totalFiles: 0 }),
}));
