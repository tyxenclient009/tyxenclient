import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Tauri v2: fixed dev port + strictPort so Rust side can reliably connect.
// `TAURI_DEV_HOST` support lets `tauri dev --host` work on LAN.
const host = process.env.TAURI_DEV_HOST;

export default defineConfig({
  plugins: [react()],
  clearScreen: false,
  define: {
    // Real version label for Settings (reads package.json via npm env).
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version ?? "0.1.0"),
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  server: {
    host: host || false,
    port: 1420,
    strictPort: true,
  },
  build: {
    // Tauri release profile expects `dist` by default (see tauri.conf.json).
    outDir: "dist",
    emptyOutDir: true,
    target: process.env.TAURI_ENV_PLATFORM === "windows" ? "chrome105" : "safari13",
    minify: !process.env.TAURI_ENV_DEBUG ? "esbuild" : false,
    sourcemap: !!process.env.TAURI_ENV_DEBUG,
  },
  // Prevent vite from obscuring Rust errors in dev.
  envPrefix: ["VITE_", "TAURI_"],
});
