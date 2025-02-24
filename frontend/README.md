# Frontend

The frontend is using Sveltekit using Javascript. Libraries are managed by `npm`.

## Developing

Once you've created a project and installed dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```bash
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of your app:

```bash
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://svelte.dev/docs/kit/adapters) for your target environment.

## Folder Structure

    /src: Contains all source code.
        /src/routes: Files for page routes. Each .svelte file in this folder represents a route.
        /src/lib: Utility functions and components that can be shared across the app.
    /static: Static assets like images, fonts, etc.
