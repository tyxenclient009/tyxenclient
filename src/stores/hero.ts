import { create } from "zustand";
import { persist } from "zustand/middleware";

export const DEFAULT_HERO_IMAGE = "/bgwall.png";

export interface HeroSettings {
  /** Custom data-URL or default path. Empty = default art. */
  image: string;
  /** Crop: focal point in percent (object-position). */
  posX: number;
  posY: number;
  /** Crop zoom: 1.0 = fill, up to 2.0 */
  zoom: number;
  /** Filters (CSS). */
  brightness: number;
  contrast: number;
  saturate: number;
  blur: number;
  grayscale: number;
}

interface HeroState extends HeroSettings {
  set: (p: Partial<HeroSettings>) => void;
  reset: () => void;
}

const DEFAULTS: HeroSettings = {
  image: "",
  posX: 50,
  posY: 15,
  zoom: 1.06,
  brightness: 100,
  contrast: 100,
  saturate: 100,
  blur: 0,
  grayscale: 0,
};

export const useHero = create<HeroState>()(
  persist(
    (set) => ({
      ...DEFAULTS,
      set: (p) => set(p),
      reset: () => set({ ...DEFAULTS }),
    }),
    { name: "tyx:hero-banner" },
  ),
);

/** Resolved image src (custom upload or bundled default). */
export function heroImageSrc(s: HeroSettings): string {
  return s.image || DEFAULT_HERO_IMAGE;
}

/** CSS filter string for the hero image. */
export function heroFilter(s: HeroSettings): string {
  const parts = [
    `brightness(${s.brightness / 100})`,
    `contrast(${s.contrast / 100})`,
    `saturate(${s.saturate / 100})`,
  ];
  if (s.grayscale > 0) parts.push(`grayscale(${s.grayscale / 100})`);
  if (s.blur > 0) parts.push(`blur(${s.blur}px)`);
  return parts.join(" ");
}

/**
 * Read an image file, downscale to max 1600px wide (keeps localStorage
 * small) and return a JPEG/PNG data-URL. Throws in plain English.
 */
export async function heroFileToDataUrl(file: File): Promise<string> {
  if (!/^image\/(png|jpe?g|webp)$/i.test(file.type)) {
    throw new Error("Please choose a PNG, JPG or WebP image.");
  }
  if (file.size > 8 * 1024 * 1024) {
    throw new Error("Image is too large (max 8 MB).");
  }
  const bitmap = await createImageBitmap(file).catch(() => null);
  const url = URL.createObjectURL(file);
  let secondUrl: string | null = null;
  try {
    const img = await new Promise<HTMLImageElement>((resolve, reject) => {
      const el = new Image();
      el.onload = () => resolve(el);
      el.onerror = () => reject(new Error("Could not read that image file."));
      el.src = bitmap ? (secondUrl = URL.createObjectURL(file)) : url;
    });
    const MAX_W = 1600;
    const scale = Math.min(1, MAX_W / img.width);
    const w = Math.max(1, Math.round(img.width * scale));
    const h = Math.max(1, Math.round(img.height * scale));
    const canvas = document.createElement("canvas");
    canvas.width = w;
    canvas.height = h;
    const ctx = canvas.getContext("2d");
    if (!ctx) throw new Error("Could not process that image.");
    ctx.drawImage(img, 0, 0, w, h);
    // JPEG for photos (small), PNG fallback keeps transparency.
    const out =
      file.type === "image/png" ? canvas.toDataURL("image/png") : canvas.toDataURL("image/jpeg", 0.86);
    if (out.length > 4_500_000) {
      throw new Error("That image is too detailed to store — try a smaller file.");
    }
    return out;
  } finally {
    URL.revokeObjectURL(url);
    if (secondUrl) URL.revokeObjectURL(secondUrl);
    if (bitmap) bitmap.close?.();
  }
}
