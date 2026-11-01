/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f0effa',
          100: '#dedaf4',
          200: '#c4bce9',
          300: '#a396dc',
          400: '#7c6aca',
          500: '#6556b6',
          600: '#5145a8',
          700: '#42378f',
          800: '#383078',
          900: '#29235c',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
