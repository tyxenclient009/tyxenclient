import { useEffect, useState, type ReactNode } from "react";
import { createPortal } from "react-dom";

/**
 * Render overlay markup directly under <body> so `fixed` always means the
 * viewport. AppShell's glass `<main>` carries `backdrop-filter`, which per
 * CSS spec becomes the containing block for `fixed` descendants — dialogs
 * rendered inside a page would then open at the scrolled content's top and
 * scroll away with it instead of staying centered on screen.
 */
export function OverlayPortal({ children }: { children: ReactNode }) {
  const [el, setEl] = useState<HTMLElement | null>(null);
  useEffect(() => {
    const div = document.createElement("div");
    div.setAttribute("data-overlay", "");
    document.body.appendChild(div);
    setEl(div);
    return () => {
      document.body.removeChild(div);
    };
  }, []);
  if (!el) return null;
  return createPortal(children, el);
}
