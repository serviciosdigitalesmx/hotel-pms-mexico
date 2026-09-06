/// <reference types="vitest" />
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import eslint from '@nabla/vite-plugin-eslint'
import path from 'path'

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
  // The ESLint Vite watcher keeps the dev server alive after Vitest completes.
  // Keep it enabled for dev/build, but not for the isolated test server.
  plugins: [react(), ...(mode === 'test' ? [] : [eslint()])],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    exclude: ['node_modules', 'dist', '.idea', '.git', '.cache', 'e2e/**', 'e2e-live/**'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      exclude: [
        'node_modules/**',
        'dist/**',
        'e2e/**',
        '**/*.config.*',
        '**/setupTests.*',
        '**/*.d.ts',
        // Bootstrap entry point — mounts React to the DOM, nothing to unit test.
        'src/main.tsx',
      ],
      thresholds: {
        // Keep thresholds aligned with the current Mexican operations surface.
        // The legacy fiscal/Alloggiati UI tests were retired with those UI
        // paths; the measured current baseline on 2026-09-03 is 78.28% branch
        // and 82.60% function coverage. App.test.tsx exercises the auth/routing
        // gate rather than duplicating every lazy-loaded page's own test suite.
        statements: 90,
        branches: 78,
        functions: 82,
        lines: 92,
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      // User management must go through the gateway (needs HMAC + role injection)
      '/api/v1/auth/users': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // Direct to auth-service for login/register/me (no gateway HMAC needed)
      '/api/v1/auth': {
        target: 'http://localhost:8087',
        changeOrigin: true
      },
      // All other API calls through the gateway
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}))
