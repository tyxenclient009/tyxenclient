import { useState } from "react";
import { Loader2, Trash2, Upload } from "lucide-react";
import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { useSkins } from "@/stores/skins";
import { useToasts } from "@/stores/app";
import type { SkinMeta } from "@/lib/skins";
import { cn } from "@/lib/utils";

/**
 * Edit a library skin: rename, switch classic/slim, replace the PNG,
 * or delete. Used by the "Edit skin" button under the big preview.
 */
export function EditSkinModal({
  skin,
  onClose,
  onDeleted,
}: {
  skin: SkinMeta | null;
  onClose: () => void;
  onDeleted?: () => void;
}) {
  const setSkinModel = useSkins((s) => s.setSkinModel);
  const removeSkin = useSkins((s) => s.removeSkin);
  const push = useToasts((s) => s.push);
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);
  const open = !!skin;

  // Seed the name field whenever a different skin is opened.
  const key = skin?.id ?? "none";

  const save = async () => {
    if (!skin) return;
    setBusy(true);
    try {
      const { updateSkinName } = await import("@/lib/skins");
      await updateSkinName(skin, (name || skin.name).slice(0, 40));
      await useSkins.getState().refresh();
      push({ kind: "success", title: "Skin updated" });
      onClose();
    } catch (e) {
      push({ kind: "error", title: "Couldn't save", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setBusy(false);
    }
  };

  const replace = async (f: File | undefined) => {
    if (!skin || !f) return;
    setBusy(true);
    try {
      const { fileToBytes, replaceSkinBytes } = await import("@/lib/skins");
      await replaceSkinBytes(skin, await fileToBytes(f));
      await useSkins.getState().refresh();
      push({ kind: "success", title: "Skin image replaced" });
      onClose();
    } catch (e) {
      push({ kind: "error", title: "Couldn't replace skin", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose}>
      {skin && (
        <div key={key}>
          <h2 className="text-lg font-bold tracking-tight">Edit skin</h2>
          <p className="mt-0.5 text-sm text-ink-muted">Rename, change the arm model, or swap the image.</p>
          <div className="mt-4 space-y-4">
            <div>
              <label className="label" htmlFor="es-name">Name</label>
              <input
                id="es-name"
                className="input"
                defaultValue={skin.name}
                maxLength={40}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div>
              <span className="label">Arm model</span>
              <div className="grid grid-cols-2 gap-2">
                {(["classic", "slim"] as const).map((m) => (
                  <button
                    key={m}
                    onClick={() => setSkinModel(skin.id, m).catch(() => {})}
                    aria-pressed={skin.model === m}
                    className={cn(
                      "h-10 rounded-md border text-sm font-semibold capitalize transition active:scale-[0.98]",
                      skin.model === m
                        ? "border-accent-500/60 bg-accent-500/10 text-ink"
                        : "text-ink-muted hover:text-ink",
                    )}
                  >
                    {m === "classic" ? "Classic (4px)" : "Slim (3px)"}
                  </button>
                ))}
              </div>
            </div>
            <div className="flex gap-2">
              <label className="inline-flex h-10 flex-1 cursor-pointer items-center justify-center gap-2 rounded-md border text-sm font-semibold text-ink-muted transition hover:text-ink">
                <Upload className="size-4" /> Replace image (.png)
                <input type="file" accept="image/png" className="hidden" onChange={(e) => replace(e.target.files?.[0])} />
              </label>
              <Button
                variant="danger"
                onClick={async () => {
                  if (!confirm(`Delete “${skin.name}” forever?`)) return;
                  await removeSkin(skin.id);
                  push({ kind: "info", title: `Deleted “${skin.name}”` });
                  onDeleted?.();
                  onClose();
                }}
              >
                <Trash2 />
              </Button>
            </div>
            <Button variant="primary" className="w-full" onClick={save} disabled={busy}>
              {busy ? <Loader2 className="animate-spin" /> : null} Save changes
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
}
