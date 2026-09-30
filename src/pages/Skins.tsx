import { Suspense, lazy, useEffect, useMemo, useRef, useState } from "react";
import { Check, ChevronDown, Loader2, Move, Pencil, Plus, Shirt, Trash2, Upload } from "lucide-react";
import { FadeUp, Button } from "@/components/ui/Button";
import { Dropdown } from "@/components/ui/Dropdown";
import { EmptyState } from "@/components/ui/States";
import { SkinPreview } from "@/components/skins/SkinPreview";
import { EditSkinModal } from "@/components/skins/EditSkinModal";
// 3D engine (three.js) loads only when this page opens — keeps app boot fast.
const SkinViewer3D = lazy(() =>
  import("@/components/skins/SkinViewer3D").then((m) => ({ default: m.SkinViewer3D })),
);

function ViewerFallback() {
  return (
    <div className="flex h-[340px] w-full items-center justify-center sm:h-[430px]">
      <Loader2 className="size-6 animate-spin text-ink-faint" />
    </div>
  );
}
import { useAccounts } from "@/stores/accounts";
import { useSkins } from "@/stores/skins";
import { useToasts } from "@/stores/app";
import {
  fetchMojangTextures,
  saveUrlAsSkin,
  skinDataUrl,
  type MojangTextures,
  type SkinMeta,
  type SkinModel,
} from "@/lib/skins";
import { DEFAULT_SKINS, getDefaultSkin } from "@/lib/default-skins";
import {
  equipSkin,
  mcProfile,
  type OwnedProfile,
} from "@/lib/microsoft";
import { cn } from "@/lib/utils";

/**
 * Skin selector — reference launcher look.
 * Left: player tag, big drag-to-rotate 3D preview, Edit action.
 * Right: Saved … (dashed upload + picks) and Default … grids.
 */
interface EffectiveSkin {
  url: string | null;
  model: SkinModel;
  kind: "account" | "library" | "default";
  lib?: SkinMeta;
  label: string;
}

export function SkinsPage() {
  const accounts = useAccounts((s) => s.accounts);
  const defaultActive = useAccounts((s) => s.active());
  const loaded = useSkins((s) => s.loaded);
  const refresh = useSkins((s) => s.refresh);
  const [accountId, setAccountId] = useState(defaultActive?.id ?? "");

  useEffect(() => {
    refresh().catch(() => {});
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!accountId && defaultActive) setAccountId(defaultActive.id);
  }, [defaultActive, accountId]);

  const account = useMemo(
    () => accounts.find((a) => a.id === accountId) ?? defaultActive,
    [accounts, accountId, defaultActive],
  );

  // Premium Mojang textures (shared by both tabs).
  const [tex, setTex] = useState<MojangTextures | null>(null);
  const [texLoading, setTexLoading] = useState(false);
  const [texTick, setTexTick] = useState(0);
  const texCache = useRef(new Map<string, MojangTextures>());
  // Equip changes the account skin/cape server-side — drop the cache and
  // refetch so the banner + 3D stage show the new look immediately.
  const refreshTex = useMemo(
    () => (id: string) => {
      texCache.current.delete(id);
      setTexTick((t) => t + 1);
    },
    [],
  );
  useEffect(() => {
    if (!account || account.kind !== "microsoft") {
      setTex(null);
      setTexLoading(false);
      return;
    }
    const hit = texCache.current.get(account.id);
    if (hit) {
      setTex(hit);
      setTexLoading(false);
      return;
    }
    let live = true;
    setTexLoading(true);
    fetchMojangTextures(account.id)
      .then((t) => {
        texCache.current.set(account.id, t);
        if (live) setTex(t);
      })
      .catch(() => live && setTex(null))
      .finally(() => live && setTexLoading(false));
    return () => {
      live = false;
    };
  }, [account?.id, account?.kind, texTick]); // eslint-disable-line react-hooks/exhaustive-deps

  if (!loaded) {
    return (
      <div className="mx-auto max-w-6xl">
        <h1 className="text-2xl font-bold tracking-tight">Skin selector</h1>
        <p className="mt-1 text-sm text-ink-muted">Loading your collection…</p>
      </div>
    );
  }

  if (!account) {
    return (
      <div className="mx-auto max-w-6xl">
        <FadeUp>
          <h1 className="text-2xl font-bold tracking-tight">Skin selector</h1>
          <div className="mt-6">
            <EmptyState
              icon={Shirt}
              title="Sign in first"
              hint="Skins belong to accounts — add an offline or Microsoft account from the sidebar, then come back."
            />
          </div>
        </FadeUp>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl">
      <FadeUp>
        <div className="flex flex-wrap items-end justify-between gap-3">
          <h1 className="text-2xl font-bold tracking-tight">Skin selector</h1>
          <div className="flex items-center gap-2">
            {accounts.length > 1 && (
              <Dropdown
                ariaLabel="Account for appearance"
                value={account.id}
                onChange={setAccountId}
                options={accounts.map((a) => ({ value: a.id, label: a.name, hint: a.kind }))}
                className="h-9 w-auto min-w-[180px] text-xs"
              />
            )}
          </div>
        </div>
      </FadeUp>

      {account.kind === "microsoft" && (
        <AccountBanner key={account.id} accountId={account.id} accountName={account.name} tex={tex} texLoading={texLoading} />
      )}

      <SkinSelectorTab key={account.id} accountId={account.id} accountKind={account.kind} tex={tex} refreshTex={refreshTex} />
    </div>
  );
}

