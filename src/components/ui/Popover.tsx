import * as RadixPopover from "@radix-ui/react-popover";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";

/**
 * App popover primitive (Radix-based).
 *
 * Why Radix instead of the old absolutely-positioned div:
 * - Renders in a portal with its own stacking context (no bleed-through,
 *   never collides with sidebar nav text)
 * - Outside-click / Escape / focus-management handled + accessible
 * - Collision-aware placement (flips/stays in view near screen edges)
 *
 * Structure note: Radix owns the OUTER positioned shell; Framer animates an
 * INNER panel. This avoids the classic conflict where motion's inline
 * `transform` would clobber Radix's positioning.
 *
 * Theme: solid opaque panel + border + soft shadow. No backdrop-blur
 * transparency bugs — the surface is intentionally solid.
 */
export function Popover({
  open,
  onOpenChange,
  children,
}: {
  open?: boolean;
  onOpenChange?: (o: boolean) => void;
  children: React.ReactNode;
}) {
  return (
    <RadixPopover.Root open={open} onOpenChange={onOpenChange}>
      {children}
    </RadixPopover.Root>
  );
}

export function PopoverTrigger({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <RadixPopover.Trigger asChild className={className}>
      {children}
    </RadixPopover.Trigger>
  );
}

export function PopoverPanel({
  children,
  className,
  side = "top",
  align = "start",
  sideOffset = 10,
  alignOffset = 0,
}: {
  children: React.ReactNode;
  className?: string;
  side?: "top" | "right" | "bottom" | "left";
  align?: "start" | "center" | "end";
  sideOffset?: number;
  alignOffset?: number;
}) {
  return (
    <RadixPopover.Portal>
      <RadixPopover.Content
        side={side}
        align={align}
        sideOffset={sideOffset}
        alignOffset={alignOffset}
        collisionPadding={12}
        avoidCollisions
        className="z-[95] outline-none"
      >
        <motion.div
          initial={{ opacity: 0, scale: 0.96, y: 6 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.15, ease: "easeOut" }}
          className={cn(
            // Solid opaque surface — readable over ANY background.
            // Default w-80 (simple menus); callers pass w-96 for detail
            // panels. Width is ALWAYS independent of the trigger/parent.
            "w-80 rounded-xl border border-white/10 bg-surface-100 p-4",
            "shadow-2xl shadow-black/50 outline-none",
            "dark:border-white/10 dark:bg-[#151b18]",
            className,
          )}
        >
          {children}
        </motion.div>
      </RadixPopover.Content>
    </RadixPopover.Portal>
  );
}

export const PopoverClose = RadixPopover.Close;
