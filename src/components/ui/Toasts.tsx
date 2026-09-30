import { AnimatePresence, motion } from "framer-motion";
import { CheckCircle2, Info, X, XCircle } from "lucide-react";
import { useToasts } from "@/stores/app";
import { cn } from "@/lib/utils";

const icons = { success: CheckCircle2, error: XCircle, info: Info };

/** Bottom-right glass toast stack (brief §Toasts): slide-in/out + accent icon. */
export function Toasts() {
  const { toasts, dismiss } = useToasts();
  return (
    <div className="pointer-events-none fixed bottom-5 right-5 z-[100] flex w-[340px] flex-col gap-2" role="status" aria-live="polite">
      <AnimatePresence>
        {toasts.map((t) => {
          const Icon = icons[t.kind];
          return (
            <motion.div
              key={t.id}
              layout
              initial={{ opacity: 0, x: 48, scale: 0.96 }}
              animate={{ opacity: 1, x: 0, scale: 1 }}
              exit={{ opacity: 0, x: 48, scale: 0.96 }}
              transition={{ type: "spring", stiffness: 420, damping: 32 }}
              className={cn(
                "panel-raised pointer-events-auto flex items-start gap-3 border-white/10 p-3.5 backdrop-blur-xl",
                t.kind === "error" && "border-red-500/40",
                t.kind === "success" && "border-accent-500/40",
              )}
            >
              <Icon
                className={cn(
                  "mt-0.5 size-5 shrink-0",
                  t.kind === "success" && "text-accent-400",
                  t.kind === "error" && "text-red-400",
                  t.kind === "info" && "text-ink-muted",
                )}
              />
              <div className="min-w-0 flex-1">
                <p className="text-sm font-semibold leading-tight">{t.title}</p>
                {t.body && <p className="mt-0.5 text-[13px] leading-snug text-ink-muted">{t.body}</p>}
              </div>
              <button
                onClick={() => dismiss(t.id)}
                className="rounded p-0.5 text-ink-faint hover:bg-surface-300 hover:text-ink"
                aria-label="Dismiss"
              >
                <X className="size-4" />
              </button>
            </motion.div>
          );
        })}
      </AnimatePresence>
    </div>
  );
}
