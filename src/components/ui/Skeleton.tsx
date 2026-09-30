import { cn } from "@/lib/utils";

/** Shimmer skeleton block for loading states (every screen needs one). */
export function Skeleton({ className }: { className?: string }) {
  return <div className={cn("skeleton-shimmer rounded-md bg-surface-200/80", className)} />;
}

/** Grid of placeholder cards while instances / mods load. */
export function CardGridSkeleton({ count = 6 }: { count?: number }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="panel p-4">
          <div className="flex items-center gap-3">
            <Skeleton className="size-11 rounded-lg" />
            <div className="flex-1 space-y-2">
              <Skeleton className="h-4 w-2/3" />
              <Skeleton className="h-3 w-1/3" />
            </div>
          </div>
          <Skeleton className="mt-4 h-9 w-full" />
        </div>
      ))}
    </div>
  );
}
