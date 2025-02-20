import { defineConfig } from 'vitest/config';
import { sveltekit } from '@sveltejs/kit/vite';

export default defineConfig({
  plugins: [sveltekit()], // Ensures SvelteKit compatibility with Vitest
  test: {
    globals: true,
    environment: "jsdom", // Necessary for frontend testing
  },
  resolve: {
    alias: {
      $lib: "/src/lib", 
      $routes: "/src/routes"
    },
  },
});
