import { useEffect, useRef } from "react";
import { httpsTexture, type SkinModel } from "@/lib/skins";

const SCALE = 4;

type Face = [number, number, number, number];

/**
 * 2D player preview (front + back) drawn straight from the skin PNG —
 * head, torso, arms, legs, overlay layer included. Slim arms render 3px.
 */
export function SkinPreview({ src, model, className }: { src: string; model: SkinModel; className?: string }) {
  const ref = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    let live = true;
    const canvas = ref.current;
    if (!canvas) return;
    const img = new Image();
    img.onload = () => {
      if (!live || !ref.current) return;
      const ctx = ref.current.getContext("2d");
      if (!ctx) return;
      const legacy = img.height === 32;
      const slim = model === "slim";
      const armW = slim ? 3 : 4;

      const aF: Face = slim ? [44, 20, 3, 12] : [44, 20, 4, 12];
      const aF2: Face = slim ? [44, 36, 3, 12] : [44, 36, 4, 12];
      const aB: Face = slim ? [51, 20, 3, 12] : [52, 20, 4, 12];
      const aB2: Face = slim ? [51, 36, 3, 12] : [52, 36, 4, 12];

      const front = {
        head: [8, 8, 8, 8] as Face, headO: [40, 8, 8, 8] as Face,
        torso: [20, 20, 8, 12] as Face, torsoO: [20, 36, 8, 12] as Face,
        arm: aF, armO: aF2,
        leg: [4, 20, 4, 12] as Face, legO: [4, 36, 4, 12] as Face,
      };
      const back = {
        head: [24, 8, 8, 8] as Face, headO: [56, 8, 8, 8] as Face,
        torso: [32, 20, 8, 12] as Face, torsoO: [32, 36, 8, 12] as Face,
        arm: aB, armO: aB2,
        leg: [12, 20, 4, 12] as Face, legO: [12, 36, 4, 12] as Face,
      };

      const figW = armW + 8 + armW;
      const gap = 6;
      const W = figW * 2 + gap;
      const H = 8 + 1 + 12 + 1 + 12;
      ref.current.width = W * SCALE;
      ref.current.height = H * SCALE;
      ctx.imageSmoothingEnabled = false;
      ctx.clearRect(0, 0, ref.current.width, ref.current.height);

      const draw = (f: Face, dx: number, dy: number) => {
        ctx.drawImage(img, f[0], f[1], f[2], f[3], dx * SCALE, dy * SCALE, f[2] * SCALE, f[3] * SCALE);
      };
      const figure = (ox: number, s: typeof front) => {
        draw(s.head, ox + armW, 0);
        if (!legacy) draw(s.headO, ox + armW, 0);
        draw(s.torso, ox + armW, 9);
        if (!legacy) draw(s.torsoO, ox + armW, 9);
        draw(s.arm, ox, 9);
        if (!legacy) draw(s.armO, ox, 9);
        draw(s.arm, ox + armW + 8, 9);
        if (!legacy) draw(s.armO, ox + armW + 8, 9);
        draw(s.leg, ox + armW, 22);
        if (!legacy) draw(s.legO, ox + armW, 22);
        draw(s.leg, ox + armW + 4, 22);
        if (!legacy) draw(s.legO, ox + armW + 4, 22);
      };

      figure(0, front);
      figure(figW + gap, back);
    };
    img.onerror = () => {};
    img.src = httpsTexture(src) ?? src;
    return () => {
      live = false;
    };
  }, [src, model]);

  return <canvas ref={ref} className={className} style={{ imageRendering: "pixelated" }} />;
}
