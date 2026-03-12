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
          dark: '#ea580c',
          glow: 'rgba(249, 115, 22, 0.4)',
          cyan: '#06b6d4',
          cyanDark: '#0891b2',
          cyanGlow: 'rgba(6, 182, 212, 0.4)',
          purple: '#8b5cf6',
          purpleDark: '#7c3aed',
          purpleGlow: 'rgba(139, 92, 246, 0.4)',
          lime: '#84cc16',
          limeDark: '#65a30d',
          limeGlow: 'rgba(132, 204, 22, 0.4)',
          pink: '#ec4899',
          pinkDark: '#db2777',
          pinkGlow: 'rgba(236, 72, 153, 0.4)',
          blue: '#3b82f6',
          blueDark: '#2563eb',
          blueGlow: 'rgba(59, 130, 246, 0.4)',
          emerald: '#10b981',
          emeraldDark: '#059669',
          emeraldGlow: 'rgba(16, 185, 129, 0.4)',
        },
        surface: {
          DEFAULT: 'rgba(255, 255, 255, 0.03)',
          hover: 'rgba(255, 255, 255, 0.06)',
          dark: 'rgba(15, 23, 42, 0.8)',
          glass: 'rgba(255, 255, 255, 0.85)',
        },
        text: {
          light: '#f8fafc',
          muted: '#94a3b8',
          dark: '#0f172a',
        },
        border: {
          DEFAULT: 'rgba(255, 255, 255, 0.08)',
          light: 'rgba(255, 255, 255, 0.15)',
        },
      },
      fontFamily: {
        sans: ['var(--font-sans)', 'Noto Sans SC', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['var(--font-display)', 'Outfit', 'sans-serif'],
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
        shimmer: 'shimmer 2s linear infinite',
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
        shimmer: {
          '0%': { backgroundPosition: '-1000px 0' },
          '100%': { backgroundPosition: '1000px 0' },
        },
      },
      backdropBlur: {
        xs: '2px',
      },
      boxShadow: {
        'glow-orange': '0 8px 16px -4px rgba(249, 115, 22, 0.4)',
        'glow-cyan': '0 8px 16px -4px rgba(6, 182, 212, 0.4)',
        'glow-purple': '0 8px 16px -4px rgba(139, 92, 246, 0.4)',
        'glow-pink': '0 8px 16px -4px rgba(236, 72, 153, 0.4)',
        'glow-blue': '0 8px 16px -4px rgba(59, 130, 246, 0.4)',
        'glow-emerald': '0 8px 16px -4px rgba(16, 185, 129, 0.4)',
      },
      zIndex: {
        '100': '100',
        '150': '150',
        '200': '200',
      },
    },
  },
  plugins: [],
};
