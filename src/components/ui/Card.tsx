import { motion } from "framer-motion";
import { cn } from "@/lib/utils";
import type { HTMLAttributes } from "react";

/** Glassy card — signature launcher surface (12–16px radius, soft shadow). */
export function Card({ className, ...rest }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("panel p-4", className)} {...rest} />;
}

/** Hoverable card — 3D depth (brief §2): lift + scale + border glow. */
export function HoverCard({
  className,
  children,
  onClick,
  ariaLabel,
}: {
  className?: string;
  children?: React.ReactNode;
  onClick?: () => void;
  ariaLabel?: string;
}) {
  return (
    <motion.div
      whileHover={{ y: -4, scale: 1.01 }}
      whileTap={{ scale: 0.99 }}
      transition={{ type: "spring", stiffness: 400, damping: 28 }}
      onClick={onClick}
      onKeyDown={(event) => {
        // The card contains secondary buttons (Play, Manage, Delete). Only
        // activate the card when the card itself owns the keyboard event.
        if (event.target !== event.currentTarget) return;
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          onClick?.();
        }
      }}
      role={onClick ? "button" : undefined}
      tabIndex={onClick ? 0 : undefined}
      aria-label={ariaLabel}
      className={cn(
        "panel group relative cursor-pointer overflow-hidden p-4 transition-colors duration-150", 
        "hover:border-accent-500/50 hover:shadow-glow",
        // Sheen sweep on hover for glass depth.
        "after:pointer-events-none after:absolute after:inset-0 after:translate-x-[-100%] after:bg-gradient-to-r after:from-transparent after:via-white/[0.06] after:to-transparent after:transition-transform after:duration-700 hover:after:translate-x-[100%]",
        className,
      )}
    >
      {children}
    </motion.div>
  );
}

/** Small pill badge for versions / loaders / status. */
export function Badge({
  children,
  tone = "neutral",
  className,
}: {
  children: React.ReactNode;
  tone?: "neutral" | "accent" | "warn" | "danger";
  className?: string;
}) {
  const tones: Record<string, string> = {
    neutral: "bg-surface-200/80 text-ink-muted border",
    accent: "bg-accent-500/15 text-accent-400 border-accent-500/30 border",
    warn: "bg-amber-500/15 text-amber-400 border-amber-500/30 border",
    danger: "bg-red-500/15 text-red-400 border-red-500/30 border",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-semibold tracking-wide",
        tones[tone],
        className,
      )}
    >
      {children}
    </span>
  );
}
