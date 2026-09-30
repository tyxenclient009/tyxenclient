import { useEffect, useRef, useState } from "react";
import { ImagePlus, RotateCcw } from "lucide-react";
import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { useToasts } from "@/stores/app";
import { heroFileToDataUrl, heroFilter, heroImageSrc, useHero, type HeroSettings } from "@/stores/hero";

function Slider({
  label,
  value,
  min,
  max,
  step = 1,
  unit = "",
  onChange,
}: {
  label: string;
  value: number;
  min: number;
  max: number;
  step?: number;
  unit?: string;
  onChange: (v: number) => void;
}) {
  return (
    <div>
      <div className="flex items-center justify-between">
        <label className="label mb-0">{label}</label>
        <span className="font-mono text-xs text-accent-400">
          {value}
          {unit}
        </span>
      </div>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="mt-1 w-full accent-[#1ea86a]"
        aria-label={label}
      />
    </div>
  );
}

/**
 * Home banner editor — custom image + crop (drag / zoom / focal point) +
 * full adjustments (brightness, contrast, saturation, blur, grayscale).
 * Everything is plain English and previewed live.
 */
export function HeroEditor({ open, onClose }: { open: boolean; onClose: () => void }) {
  const saved = useHero();
  const push = useToasts((s) => s.push);
  const [draft, setDraft] = useState<HeroSettings>({
    image: saved.image,
    posX: saved.posX,
    posY: saved.posY,
    zoom: saved.zoom,
    brightness: saved.brightness,
    contrast: saved.contrast,
    saturate: saved.saturate,
    blur: saved.blur,
    grayscale: saved.grayscale,
  });
  const [busy, setBusy] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);
  const dragRef = useRef(false);

  // Refresh draft every time the editor opens.
  useEffect(() => {
    if (!open) return;
    const s = useHero.getState();
    setDraft({
      image: s.image,
      posX: s.posX,
      posY: s.posY,
      zoom: s.zoom,
      brightness: s.brightness,
      contrast: s.contrast,
      saturate: s.saturate,
      blur: s.blur,
      grayscale: s.grayscale,
    });
  }, [open]);

  const patch = (p: Partial<HeroSettings>) => setDraft((d) => ({ ...d, ...p }));

  const onPick = async (f: File | undefined) => {
    if (!f) return;
    setBusy(true);
    try {
      const url = await heroFileToDataUrl(f);
      patch({ image: url });
      push({ kind: "success", title: "Banner image updated", body: "Drag the preview to crop, then Save." });
    } catch (e) {
      push({ kind: "error", title: "Could not use that image", body: e instanceof Error ? e.message : String(e) });
    } finally {
      setBusy(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  };

  const onPreviewMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!dragRef.current) return;
    const r = e.currentTarget.getBoundingClientRect();
    const x = Math.round(((e.clientX - r.left) / r.width) * 100);
    const y = Math.round(((e.clientY - r.top) / r.height) * 100);
    patch({
      posX: Math.max(0, Math.min(100, x)),
      posY: Math.max(0, Math.min(100, y)),
    });
  };

  const save = () => {
    useHero.getState().set({ ...draft });
    push({ kind: "success", title: "Home banner saved" });
    onClose();
  };

  const resetAll = () => {
    useHero.getState().reset();
    const s = useHero.getState();
    setDraft({
      image: s.image,
      posX: s.posX,
      posY: s.posY,
      zoom: s.zoom,
      brightness: s.brightness,
      contrast: s.contrast,
      saturate: s.saturate,
      blur: s.blur,
      grayscale: s.grayscale,
    });
    push({ kind: "info", title: "Banner reset to default art" });
  };

  return (
    <Modal open={open} onClose={onClose} wide>
      <h2 className="text-lg font-bold tracking-tight">Edit home banner</h2>
      <p className="mt-0.5 text-sm text-ink-muted">
        Upload your own image, drag to crop, tune brightness, contrast and color.
      </p>

      {/* Live preview — drag to crop */}
      <div
        className="relative mt-4 cursor-crosshair overflow-hidden rounded-xl border border-line shadow-2"
        style={{ touchAction: "none" }}
        onPointerDown={(e) => {
          dragRef.current = true;
          (e.target as HTMLElement).setPointerCapture?.(e.pointerId);
          onPreviewMove(e);
        }}
        onPointerMove={onPreviewMove}
        onPointerUp={() => (dragRef.current = false)}
        onPointerLeave={() => (dragRef.current = false)}
        title="Drag to crop (sets focal point)"
        aria-label="Banner preview. Drag to crop."
      >
        <img
          src={heroImageSrc(draft)}
          alt="Banner preview"
          className="h-52 w-full object-cover md:h-60"
          style={{
            objectPosition: `${draft.posX}% ${draft.posY}%`,
            transform: `scale(${draft.zoom})`,
            filter: heroFilter(draft),
          }}
          draggable={false}
        />
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-black/70 via-black/25 to-transparent" />
        <div className="pointer-events-none absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-black/80 via-black/25 to-transparent" />
        <span className="pointer-events-none absolute bottom-2 right-3 rounded-full border border-white/20 bg-black/50 px-2.5 py-0.5 text-[11px] font-semibold text-white backdrop-blur-md">
          Drag image to crop
        </span>
      </div>

      {/* Upload / reset */}
      <div className="mt-3 flex flex-wrap gap-2">
        <Button variant="secondary" size="sm" onClick={() => fileRef.current?.click()} disabled={busy}>
          <ImagePlus /> {busy ? "Loading image" : draft.image ? "Change image" : "Upload image"}
        </Button>
        <Button variant="ghost" size="sm" onClick={resetAll}>
          <RotateCcw /> Reset to default
        </Button>
        <input
          ref={fileRef}
          type="file"
          accept="image/png,image/jpeg,image/webp"
          className="hidden"
          aria-label="Upload banner image"
          onChange={(e) => onPick(e.target.files?.[0])}
        />
      </div>

      {/* Crop */}
      <h3 className="mt-5 text-sm font-bold uppercase tracking-wider text-ink-muted">Crop</h3>
      <div className="mt-2 grid grid-cols-1 gap-3 sm:grid-cols-3">
        <Slider label="Zoom" value={Math.round(draft.zoom * 100)} min={100} max={200} unit="%" onChange={(v) => patch({ zoom: v / 100 })} />
        <Slider label="Position X" value={draft.posX} min={0} max={100} unit="%" onChange={(v) => patch({ posX: v })} />
        <Slider label="Position Y" value={draft.posY} min={0} max={100} unit="%" onChange={(v) => patch({ posY: v })} />
      </div>

      {/* Adjust */}
      <h3 className="mt-5 text-sm font-bold uppercase tracking-wider text-ink-muted">Adjust</h3>
      <div className="mt-2 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <Slider label="Brightness" value={draft.brightness} min={40} max={160} unit="%" onChange={(v) => patch({ brightness: v })} />
        <Slider label="Contrast" value={draft.contrast} min={40} max={160} unit="%" onChange={(v) => patch({ contrast: v })} />
        <Slider label="Saturation" value={draft.saturate} min={0} max={200} unit="%" onChange={(v) => patch({ saturate: v })} />
        <Slider label="Black and white" value={draft.grayscale} min={0} max={100} unit="%" onChange={(v) => patch({ grayscale: v })} />
        <Slider label="Blur" value={Math.round(draft.blur * 10) / 10} min={0} max={4} step={0.1} unit="px" onChange={(v) => patch({ blur: v })} />
      </div>

      <div className="mt-5 flex gap-2">
        <Button variant="secondary" className="flex-1" onClick={onClose}>
          Cancel
        </Button>
        <Button variant="primary" className="flex-1" onClick={save}>
          Save banner
        </Button>
      </div>
    </Modal>
  );
}
