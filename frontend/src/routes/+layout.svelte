<script>
  import "../app.css";
  import { SvelteToast } from "@zerodevx/svelte-toast";
  import { Modals } from 'svelte-modals'
  import Loading from "$lib/Loading.svelte";
	import { checkRole } from "$lib/rbacUtils";
	import { ROLES } from "../constants";
	import { user } from "$lib/stores/user_stores"; 

  let { children } = $props();

  // RBAC implementation
  let userRole = "admin"; // change to "admin" for admin view
  let userValue;
  user.subscribe(value => {
    userValue = value;
  });
  let isLoggedIn = true;//!!userValue; // check if user is connected when login is implemented
  let isAdmin = true;//checkRole(userValue, ROLES.ADMIN);
  let isWebmaster = checkRole(userValue, ROLES.WEBMASTER);

  // handle user authentication (changing it when correct login implementation)
  function handleAuth() {
    if (isLoggedIn) {
      // Log out logic to do when login ok
      user.set(null);
    } else {
      window.location.href = "/login";
    }
  } 
</script>

<header class="bg-teal-500 h-16 flex items-center justify-between px-6">
  <!-- Logo -->
  <a href="/">
    <img alt="Logo MD" src="logo-blanc.png" class="h-10" />
  </a>

  <!-- Navigation Bar -->
  <nav class="hidden md:flex space-x-6">
    <!-- add here pages to see them easily for developing -->

    <a href="/" class="text-white hover:text-teal-300 transition">Home</a>
    <a href="/searches" class="text-white hover:text-teal-300 transition">Searches</a>

    {#if isAdmin || isWebmaster}
      <a href="/admin/import" class="text-white hover:text-teal-300 transition">Import</a>
    {/if}

    {#if isWebmaster}
      <a href="/webmaster" class="text-white hover:text-teal-300 transition">Webmaster</a>
    {/if}
  </nav>

  <!-- Bouton Login / Logout -->
  <button
    onclick={handleAuth}
    class="px-4 py-2 rounded bg-yellow-100 text-black hover:bg-gray-500 transition"
  >
    {isLoggedIn ? "Log out" : "Login"}
  </button>
</header>

<div class="h-screen bg-white bg-[radial-gradient(#e5e7eb_1px,transparent_1px)] [background-size:16px_16px]">

{@render children()}

</div>

<SvelteToast />

<Modals>
  <!-- shown when any modal is opened -->
  {#snippet backdrop({ close })}
    <button
      class="backdrop"
      onclick={() => close()}
      onkeydown={(event) => event.key === 'Escape' && close()}
      aria-label="Close modal"
    ></button>
  {/snippet}
</Modals>

<style>
  .backdrop {
    position: fixed;
    top: 0;
    bottom: 0;
    right: 0;
    left: 0;
    background: rgba(0,0,0,0.50)
  }

  :root {
    --toastContainerTop: 4.25rem;
    --toastContainerRight: 1rem;
    --toastContainerBottom: auto;
    --toastContainerLeft: auto;
  }
</style>
