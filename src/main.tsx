import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { ConsoleWindow } from "./components/launch/ConsoleWindow";
import "@/styles/globals.css";
import { initTheme } from "@/stores/app";

// Apply persisted theme before first paint to avoid a light-flash.
initTheme();

// `?console` boots the standalone log window (own Tauri WebviewWindow);
// everything else boots the main launcher shell.
const isConsole = new URLSearchParams(window.location.search).has("console");

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>{isConsole ? <ConsoleWindow /> : <App />}</React.StrictMode>,
);
