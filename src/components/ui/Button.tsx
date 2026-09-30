import { motion } from "framer-motion";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";
import type { ButtonHTMLAttributes } from "react";

const button = cva(
  // Brief §Radius + micro-interactions: 8px inputs / 12px buttons,
  // press scale, glow on primary, spinner overlay slot via relative.
  "inline-flex relative select-none items-center justify-center gap-2 overflow-hidden whitespace-nowrap rounded-md text-sm font-semibold transition-all duration-150 outline-none focus-visible:ring-2 focus-visible:ring-accent-500/50 active:scale-[0.97] disabled:pointer-events-none disabled:opacity-50 [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        // Primary: green gradient + glow (only Play / Install).
        primary:
          "bg-gradient-to-b from-accent-400 to-accent-500 text-white shadow-2 hover:shadow-glow hover:brightness-110",
        secondary: "border border-white/10 bg-surface-200/50 text-ink backdrop-blur-xl hover:bg-surface-300/70",
        ghost: "text-ink-muted hover:bg-surface-200/70 hover:text-ink hover:font-semibold",
        danger: "bg-red-500/10 text-red-400 border border-red-500/20 hover:bg-red-500/20",
      },
      size: {
        sm: "h-8 px-3 text-[13px]",
        md: "h-10 px-4",
        lg: "h-11 px-6 text-[15px]",
        icon: "h-9 w-9",
      },
    },
    defaultVariants: { variant: "secondary", size: "md" },
  },
);

type Props = ButtonHTMLAttributes<HTMLButtonElement> & VariantProps<typeof button>;

/** Product button — motion tap feedback baked in via CSS active:scale. */
export function Button({ variant, size, className, ...rest }: Props) {
  return <button className={cn(button({ variant, size }), className)} {...rest} />;
}

/** Motion wrapper for page-level entrance (fade+rise, Modrinth-like). */
export function FadeUp({
  children,
  delay = 0,
  className,
}: {
  children: React.ReactNode;
  delay?: number;
  className?: string;
}) {
  return (
    <motion.div
      className={className}
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25, delay, ease: "easeOut" }}
    >
      {children}
    </motion.div>
  );
}
