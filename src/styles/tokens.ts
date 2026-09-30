/**
 * Design tokens — single source of truth mirrored in tailwind.config.js + globals.css.
 *
 * Why a TS mirror? So non-CSS code (canvas art, charts, dynamic styles) can
 * import the exact same palette instead of hardcoding hex values.
 */
export const tokens = {
  radius: { xs: 6, sm: 8, md: 12, lg: 16, xl: 20 },
  spacingGrid: 8,
  accent: { 300: "#7ee2a8", 400: "#4fd68a", 500: "#1ea86a", 600: "#178a57" },
  // Soft surfaces — never pure black/white (see globals.css for rgb channels).
  dark: { bg: "#0e1210", panel: "#151b18", raised: "#1c2420", line: "#26302b" },
  light: { bg: "#f4f5f3", panel: "#ffffff", raised: "#eef1ee", line: "#e2e7e3" },
} as const;

export type ThemeMode = "dark" | "light" | "system";
