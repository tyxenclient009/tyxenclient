# Tyx Launcher — full-stack Minecraft launcher (Tauri v2 + React) + Tyxen mod + backend

## What it does (all 5 phases)

| Phase | Status | What |
|-------|--------|------|
| 1 Shell + design system | ✅ | Borderless window, custom title bar, sidebar, dark/light/system theme, toasts, skeletons, empty/error states |
| 2 Instances | ✅ | Create (live Mojang manifest + Fabric/Quilt/Forge/NeoForge), isolated folders on disk, detail drawer (mods/worlds/shots/logs), per-instance settings (RAM slider, Java override, JVM args, resolution) |
| 3 Modrinth | ✅ | Live search, loader + MC-version facets, 4 sorts, infinite scroll, project detail (gallery/description/versions), SHA1-verified one-click install into any instance |
| 4 Auth + launch | ✅ | Offline accounts (instant) + Microsoft one-click browser login (locked global Azure app) → Xbox → XSTS → MC profile, device-code fallback; multi-account switcher; JVM preview + spawn with live log console |
| 5 Polish | ✅ | Global settings (default RAM, Java detect, concurrency, updater), Cmd/Ctrl+K palette, NSIS/MSI packaging |
| 6 Tyxen stack | ✅ | Bundled client mod auto-inject + Fabric API, known-crash quarantine (`src/lib/incompat-mods.ts`), update manifest |

## Monorepo map

| Part | Path | Version | Serve/run |
|------|------|---------|-----------|
| Launcher | `src/`, `src-tauri/` | 0.1.0 (`__APP_VERSION__` in Settings) | `npm run tauri:dev` / `tauri:build` |
| Tyxen mod (client HUD) | `tyxen-src/`, `tyxen-res/`, `public/tyxen/` | 1.0.73 (`tyxen-res/fabric.mod.json`) | auto-injected on Play for Fabric 1.21.11 |
| Tyxen backend (store/capes/badges/updates) | `tyxen-server/server.cjs` | — | `node server.cjs` (port 8787; prod `api.tyxen.space:11789`) |
| Staff admin UI | `tyxen-admin/admin-ui/` (embedded into `server.cjs` → `GET /admin`) | — | same server |
| Retired companion mod | `tyx-client-mod/` | 1.6.2 (do not ship) | — |
| Vendored Fabric API (mod compile deps) | `tyxen-libs/` | 0.141.6+1.21.11 | — |
| Helper scripts (live) | `repack.py`, `tyxen-fixes.py`, `tyxen-rebrand.py`, `rename-vars.py`, `sig-sweep.py`, `yarn-lookup.py` | — | run manually |
| One-shot history (do NOT re-run) | `scripts/archive/` | — | archived, superseded |

Key backend routes: `/api/tyxen/ping|latest|online|announcements`, `/tyxen/active-users.txt` (LIVE 5-min TTL badge list), `/files/*`, `/textures/*`, `/admin/*` (session login). Store + cape wardrobe removed (410 Gone).

## Folder structure

```
src/
├── main.tsx / App.tsx
├── styles/            tokens.ts, globals.css (theme channels, panels, inputs)
├── lib/
│   ├── utils.ts       cn, formatBytes, timeAgo
│   ├── tauri.ts       window controls + isTauri
│   ├── backend.ts     typed Rust bridge w/ localStorage fallback (works in browser!)
│   ├── modrinth.ts    Modrinth v2 client (search/project/versions/facets)
│   ├── minecraft.ts   Mojang manifest + loader-meta fetch w/ offline fallback
│   ├── microsoft.ts   MS device-code → Xbox → XSTS → MC profile
│   └── play.ts        shared Press-Play pipeline
├── stores/            app (theme/nav/toasts), instances, accounts, settings, launch
├── components/
│   ├── ui/            Button, Card, Skeleton, States, Toasts, Modal
│   ├── shell/         TitleBar, Sidebar (accounts), AppShell, CommandPalette
│   ├── dialogs/       CreateInstance, InstanceDetail, ProjectDetail, Auth
│   └── launch/        LaunchConsole (live game logs)
└── pages/             Home, Instances, Browse, Settings
src-tauri/src/
├── main.rs            command registration
├── util.rs            app-data paths, ids
├── instances.rs       CRUD + file browsing over isolated folders
├── minecraft.rs       version fallback + loader record
├── java.rs            runtime detection (JAVA_HOME/PATH/bundled)
├── auth.rs            offline UUID derivation
└── launch.rs          JVM preview + spawn w/ tyx://game-log streaming
```

## Run it

```powershell
npm install
npm run dev            # browser preview (localStorage store, simulated launch)
```

Desktop (needs Rust from https://rustup.rs/):
```powershell
npm run tauri:dev      # real window, disk store, Java scan, real downloads
npm run tauri:build    # → src-tauri/target/release/bundle/nsis/*-setup.exe (+ .msi)
```

## Try the full loop (2 min, no Rust needed)

1. `npm run dev` → Home → **New instance** (live version list) → Create.
2. **Browse** → search "sodium" → **Install** (browser logs the would-be download; in Tauri it lands SHA1-verified in `mods/`).
3. Sidebar → **Sign in** → offline `Steve_123` → press **Play** on the card → watch the **console** stream.
4. `Ctrl+K` → type instance name → Enter plays. Instances → card → **Manage** → mods tab, RAM slider.

## Credentials you must supply

| What | Where | Notes |
|------|-------|-------|
| Microsoft login (MS login) | Built-in, locked | Global Azure app, Modrinth-style — no user setup. Offline mode works without it. NOTE: Mojang must authorize new Azure apps for the Minecraft API (`login_with_xbox` 403 until approved — request via https://aka.ms/mce-reviewappid, takes weeks). |
| Updater keypair | `tauri.conf.json → plugins.updater` | `active:false` + placeholder pubkey — updater OFF by design for now; Settings shows "Auto-update not configured", update via setup installer. To enable: `tauri signer generate`, set pubkey + `releases.tyx.gg` endpoint, flip `active:true`. |
| Code-signing cert | release CI | Without it the .exe installs with an "Unknown publisher" prompt. |

## Honest limits of this iteration

- **Game files auto-download** on first Play (client jar, libraries with OS rules,
  natives, assets from Mojang; JVM args built from version.json). Offline session
  (`--accessToken 0`) boots to menu; online servers need a real Microsoft session.
- **mrpack import** works via Rust `modpack_install` (drag-drop / picker + Modrinth install), progress over `tyx://assets`.
- Tokens persist via Tauri `store`; move refresh tokens to the OS keychain
  (plugin-keyring) before public release, and add MC token refresh on expiry.
