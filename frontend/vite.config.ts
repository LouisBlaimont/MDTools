import { defineConfig, coverageConfigDefaults } from "vitest/config";
import { sveltekit } from '@sveltejs/kit/vite';

export default defineConfig({
    plugins: [sveltekit()],

    server: {
        port: 3000, // Set the port to 3000
        strictPort: true // Fail if the port is already in use
    },

    test: {
        include: ['src/**/*.{test,spec}.{js,ts}'],
        coverage: {
            exclude: ['coverage', 'node_modules', '.svelte-kit', '*.config.js', ...coverageConfigDefaults.exclude]
        }    
    }
});

