import { useEffect, useState } from "react";
import { Loader2, Plus, Upload } from "lucide-react";
import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { Dropdown } from "@/components/ui/Dropdown";
import { ICON_CHOICES, LOADERS, useInstances } from "@/stores/instances";
import { INSTANCE_ICONS } from "@/components/icons/instances";
import { useSettings } from "@/stores/settings";
import { useToasts } from "@/stores/app";
import { fetchLoaderVersions, fetchMcVersions } from "@/lib/minecraft";
import { cn } from "@/lib/utils";

/**
 * Create-instance flow (Phase 2): name + icon + MC version (live Mojang
 * manifest) + loader + loader version. Creates the isolated folder via Rust
 * (or localStorage in browser preview) and selects it.
 */
export function CreateInstanceDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const create = useInstances((s) => s.create);
  const push = useToasts((s) => s.push);
  const defaultRam = useSettings((s) => s.defaultRamMb);
  const [name, setName] = useState("");
  const [icon, setIcon] = useState(ICON_CHOICES[0]);
  const [versions, setVersions] = useState<string[]>([]);
  const [version, setVersion] = useState("");
  const [loader, setLoader] = useState<(typeof LOADERS)[number]>("Fabric");
  const [loaderVersions, setLoaderVersions] = useState<string[]>(["recommended"]);
  const [loaderVersion, setLoaderVersion] = useState("recommended");
  const [busy, setBusy] = useState(false);
  /** Pending custom upload (`file:…` or data-URL) — applied after create. */
  const [uploadSrc, setUploadSrc] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    fetchMcVersions()
      .then((v) => {
        const releases = v.filter((x) => x.type === "release").map((x) => x.id);
        setVersions(releases.length ? releases : []);
        setVersion((cur) => cur || releases[0] || "1.20.4");
      })
      .catch(() => {
        setVersions(["1.21.1", "1.20.4", "1.19.2"]);
        setVersion("1.20.4");
      });
  }, [open ]);

  useEffect(() => {
    if (!open || !version) return;
    fetchLoaderVersions(loader, version).then((vs) => {
      setLoaderVersions(vs);
      // Keep the previous pick if still valid, else fall back to recommended.
      setLoaderVersion((cur) => (vs.includes(cur) ? cur : vs[0] ?? "recommended"));
    });
  }, [open, loader, version]);

  const submit = async () => {
    if (!name.trim()) {
      push({ kind: "error", title: "Name your instance" });
      return;
    }
    if (!version) {
      push({ kind: "error", title: "Pick a Minecraft version" });
      return;
    }
    setBusy(true);
    try {
      const setIcon = useInstances.getState().setIcon;
      const inst = await create({ name: name.trim(), version, loader, loaderVersion, icon });
      if (uploadSrc) {
        try {
          await setIcon(inst.id, uploadSrc);
        } catch (e) {
          push({ kind: "error", title: "Icon upload failed", body: e instanceof Error ? e.message : String(e) });
        }
      }
      push({ kind: "success", title: `Created “${inst.name}”`, body: `${version} · ${loader}${loader !== "Vanilla" ? ` ${loaderVersion}` : ""} · ${defaultRam} MB default RAM` });
      setName("");
      setUploadSrc(null);
      onClose();
    } catch (e) {
      push({ kind: "error", title: "Couldn't create instance", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose}>
      <h2 className="text-lg font-bold tracking-tight">New instance</h2>      <p className="mt-0.5 text-sm text-ink-muted">Isolated profile — own version, loader, mods, worlds.</p>

      <div className="mt-4 space-y-4">
        <div>
          <label className="label" htmlFor="ci-name">Name</label>
          <input id="ci-name" className="input" placeholder="My modded survival" value={name} onChange={(e) => setName(e.target.value)} />
        </div>

        <div>
          <span className="label">Icon</span>
          <div className="flex flex-wrap gap-1.5">
            {INSTANCE_ICONS.map(({ id, label, Icon }) => (
              <button
                key={id}
                title={label}
                aria-label={label}
                aria-pressed={!uploadSrc && icon === id}
                onClick={() => { setIcon(id); setUploadSrc(null); }}
                className={cn("flex size-10 items-center justify-center rounded-md border transition active:scale-95", !uploadSrc && icon === id ? "border-accent-500/60 bg-accent-500/10 text-accent-400" : "text-ink-muted hover:bg-surface-200/70 hover:text-ink")}
              >
                <Icon className="size-5" />
              </button>
            ))}
            <button
              title="Upload custom image (PNG/JPG/WebP)"
              aria-label="Upload custom icon"
              aria-pressed={!!uploadSrc}
              onClick={async () => {
                try {
                  const { pickImageFile } = await import("@/lib/backend");
                  const picked = await pickImageFile();
                  if (picked) setUploadSrc(picked);
                } catch (e) {
                  push({ kind: "error", title: "Couldn't pick image", body: e instanceof Error ? e.message : String(e) });
                }
              }}
              className={cn(
                "flex size-10 items-center justify-center rounded-md border border-dashed transition active:scale-95",
                uploadSrc ? "border-accent-500/60 bg-accent-500/10 text-accent-400" : "text-ink-faint hover:bg-surface-200/70 hover:text-ink",
              )}
            >
              {uploadSrc ? (
                <InstanceIconPreview src={uploadSrc} />
              ) : (
                <Upload className="size-5" />
              )}
            </button>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label" htmlFor="ci-ver">Minecraft version</label>
            <Dropdown
              id="ci-ver"
              ariaLabel="Minecraft version"
              value={version}
              onChange={setVersion}
              options={versions}
              mono
              searchable
              placeholder={versions.length ? "Pick a version…" : "Loading versions…"}
            />
          </div>
          <div>
            <label className="label" htmlFor="ci-loader">Loader</label>
            <Dropdown
              id="ci-loader"
              ariaLabel="Loader"
              value={loader}
              onChange={(v) => setLoader(v as typeof loader)}
              options={LOADERS.map((l) => ({ value: l, label: l }))}
            />
          </div>
        </div>

        {loader !== "Vanilla" && (
          <div>
            <label className="label" htmlFor="ci-lver">Loader version</label>
            <Dropdown
              id="ci-lver"
              ariaLabel="Loader version"
              value={loaderVersion}
              onChange={setLoaderVersion}
              options={loaderVersions}
              mono
              searchable={loaderVersions.length > 6}
              placeholder="recommended"
            />
          </div>
        )}

        <Button variant="primary" className="w-full" onClick={submit} disabled={busy}>
          {busy ? <Loader2 className="animate-spin" /> : <Plus />} {busy ? "Creating…" : "Create instance"}
        </Button>
      </div>
    </Modal>
  );
}

/** Pending-upload tile: filename chip (bytes load after create). */
function InstanceIconPreview({ src }: { src: string }) {
  if (src.startsWith("data:")) {
    return <img src={src} alt="" className="size-7 rounded-md object-cover" draggable={false} />;
  }
  const base = src.split(/[\\/]/).pop() ?? "image";
  return (
    <span className="max-w-[76px] truncate px-1 font-mono text-[9px] font-semibold" title={src}>
      {base}
    </span>
  );
}
