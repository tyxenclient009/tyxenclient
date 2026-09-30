import { useCallback, useEffect, useRef, useState } from "react";
import { Volume2, VolumeX } from "lucide-react";
import { TyxLogo } from "@/components/icons/brand";
import { cn } from "@/lib/utils";

const FADE_MS = 2000;
// If the file is missing/broken the WebView may sit on HAVE_NOTHING
// forever without firing `error` — never trap the user behind black.
const STUCK_TIMEOUT_MS = 6000;
// A broken video still shows a branded splash for this long (skippable),
// so a missing codec/file is visible instead of a "jump to dashboard".
const FALLBACK_SPLASH_MS = 2500;

/**
 * Startup cinema: fullscreen video on launch, fades out over its last
 * 2 seconds (or on click/Esc/Skip), then the dashboard fades in underneath.
 *
 * Autoplay-safe order for WebView2/Chromium policy:
 * 1. Render muted + playsInline so `play()` is never blocked.
 * 2. Once `canplay`, attempt ONE unmute (sound-attempt).
 * 3. If blocked, stay muted + show the "Tap for sound" pill.
 *
 * The video ALWAYS plays (no reduced-motion skip — this is the launcher's
 * branded opener and the user explicitly requires it). Anyone can still
 * skip instantly via click / Skip button / Esc.
 * Broken file or undecodable codec → branded fallback splash for a moment,
 * never an instant cut to the dashboard and never a black trap.
 */
export function IntroVideo({ onDone }: { onDone: () => void }) {
  const video = useRef<HTMLVideoElement>(null);
  const [fading, setFading] = useState(false);
  // null = sound playing/attempting, true = blocked → show the pill.
  const [soundBlocked, setSoundBlocked] = useState(false);
  const [muted, setMuted] = useState(true);
  // True once the video element reports it cannot play the file.
  const [failed, setFailed] = useState(false);
  const done = useRef(false);
  const onDoneRef = useRef(onDone);
  onDoneRef.current = onDone;

  const finish = useCallback((fast: boolean) => {
    if (done.current) return;
    done.current = true;
    setFading(true);
    // Unmount right after the fade completes.
    setTimeout(() => onDoneRef.current(), fast ? 350 : FADE_MS + 150);
  }, []);

  useEffect(() => {
    let cancelled = false;
    const finishFast = () => finish(true);

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") finishFast();
    };
    window.addEventListener("keydown", onKeyDown);

    const v = video.current;
    if (!v) {
      window.removeEventListener("keydown", onKeyDown);
      return;
    }

    const onTime = () => {
      if (v.duration && v.duration - v.currentTime <= FADE_MS / 1000) finish(false);
    };
    const onEnd = () => finish(false);
    // Broken file / undecodable codec: show the branded fallback splash
    // instead of cutting straight to the dashboard.
    const onErr = () => {
      if (done.current || cancelled) return;
      setFailed(true);
    };
    // Sound-attempt: once we have frames, try unmuted. Blocked → stay
    // muted + offer the pill. Success → sound on, no pill.
    const onCanPlay = () => {
      if (done.current || cancelled) return;
      v.muted = false;
      v.play()
        .then(() => {
          if (!cancelled && !done.current) {
            setMuted(false);
            setSoundBlocked(false);
          }
        })
        .catch(() => {
          if (cancelled || done.current) return;
          v.muted = true;
          setMuted(true);
          setSoundBlocked(true);
          v.play().catch(() => {});
        });
    };

    v.addEventListener("timeupdate", onTime);
    v.addEventListener("ended", onEnd);
    v.addEventListener("error", onErr);
    v.addEventListener("canplay", onCanPlay);

    // Start muted so autoplay is allowed everywhere (WebView2 included).
    // Always the full cinema — no reduced-motion skip (user requirement);
    // click / Skip / Esc still skips instantly for anyone who wants out.
    v.muted = true;
    v.play().catch(() => {
      // First kick rejected (not ready yet) — `canplay` retries with sound.
      // If neither canplay nor error ever arrives, the stuck-timer moves on.
    });
    const stuck = window.setTimeout(() => {
      if (!done.current && !cancelled && v.readyState < 2 && !failed) onDoneRef.current();
    }, STUCK_TIMEOUT_MS);

    return () => {
      cancelled = true;
      window.clearTimeout(stuck);
      window.removeEventListener("keydown", onKeyDown);
      v.removeEventListener("timeupdate", onTime);
      v.removeEventListener("ended", onEnd);
      v.removeEventListener("error", onErr);
      v.removeEventListener("canplay", onCanPlay);
      try {
        v.pause();
      } catch {
        /* ignore */
      }
    };
  }, [finish]);

  const toggleSound = (e: React.MouseEvent) => {
    e.stopPropagation(); // don't skip the intro
    const v = video.current;
    if (!v) return;
    v.muted = !v.muted;
    setMuted(v.muted);
    if (!v.muted) {
      setSoundBlocked(false);
      v.play().catch(() => {});
    }
  };

  // Broken video → branded splash holds for a moment (still skippable),
  // then fades to the dashboard. Never an instant cut.
  useEffect(() => {
    if (!failed) return;
    const t = window.setTimeout(() => finish(true), FALLBACK_SPLASH_MS);
    return () => window.clearTimeout(t);
  }, [failed, finish]);

  return (
    <div
      className={cn(
        "fixed inset-0 z-[200] flex items-center justify-center bg-black transition-opacity",
        fading ? "opacity-0" : "opacity-100",
      )}
      style={{ transitionDuration: `${fading ? FADE_MS : 300}ms` }}
      onClick={() => finish(true)}
    >
      {failed ? (
        <div className="flex flex-col items-center gap-4 px-8 text-center">
          <TyxLogo className="size-20 rounded-2xl shadow-glow" />
          <div>
            <p className="text-xl font-bold tracking-tight text-white">Tyxen Launcher</p>
            <p className="mt-1 text-[13px] text-white/60">Preparing your launcher…</p>
          </div>
          <div className="h-1 w-44 overflow-hidden rounded-full bg-white/10">
            <div className="hero-gradient-bar h-full w-1/2 animate-pulse rounded-full" />
          </div>
        </div>
      ) : (
        <video
          ref={video}
          className="h-full w-full object-cover"
          playsInline
          autoPlay
          muted
          preload="auto"
          disablePictureInPicture
        >
          {/* Explicit MIME helps WebView2 pick the decoder; absolute path
              resolves to dist/start.mp4 in Tauri and public/ in vite dev. */}
          <source src="/start.mp4" type="video/mp4" />
        </video>
      )}
      {soundBlocked && (
        <button
          onClick={toggleSound}
          className="absolute bottom-5 left-6 flex items-center gap-2 rounded-full border border-white/20 bg-black/60 px-4 py-2 text-[13px] font-semibold text-white backdrop-blur-md transition hover:bg-black/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-500/60"
        >
          {muted ? <VolumeX className="size-4" /> : <Volume2 className="size-4" />}
          <span className="whitespace-nowrap">{muted ? "Tap for sound" : "Sound on"}</span>
        </button>
      )}
      <button
        type="button"
        onClick={() => finish(true)}
        className="absolute bottom-5 right-6 rounded-full border border-white/15 bg-black/35 px-3 py-1.5 text-xs font-semibold tracking-wide text-white/75 backdrop-blur-md transition hover:bg-black/60 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-400"
      >
        Skip intro <span aria-hidden>·</span> Esc
      </button>
    </div>
  );
}
