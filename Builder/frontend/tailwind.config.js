/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#0f172a',
          light: '#1e293b',
          dark: '#020617',
        },
        accent: {
          DEFAULT: '#f97316',
          glow: 'rgba(249, 115, 22, 0.4)',
          cyan: '#06b6d4',
          purple: '#8b5cf6',
          lime: '#84cc16',
          pink: '#ec4899',
          blue: '#3b82f6',
          emerald: '#10b981',
        },
        surface: {
          DEFAULT: 'rgba(255, 255, 255, 0.03)',
          hover: 'rgba(255, 255, 255, 0.06)',
          dark: 'rgba(15, 23, 42, 0.8)',
          glass: 'rgba(255, 255, 255, 0.85)',
        },
      },
      fontFamily: {
        sans: ['Noto Sans SC', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['Outfit', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
      },
      animation: {
        float: 'float 25s ease-in-out infinite',
        fadeIn: 'fadeIn 0.6s ease-out both',
        fadeInUp: 'fadeInUp 0.8s ease-out both',
        fadeInDown: 'fadeInDown 0.8s ease-out both',
        fadeInLeft: 'fadeInLeft 0.8s ease-out both',
        pulse: 'pulse 2s ease-in-out infinite',
        rotate: 'rotate 30s linear infinite',
        spin: 'spin 0.8s linear infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0) rotate(0deg)', opacity: '0.2' },
          '50%': { transform: 'translateY(-40px) rotate(8deg)', opacity: '0.4' },
        },
        fadeIn: {
          from: { opacity: '0', transform: 'translateY(10px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        fadeInUp: {
          from: { opacity: '0', transform: 'translateY(30px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        fadeInDown: {
          from: { opacity: '0', transform: 'translateY(-30px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        fadeInLeft: {
          from: { opacity: '0', transform: 'translateX(-30px)' },
          to: { opacity: '1', transform: 'translateX(0)' },
        },
        pulse: {
          '0%, 100%': { opacity: '1', transform: 'scale(1)' },
          '50%': { opacity: '0.5', transform: 'scale(1.2)' },
        },
        rotate: {
          to: { transform: 'rotate(360deg)' },
        },
        spin: {
          to: { transform: 'rotate(360deg)' },
        },
      },
      backdropBlur: {
        xs: '2px',
      },
      boxShadow: {
        'glow-orange': '0 8px 16px -4px rgba(249, 115, 22, 0.4)',
        'glow-cyan': '0 8px 16px -4px rgba(6, 182, 212, 0.4)',
        'glow-purple': '0 8px 16px -4px rgba(139, 92, 246, 0.4)',
      },
    },
  },
  plugins: [],
};
