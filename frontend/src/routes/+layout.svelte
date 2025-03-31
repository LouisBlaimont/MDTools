<script>
  import "../app.css";
  import { SvelteToast } from "@zerodevx/svelte-toast";
  import { Modals } from "svelte-modals";
  import { ROLES } from "../constants";
  import { user, isLoggedIn, isAdmin, isUser, isWebmaster } from "$lib/stores/user_stores";
  import { login, checkUser, handleLogin, handleLogout } from "../auth";
  import { onMount } from "svelte";
  import { apiFetch } from "$lib/utils/fetch";
  import { toast } from "@zerodevx/svelte-toast";
  import { goto } from "$app/navigation";
  import { browser } from "$app/environment";
  import { page } from '$app/stores';
  import UserDropdown from "$lib/components/userDropdown.svelte";
  import { replaceState, afterNavigate } from "$app/navigation";


  let showDataMenu = false;

  function toggleDataMenu() {
    showDataMenu = !showDataMenu;
  }

  function closeMenu() {
    showDataMenu = false;
  }

  // Handle redirect to login page if not logged in
  $: if(browser) {
    if (!$isLoggedIn && !($page.url.searchParams.get("login") === "success") && window && window.location.pathname !== "/login") {
        goto("/login");
    }
  }

  // Determine if we should check the user
  $: shouldCheckUser = !$user || ($user?.expiresAt ?? 0) < Date.now();

  onMount(() => {
    if(shouldCheckUser) {
      checkUser();
    }

    if(browser && $page.url.searchParams.get("login") === "success") {
      toast.push("You have successfully log in !");
    }
  }
)

</script>

<header class="bg-teal-500 h-16 flex items-center justify-between px-6">
  <!-- Logo -->
  <img alt="Logo MD" src="/logo-blanc.png" class="h-10" />

  <!-- Navigation Bar -->
  <nav class="hidden md:flex space-x-6">
    {#if $isLoggedIn}
      <a href="/" class="text-white hover:text-teal-300 transition">Home</a>
      <a href="/searches" class="text-white hover:text-teal-300 transition">Searches</a>
      <a href="/users" class="text-white hover:text-teal-300 transition">User Profile</a>
    {/if}

    {#if $isAdmin || $isWebmaster}
      <div class="relative">
        <button 
          onclick={toggleDataMenu}
          class="text-white hover:text-teal-300 transition flex items-center gap-1"
        >
          Data
          <span class="text-xs">{showDataMenu ? "▲" : "▼"}</span>
        </button>

        {#if showDataMenu}
          <!-- svelte-ignore a11y_no_static_element_interactions -->
          <div 
            class="absolute right-0 bg-teal-500 text-white shadow-lg rounded mt-2 w-32 z-50 text-sm"
            onmouseleave={closeMenu}
          >
            <a href="/admin/import" class="block px-4 py-2 hover:bg-teal-400 transition">Import</a>
            <a href="/admin/export" class="block px-4 py-2 hover:bg-teal-400 transition">Export</a>
          </div>
        {/if}
      </div>

      <!-- Keep these always visible -->
      <a href="/admin/users" class="text-white hover:text-teal-300 transition">Users</a>
      <a href="/admin/suppliers" class="text-white hover:text-teal-300 transition">Suppliers</a>
      <a href="/admin/abbreviations" class="text-white hover:text-teal-300 transition">Abbreviations</a>
    {/if}

    {#if $isWebmaster}
      <a href="/webmaster" class="text-white hover:text-teal-300 transition">Webmaster</a>
    {/if}
  </nav>

  <!-- Login / Logout Button -->
  <UserDropdown />
 
</header>

<main class="h-screen bg-white bg-[radial-gradient(#e5e7eb_1px,transparent_1px)] [background-size:16px_16px]">
  <slot />
</main>

<SvelteToast />

<Modals>
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
    background: rgba(0, 0, 0, 0.5);
  }

  :root {
    --toastContainerTop: 4.5rem;
    --toastContainerRight: 1rem;
    --toastContainerBottom: auto;
    --toastContainerLeft: auto;
  }
</style>
