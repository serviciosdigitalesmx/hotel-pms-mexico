import path from "path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vitest/config"

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': {
        target: process.env.FIXI_API_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  define: {
    'import.meta.env.VITE_BASENAME': JSON.stringify(process.env.VITE_BASENAME || ''),
  },
  test: {
    globals: true,
    environment: 'jsdom',
    // axe checks and async UI flows exceed Vitest's 5s default under CI load.
    testTimeout: 15_000,
    setupFiles: ['./src/setupTests.ts'],
    css: true,
    coverage: { provider: 'v8', reporter: ['text', 'lcov'] },
  },
})
