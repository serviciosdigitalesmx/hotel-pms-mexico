/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f7f7ff',
          100: '#efefff',
          200: '#dcdcfe',
          300: '#b9b7fb',
          400: '#9290f2',
          500: '#6f6de5',
          600: '#5a58d6',
          700: '#4542bc',
          800: '#39369a',
          900: '#302d7b',
        },
      },
      fontFamily: {
        sans: ['Inter', 'Segoe UI Variable', 'Segoe UI', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['Inter', 'Segoe UI Variable', 'Segoe UI', 'system-ui', 'sans-serif'],
        mono: ['DM Mono', 'Cascadia Code', 'SFMono-Regular', 'Consolas', 'monospace'],
      },
      boxShadow: {
        panel: '0 1px 2px rgba(15, 32, 39, 0.03), 0 12px 36px rgba(15, 32, 39, 0.055)',
        float: '0 20px 60px rgba(10, 26, 33, 0.16)',
      },
    },
  },
  plugins: [],
}
