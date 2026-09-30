import { create } from "zustand";
import { persist } from "zustand/middleware";

/**
 * Microsoft login uses the OFFICIAL Minecraft Launcher app registration.
 * Custom Azure app IDs are rejected by Minecraft Services with HTTP 403
 * "Invalid app registration" (see https://aka.ms/AppRegInfo) — Mojang only
 * authorizes their own registration for login_with_xbox. This public client
 * ID is compiled in and is NOT user-editable. Auth-code flow with the
 * `https://login.live.com/oauth20_desktop.srf` redirect (see microsoft.ts).
 */
export const AZURE_CLIENT_ID = "00000000402b5328";

/** Redirect registered on the official app — the browser lands here with ?code= after sign-in. */
export const MS_REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";

/** Global launcher settings (Phase 5) — persisted, applied everywhere. */
export interface GlobalSettings {  defaultRamMb: number;
  javaPath: string; // empty = auto-detect
  concurrency: number; // parallel downloads
  storagePath: string; // display only in Phase 5 (Rust owns the real path)
  checkUpdates: boolean;
}

interface SettingsState extends GlobalSettings {
  set: (p: Partial<GlobalSettings>) => void;
}

export const useSettings = create<SettingsState>()(
  persist(
    (set) => ({
      defaultRamMb: 4096,
      javaPath: "",
      concurrency: 6,
      storagePath: "",
      checkUpdates: true,
      set: (p) => set(p),
    }),
    {
      name: "tyx:settings",
      // Strip any legacy `azureClientId` left in old installs — the ID is
      // locked now, stored overrides must never come back.
      merge: (persisted, current) => {
        const p = { ...((persisted ?? {}) as Record<string, unknown>) };
        delete p.azureClientId;
        return { ...current, ...p };
      },
    },
  ),
);
