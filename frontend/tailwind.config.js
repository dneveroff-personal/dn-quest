/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}"
    ],
    theme: {
        extend: {
            colors: {
                brand: {
                    indigo: "#6366f1",  // основной акцент
                    emerald: "#10b981",
                    rose: "#f43f5e"
                }
            }
        },
    },
    plugins: [],
}