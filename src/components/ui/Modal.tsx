import { AnimatePresence, motion } from "framer-motion";
import { X } from "lucide-react";
import { useEffect, useRef } from "react";
import { useScrollLock } from "@/lib/scroll-lock";
import { OverlayPortal } from "./Portal";

/** Generic modal — blurred backdrop, spring panel, Esc to close. */
export function Modal({
  open,
  onClose,
  children,
  wide,
}: {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  wide?: boolean;
}) {
  const dialogRef = useRef<HTMLDivElement>(null);
  const restoreFocusRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!open) return;
    restoreFocusRef.current = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    const dialog = dialogRef.current;
    const focusable = dialog?.querySelector<HTMLElement>(
      'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
    );
    // Move focus into the dialog and return it to the launching control when
    // the dialog closes. This keeps keyboard users out of the page behind it.
    queueMicrotask(() => (focusable ?? dialog)?.focus());

    const h = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        e.preventDefault();
        onClose();
        return;
      }
      if (e.key !== "Tab" || !dialog) return;
      const nodes = Array.from(dialog.querySelectorAll<HTMLElement>(
        'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      ));
      if (nodes.length === 0) {
        e.preventDefault();
        dialog.focus();
        return;
      }
      const first = nodes[0];
      const last = nodes[nodes.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    };
    window.addEventListener("keydown", h);
    return () => {
      window.removeEventListener("keydown", h);
      restoreFocusRef.current?.focus();
      restoreFocusRef.current = null;
    };
  }, [open, onClose]);
  // Background must not scroll behind the dialog (covers every Modal user).
  useScrollLock(open);

  return (
    <OverlayPortal>
      <AnimatePresence>
        {open && (
          <motion.div
            className="fixed inset-0 z-[80] flex items-center justify-center bg-black/60 p-4 backdrop-blur-md"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.18 }}
            onClick={onClose}
          >
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 12 }}
            transition={{ duration: 0.15, ease: "easeOut" }}
            onClick={(e) => e.stopPropagation()}
            ref={dialogRef}
            role="dialog"
            aria-modal="true"
            aria-label="Tyxen Launcher dialog"
            tabIndex={-1}
            className={`panel-raised max-h-[86vh] w-full ${wide ? "max-w-3xl" : "max-w-lg"} overflow-y-auto border-white/10 p-6 shadow-3`}
          >
            <button
              onClick={onClose}
              aria-label="Close"
              className="float-right rounded-md p-1.5 text-ink-faint hover:bg-surface-300 hover:text-ink"
            >
              <X className="size-4" />
            </button>
              {children}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </OverlayPortal>
  );
}
