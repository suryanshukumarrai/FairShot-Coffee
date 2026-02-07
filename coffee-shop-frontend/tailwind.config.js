/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                bg: {
                    primary: '#0f172a',
                    secondary: '#1e293b',
                    tertiary: '#334155',
                },
                accent: {
                    coffee: '#92400e',
                    light: '#fbbf24',
                },
                status: {
                    urgent: '#ef4444',
                    warning: '#f59e0b',
                    normal: '#10b981',
                }
            },
            fontFamily: {
                sans: ['Inter', 'system-ui', 'sans-serif'],
            },
        },
    },
    plugins: [],
}