/* ── shared bits ─────────────────────────────────────────────────────── */

function SectionHeader({ title, open, onToggle }: { title: string; open: boolean; onToggle: () => void }) {
  return (
    <button onClick={onToggle} aria-expanded={open} className="flex items-center gap-1.5 text-[15px] font-semibold text-ink-muted transition hover:text-ink">
      <ChevronDown className={cn("size-4 transition-transform duration-200", !open && "-rotate-90")} />
      {title}
    </button>
  );
}

function CheckBadge() {
  return (
    <span className="absolute right-2 top-2 flex size-6 items-center justify-center rounded-full bg-white text-black shadow-lg">
      <Check className="size-4" strokeWidth={3} />
    </span>
  );
}

function useLibraryUrls<T extends { id: string }>(items: T[], load: (t: T) => Promise<string>): Record<string, string> {
  const [urls, setUrls] = useState<Record<string, string>>({});
  useEffect(() => {
    let live = true;
    Promise.allSettled(items.map(async (it) => ({ id: it.id, url: await load(it) }))).then((rs) => {
      if (!live) return;
      const m: Record<string, string> = {};
      rs.forEach((r) => {
        if (r.status === "fulfilled") m[r.value.id] = r.value.url;
      });
      setUrls(m);
    });
    return () => {
      live = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [items.map((i) => i.id).join(",")]);
  return urls;
}

/* ── premium account banner ──────────────────────────────────────────── */

function AccountBanner({
  accountId, accountName, tex, texLoading,
}: {
  accountId: string; accountName: string; tex: MojangTextures | null; texLoading: boolean;
}) {
  const push = useToasts((s) => s.push);
  const sel = useSkins((s) => s.activeByAccount[accountId]);
  const setSelection = useSkins((s) => s.setSelection);
  const [saving, setSaving] = useState(false);
  const usingAccount = sel?.useAccountSkin !== false;

  return (
    <FadeUp delay={0.03}>
      <div className="mt-4 flex flex-wrap items-center gap-3 rounded-xl border border-white/10 bg-surface-100/60 px-4 py-3">
        <div className="flex size-11 items-center justify-center overflow-hidden rounded-lg border border-white/10 bg-surface-200/60">
          {texLoading ? (
            <Loader2 className="size-4 animate-spin text-ink-faint" />
          ) : tex?.skinUrl ? (
            <SkinPreview src={tex.skinUrl} model={tex.slim ? "slim" : "classic"} className="h-10" />
          ) : (
            <span className="text-lg font-bold text-accent-400">{accountName.slice(0, 1).toUpperCase()}</span>
          )}
        </div>
        <div className="min-w-0 flex-1 basis-48">
          <p className="truncate text-sm font-semibold">
            {accountName} — Mojang account skin
          </p>
          <p className="truncate text-[13px] text-ink-muted">
            {tex?.skinUrl ? "Used in-game automatically." : texLoading ? "Checking your account…" : "No custom skin on this account — Steve/Alex is used."}
          </p>
        </div>
        <Button variant={usingAccount ? "secondary" : "primary"} size="sm" onClick={() => setSelection(accountId, { useAccountSkin: true })}>
          {usingAccount && <Check />} {usingAccount ? "Using account skin" : "Use account skin"}
        </Button>
        {tex?.skinUrl && (
          <Button
            variant="ghost"
            size="sm"
            disabled={saving}
            onClick={async () => {
              setSaving(true);
              try {
                const meta = await saveUrlAsSkin(tex.skinUrl!, `${accountName}'s skin`);
                await useSkins.getState().refresh();
                push({ kind: "success", title: `Saved “${meta.name}” to the library` });
              } catch (e) {
                push({ kind: "error", title: "Couldn't save skin", body: e instanceof Error ? e.message : String(e) });
              } finally {
                setSaving(false);
              }
            }}
          >
            {saving ? <Loader2 className="animate-spin" /> : <Upload />} Save to library
          </Button>
        )}
      </div>
    </FadeUp>
  );
}

/* ── effective picks ─────────────────────────────────────────────────── */

function useSkinURLs() {
  const skins = useSkins((s) => s.skins);
  return { skins, urls: useLibraryUrls(skins, skinDataUrl) };
}

function resolveSkin(
  accountKind: string, sel: { skinId?: string; defaultSkinId?: string; useAccountSkin?: boolean },
  skins: SkinMeta[], urls: Record<string, string>, tex: MojangTextures | null,
): EffectiveSkin {
  if (accountKind === "microsoft" && sel.useAccountSkin !== false && tex?.skinUrl) {
    return { url: tex.skinUrl, model: tex.slim ? "slim" : "classic", kind: "account", label: "Account skin" };
  }
  const lib = skins.find((s) => s.id === sel.skinId);
  if (lib && urls[lib.id]) {
    return { url: urls[lib.id], model: lib.model, kind: "library", lib, label: lib.name };
  }
  const def = getDefaultSkin(sel.defaultSkinId) ?? DEFAULT_SKINS[0];
  return { url: def.make(), model: def.model, kind: "default", label: def.name };
}

/* ── skin selector tab ───────────────────────────────────────────────── */

function SkinSelectorTab({
  accountId, accountKind, tex, refreshTex,
}: {
  accountId: string; accountKind: string; tex: MojangTextures | null; refreshTex: (id: string) => void;
}) {
  const sel = useSkins((s) => s.activeByAccount[accountId]);
  const setSelection = useSkins((s) => s.setSelection);
  const addSkin = useSkins((s) => s.addSkin);
  const push = useToasts((s) => s.push);
  const { skins, urls } = useSkinURLs();
  const [savedOpen, setSavedOpen] = useState(true);
  const [defaultOpen, setDefaultOpen] = useState(true);
  const [editing, setEditing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const file = useRef<HTMLInputElement>(null);

  const selection = sel ?? {};
  const eff = resolveSkin(accountKind, selection, skins, urls, tex);

  const upload = async (f: File | undefined) => {
    if (!f) return;
    setUploading(true);
    try {
      const meta = await addSkin(f);
      setSelection(accountId, { skinId: meta.id, defaultSkinId: undefined, useAccountSkin: false });
      push({ kind: "success", title: `Added “${meta.name}”` });
    } catch (e) {
      push({ kind: "error", title: "Couldn't add skin", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setUploading(false);
      if (file.current) file.current.value = "";
    }
  };

  const isLibActive = (id: string) => eff.kind === "library" && (eff.lib?.id === id);
  const isDefActive = (id: string) => eff.kind === "default" && eff.label === getDefaultSkin(id)?.name;

  return (
    <div className="mt-5 grid grid-cols-1 gap-8 lg:grid-cols-[300px_1fr]">
      {/* ── left: stage ── */}
      <FadeUp>
        <div className="flex flex-col items-center">
          <span className="rounded-md bg-surface-200/80 px-4 py-1.5 font-mono text-sm font-semibold tracking-wide text-ink-muted">
            {useAccounts.getState().accounts.find((a) => a.id === accountId)?.name ?? "Player"}
          </span>
          <div className="relative mt-3 w-full">
            <div className="pointer-events-none absolute inset-x-8 bottom-6 top-10 rounded-full bg-accent-500/10 blur-2xl" aria-hidden />
            <Suspense fallback={<ViewerFallback />}>
              <SkinViewer3D
                skinUrl={eff.url}
                model={eff.model}
                className="relative h-[340px] w-full sm:h-[430px]"
              />
            </Suspense>
          </div>
          <p className="mt-4 flex items-center gap-1.5 text-sm text-ink-muted">
            <Move className="size-4" /> Drag to rotate
          </p>
          <Button
            variant="secondary"
            className="mt-2 rounded-full px-6"
            onClick={() => {
              if (eff.kind === "library" && eff.lib) setEditing(true);
              else push({ kind: "info", title: eff.kind === "account" ? "Account skins live on Mojang" : "Default skins can't be edited", body: "Upload your own skin to rename, restyle or replace it." });
            }}
          >
            <Pencil /> Edit skin
          </Button>
          <input ref={file} type="file" accept="image/png" className="hidden" aria-label="Upload skin PNG" onChange={(e) => upload(e.target.files?.[0])} />
        </div>
      </FadeUp>

      {/* ── right: collections ── */}
      <div className="min-w-0">
        <FadeUp delay={0.05}>
          <SectionHeader title="Saved skins" open={savedOpen} onToggle={() => setSavedOpen((o) => !o)} />
          {savedOpen && (
            <div className="mt-3 flex gap-3 overflow-x-auto pb-1">
              {/* dashed upload box */}
              <button
                onClick={() => file.current?.click()}
                onDragOver={(e) => {
                  e.preventDefault();
                  setDragOver(true);
                }}
                onDragLeave={() => setDragOver(false)}
                onDrop={(e) => {
                  e.preventDefault();
                  setDragOver(false);
                  const f = [...(e.dataTransfer.files ?? [])].find((x) => /\.png$/i.test(x.name));
                  if (f) upload(f);
                  else push({ kind: "error", title: "Drop a .png skin file" });
                }}
                className={cn(
                  "flex h-44 w-36 shrink-0 flex-col items-center justify-center gap-1.5 rounded-xl border border-dashed transition",
                  dragOver
                    ? "border-accent-400 bg-accent-500/10"
                    : "border-white/15 bg-surface-100/40 hover:border-accent-500/50 hover:bg-surface-200/40",
                )}
              >
                {uploading ? <Loader2 className="size-6 animate-spin text-ink-faint" /> : <Plus className="size-6 text-ink" />}
                <span className="text-sm font-bold">Add skin</span>
                <span className="text-xs text-ink-faint">Drag and drop</span>
              </button>
              {skins.map((s) => {
                const url = urls[s.id];
                const active = isLibActive(s.id);
                return (
                  <button
                    key={s.id}
                    onClick={() => setSelection(accountId, { skinId: s.id, defaultSkinId: undefined, useAccountSkin: false })}
                    title={s.name}
                    className={cn(
                      "relative h-44 w-36 shrink-0 overflow-hidden rounded-xl border bg-surface-200/50 shadow-2 transition",
                      active ? "border-white/40 shadow-glow" : "border-white/10 hover:border-white/25",
                    )}
                  >
                    {active && <CheckBadge />}
                    <div className="flex h-full items-center justify-center p-2">
                      {url ? <SkinPreview src={url} model={s.model} className="h-36" /> : <Loader2 className="animate-spin text-ink-faint" />}
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </FadeUp>

        <FadeUp delay={0.065}>
          <MicrosoftSkins accountId={accountId} refreshTex={refreshTex} />
        </FadeUp>

        <FadeUp delay={0.08}>
          <div className="mt-6">
            <SectionHeader title="Vanilla skins · Official Minecraft" open={defaultOpen} onToggle={() => setDefaultOpen((o) => !o)} />            {defaultOpen && (
              <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-4">
                {DEFAULT_SKINS.map((d) => {
                  const active = isDefActive(d.id);
                  return (
                    <button
                      key={d.id}
                      onClick={() => setSelection(accountId, { defaultSkinId: d.id, skinId: undefined, useAccountSkin: false })}
                      title={d.name}
                      className={cn(
                        "relative h-44 overflow-hidden rounded-xl border bg-surface-200/50 shadow-2 transition",
                        active ? "border-white/40 shadow-glow" : "border-white/10 hover:border-white/25",
                      )}
                    >
                      {active && <CheckBadge />}
                      <div className="flex h-full items-center justify-center p-2">
                        <SkinPreview src={d.make()} model={d.model} className="h-36" />
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        </FadeUp>
      </div>

      <EditSkinModal skin={editing && eff.kind === "library" ? (eff.lib ?? null) : null} onClose={() => setEditing(false)} />
    </div>
  );
}

/* ── Microsoft owned library (every skin + cape on the account) ────────── */

function useOwned(accountId: string) {
  const account = useAccounts((s) => s.accounts.find((a) => a.id === accountId));
  const [prof, setProf] = useState<OwnedProfile | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const key = account ? `${account.id}:${account.accessToken ?? ""}` : "";
  useEffect(() => {
    if (!account || account.kind !== "microsoft" || !account.accessToken) {
      setProf(null);
      setError("");
      setLoading(false);
      return;
    }
    let live = true;
    setLoading(true);
    setError("");
    mcProfile(account.accessToken)
      .then((p) => live && setProf(p))
      .catch((e) => live && setError(e instanceof Error ? e.message : String(e)))
      .finally(() => live && setLoading(false));
    return () => {
      live = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);
  return { prof, loading, error, setProf, token: account?.accessToken ?? "" };
}

function MicrosoftSkins({ accountId, refreshTex }: { accountId: string; refreshTex: (id: string) => void }) {
  const push = useToasts((s) => s.push);
  const { prof, loading, error, setProf, token } = useOwned(accountId);
  const [busy, setBusy] = useState<string | null>(null);
  if (!useAccounts.getState().accounts.find((a) => a.id === accountId && a.kind === "microsoft")) {
    return null;
  }
  const equip = async (skinId: string, variant: "CLASSIC" | "SLIM") => {
    setBusy(skinId);
    try {
      const next = await equipSkin(token, skinId, variant);
      setProf(next);
      refreshTex(accountId);
      push({ kind: "success", title: "Skin equipped", body: "Shows in-game within a minute." });
    } catch (e) {
      push({ kind: "error", title: "Couldn't equip skin", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setBusy(null);
    }
  };
  return (
    <div className="mt-6">
      <SectionHeader title={`Microsoft skins · ${prof?.skins.length ?? "…"}`} open onToggle={() => {}} />
      {loading ? (
        <p className="mt-3 flex items-center gap-2 text-sm text-ink-muted">
          <Loader2 className="size-4 animate-spin" /> Loading your Mojang skins…
        </p>
      ) : error ? (
        <p className="mt-3 text-sm text-ink-muted">{error}</p>
      ) : !prof || prof.skins.length === 0 ? (
        <p className="mt-3 text-sm text-ink-muted">No saved skins on this Microsoft account yet.</p>
      ) : (
        <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-4">
          {prof.skins.map((s) => {
            const active = s.state === "ACTIVE";
            return (
              <div
                key={s.id}
                className={cn(
                  "relative overflow-hidden rounded-xl border bg-surface-200/50 shadow-2",
                  active ? "border-accent-500/60 shadow-glow" : "border-white/10",
                )}
              >
                {active && <CheckBadge />}
                <div className="flex h-36 items-center justify-center p-2">
                  <SkinPreview src={s.url} model={s.variant === "SLIM" ? "slim" : "classic"} className="h-32" />
                </div>
                <div className="flex items-center justify-between gap-2 border-t border-white/10 px-2.5 py-2">
                  <span className="truncate text-xs font-semibold" title={s.alias || s.variant}>
                    {s.alias || s.variant.toLowerCase()}
                  </span>
                  <Button variant={active ? "secondary" : "primary"} size="sm" disabled={active || busy !== null} onClick={() => equip(s.id, s.variant)}>
                    {busy === s.id ? <Loader2 className="animate-spin" /> : active ? "Worn" : "Equip"}
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

/* ── Microsoft owned skins (kept above; capes removed — see git history) ── */

