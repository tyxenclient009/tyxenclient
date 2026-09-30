import { useEffect, useRef } from "react";
import { SkinViewer, WalkingAnimation } from "skinview3d";
import { httpsTexture, type SkinModel } from "@/lib/skins";

const toViewerModel = (m: SkinModel): "default" | "slim" => (m === "slim" ? "slim" : "default");

/**
 * True 3D player preview (three.js via skinview3d).
 * Drag to rotate (zoom/pan locked), slow walk animation, cape on the back.
 * Transparent background so it sits on any panel.
 */
export function SkinViewer3D({
  skinUrl,
  capeUrl,
  model = "classic",
  className,
}: {
  skinUrl: string | null;
  capeUrl?: string | null;
  model?: SkinModel;
  className?: string;
}) {
  const wrap = useRef<HTMLDivElement>(null);
  const canvas = useRef<HTMLCanvasElement>(null);
  const viewer = useRef<SkinViewer | null>(null);

  // Init once.
  useEffect(() => {
    const el = canvas.current;
    const box = wrap.current;
    if (!el || !box) return;
    const w = Math.max(80, box.clientWidth);
    const h = Math.max(160, box.clientHeight);
    let v: SkinViewer | null = null;
    try {
      v = new SkinViewer({ canvas: el, width: w, height: h, preserveDrawingBuffer: false });
    } catch {
      return;
    }
    viewer.current = v;
    v.controls.enableZoom = false;
    v.controls.enablePan = false;
    v.animation = new WalkingAnimation();
    v.animation.speed = 0.55;
    v.camera.position.set(0, 1, 42);

    const ro = new ResizeObserver(() => {
      if (!wrap.current || !viewer.current) return;
      viewer.current.setSize(
        Math.max(80, wrap.current.clientWidth),
        Math.max(160, wrap.current.clientHeight),
      );
    });
    ro.observe(box);
    return () => {
      ro.disconnect();
      v.dispose();
      viewer.current = null;
    };
  }, []);

  // Skin + model.
  useEffect(() => {
    const v = viewer.current;
    if (!v || v.disposed) return;
    try {
      const safe = httpsTexture(skinUrl) ?? null;
      if (safe) v.loadSkin(safe, { model: toViewerModel(model) });
      else v.loadSkin(null);
    } catch {
      /* a bad texture must never kill the preview */
    }
  }, [skinUrl, model]);

  // Cape.
  useEffect(() => {
    const v = viewer.current;
    if (!v || v.disposed) return;
    try {
      const safe = httpsTexture(capeUrl) ?? null;
      if (safe) v.loadCape(safe, { backEquipment: "cape" });
      else v.loadCape(null);
    } catch {
      /* ignore */
    }
  }, [capeUrl]);

  return (
    <div ref={wrap} className={className} style={{ minHeight: 160 }}>
      <canvas ref={canvas} style={{ width: "100%", height: "100%", display: "block", touchAction: "none" }} />
    </div>
  );
}
