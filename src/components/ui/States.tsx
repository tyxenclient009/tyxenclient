import { Button } from "./Button";

/** Empty state — icon, title, hint, optional action. Never a blank screen. */
export function EmptyState({
  icon: Icon,
  title,
  hint,
  actionLabel,
  onAction,
}: {
  icon: React.ComponentType<{ className?: string }>;
  title: string;
  hint: string;
  actionLabel?: string;
  onAction?: () => void;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed py-16 text-center">
      <div className="flex size-12 items-center justify-center rounded-xl border border-accent-500/25 bg-accent-500/10 shadow-glow">
        <Icon className="size-6 text-accent-400" />
      </div>
      <h3 className="mt-4 text-[15px] font-semibold">{title}</h3>
      <p className="mt-1 max-w-sm text-sm text-ink-muted">{hint}</p>
      {actionLabel && (
        <Button variant="primary" size="sm" className="mt-4" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
}

/** Error state — same slot as empty state, red-tinted with retry. */
export function ErrorState({
  title = "Something went wrong",
  hint = "Check your connection and try again.",
  onRetry,
}: {
  title?: string;
  hint?: string;
  onRetry?: () => void;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-red-500/25 bg-red-500/5 py-16 text-center">
      <h3 className="text-[15px] font-semibold text-red-400">{title}</h3>
      <p className="mt-1 max-w-sm text-sm text-ink-muted">{hint}</p>
      {onRetry && (
        <Button variant="secondary" size="sm" className="mt-4" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  );
}
