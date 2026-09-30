import { useState } from "react";
import { MotionConfig } from "framer-motion";
import { AppShell } from "@/components/shell/AppShell";
import { IntroVideo } from "@/components/shell/IntroVideo";
import { cn } from "@/lib/utils";

/** Root — startup cinema first, then the dashboard fades in underneath. */
export default function App() {
  const [introDone, setIntroDone] = useState(false);

  return (
    <MotionConfig reducedMotion="user">
      <div className="h-screen w-screen p-0">
        <div className={cn("h-full w-full transition-opacity duration-700", introDone ? "opacity-100" : "opacity-0")}>
          <AppShell />
        </div>
        {!introDone && <IntroVideo onDone={() => setIntroDone(true)} />}
      </div>
    </MotionConfig>
  );
}
