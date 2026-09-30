/** @type {import('tailwindcss').Config} */
export default {
  // Class-based dark mode so the theme toggle (and OS sync) is fully manual.
  // We toggle `dark` on <html> from the theme store.
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      // ── Design tokens (see src/styles/tokens.ts for the TS mirror) ──────
      colors: {
        // Surfaces — deep charcoal dark, warm paper light.
        surface: {
          50: "rgb(var(--surface-50) / <alpha-value>)",
          100: "rgb(var(--surface-100) / <alpha-value>)",
          200: "rgb(var(--surface-200) / <alpha-value>)",
          300: "rgb(var(--surface-300) / <alpha-value>)",
          400: "rgb(var(--surface-400) / <alpha-value>)",
        },
        ink: {
          DEFAULT: "rgb(var(--ink) / <alpha-value>)",
          muted: "rgb(var(--ink-muted) / <alpha-value>)",
          faint: "rgb(var(--ink-faint) / <alpha-value>)",
        },
        // Accent — Modrinth-like green. Used sparingly for primary actions.
        accent: {
          300: "rgb(var(--accent-300) / <alpha-value>)",
          400: "rgb(var(--accent-400) / <alpha-value>)",
          500: "rgb(var(--accent-500) / <alpha-value>)",
          600: "#178a57",
          DEFAULT: "rgb(var(--accent-500) / <alpha-value>)",
        },
        line: "rgb(var(--line) / <alpha-value>)",
      },
      borderRadius: {
        xs: "6px",
        sm: "8px",
        md: "12px",
        lg: "16px",
        xl: "20px",
      },
      boxShadow: {
        // Glass elevation: soft + inner highlight for frosted depth.
        1: "0 1px 2px rgb(0 0 0 / 0.08), 0 1px 3px rgb(0 0 0 / 0.1), inset 0 1px 0 rgb(255 255 255 / 0.06)",
        2: "0 4px 12px -2px rgb(0 0 0 / 0.16), 0 2px 6px -2px rgb(0 0 0 / 0.1), inset 0 1px 0 rgb(255 255 255 / 0.07)",
        3: "0 12px 32px -8px rgb(0 0 0 / 0.28), 0 4px 12px -4px rgb(0 0 0 / 0.16), inset 0 1px 0 rgb(255 255 255 / 0.08)",
        glow: "0 0 0 1px rgb(30 168 106 / 0.35), 0 8px 24px -6px rgb(30 168 106 / 0.45)",
        "glow-lg": "0 0 0 1px rgb(30 168 106 / 0.45), 0 12px 40px -8px rgb(30 168 106 / 0.5)",
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "Segoe UI", "Roboto", "sans-serif"],
        mono: ["JetBrains Mono", "ui-monospace", "SFMono-Regular", "Menlo", "monospace"],
      },
      keyframes: {
        "fade-up": {
          from: { opacity: "0", transform: "translateY(8px)" },
          to: { opacity: "1", transform: "translateY(0)" },
        },
        shimmer: {
          "100%": { transform: "translateX(100%)" },
        },
        "gradient-x": {
          "0%, 100%": { "background-position": "0% 50%" },
          "50%": { "background-position": "100% 50%" },
        },
        "heart-burst": {
          "0%": { transform: "scale(1)" },
          "40%": { transform: "scale(1.35)" },
          "70%": { transform: "scale(0.92)" },
          "100%": { transform: "scale(1)" },
        },
        "success-ping": {
          "0%": { transform: "scale(1)", opacity: "1" },
          "100%": { transform: "scale(1.6)", opacity: "0" },
        },
      },
      animation: {
        "fade-up": "fade-up 0.25s ease-out both",
        shimmer: "shimmer 1.6s infinite",
        "gradient-x": "gradient-x 6s ease infinite",
        "heart-burst": "heart-burst 0.35s ease-out",
        "success-ping": "success-ping 0.5s ease-out",
      },
    },
  },
  plugins: [],
};
