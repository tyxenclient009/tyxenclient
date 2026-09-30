import { Component, type ReactNode } from "react";
import { Button } from "./Button";
import { CrashIcon } from "@/components/icons/brand";

interface Props {
  /** Remount key — AppShell passes the route so errors reset on navigation. */
  resetKey?: string;
  children: ReactNode;
}

interface State {
  error: Error | null;
}

/**
 * Page-level error boundary: if any page throws during render, show a
 * recoverable error card INSTEAD of unmounting the whole app (which leaves
 * a blank white/transparent window with zero explanation).
 */
export class PageErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: { componentStack?: string }) {
    console.error("[tyx] page crash:", error, info.componentStack);
  }

  componentDidUpdate(prev: Props) {
    if (prev.resetKey !== this.props.resetKey && this.state.error) {
      this.setState({ error: null });
    }
  }

  render() {
    if (this.state.error) {
      return (
        <div className="mx-auto max-w-lg py-16 text-center">
          <div className="panel p-8">
            <div className="mx-auto flex size-12 items-center justify-center rounded-xl border border-red-500/30 bg-red-500/10">
              <CrashIcon className="size-6 text-red-400" />
            </div>
            <h2 className="mt-3 text-lg font-bold">This page crashed</h2>
            <p className="mt-1 font-mono text-xs text-ink-muted">
              {this.state.error.message || String(this.state.error)}
            </p>
            <div className="mt-4 flex justify-center gap-2">
              <Button variant="primary" size="sm" onClick={() => this.setState({ error: null })}>
                Try again
              </Button>
              <Button variant="secondary" size="sm" onClick={() => window.location.reload()}>
                Reload app
              </Button>
            </div>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
