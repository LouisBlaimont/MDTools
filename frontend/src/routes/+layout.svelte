<script>
  import "../app.css";
  import { SvelteToast } from "@zerodevx/svelte-toast";
  import { Modals } from "svelte-modals";
  import { ROLES } from "../constants";
  import { user, isLoggedIn, isAdmin, isUser, isWebmaster, authReady, authChecking } from "$lib/stores/user_stores";
  import { login, checkUser, handleLogin, handleLogout } from "../auth";
  import { onMount } from "svelte";
  import { apiFetch } from "$lib/utils/fetch";
  import { toast } from "@zerodevx/svelte-toast";
  import { goto } from "$app/navigation";
  import { browser } from "$app/environment";
  import { page } from "$app/stores";
  import UserDropdown from "$lib/components/userDropdown.svelte";
  import { replaceState, afterNavigate } from "$app/navigation";
  import EditButton from "./searches/EditButton.svelte";
  import addInstrumentModal from "$lib/modals/addInstrumentModal.svelte";
  import { modals } from "svelte-modals";
  import addSupplierModal from "$lib/modals/addSupplierModal.svelte";
  import addCategoryModal from "$lib/modals/addCategoryModal.svelte";
  import AddGroupModal from "$lib/modals/AddGroupModal.svelte";
  import AddSubGroupModal from "$lib/modals/AddSubGroupModal.svelte";
  import { stopImmediatePropagation } from "svelte/legacy";
  import "$lib/i18n"; // Import to initialize. Important :)
  import { isLoading, locale, waitLocale, _ } from "svelte-i18n";
  import I18nSelector from "$lib/components/i18n_selector.svelte";
  import Loading from "$lib/Loading.svelte";

  async function i18nInit() {
    await waitLocale();
    console.log("I18n initialized with locale:", $locale);
  }

  let showDataMenu = false;

  function toggleDataMenu() {
    showDataMenu = !showDataMenu;
  }

  function closeMenu() {
    showDataMenu = false;
  }

  let showMoreMenu = false;
  let moreButton;
  let dataButton;

  function toggleMoreMenu(event) {
    event.stopPropagation();
    showMoreMenu = !showMoreMenu;
  }

  function closeMoreMenu() {
    showMoreMenu = false;
  }

  let showManageMenu = false;

  function toggleManageMenu() {
    showManageMenu = !showManageMenu;
  }

  function closeManageMenu() {
    stopImmediatePropagation();
    showManageMenu = false;
  }

  // Add a custom clickOutside directive
  export function clickOutside(node, options = {}) {
    const { exclude = [] } = options;

    const handleClick = (event) => {
      if (!node.contains(event.target) && !exclude.some((el) => el.contains(event.target))) {
        node.dispatchEvent(new CustomEvent("click_outside"));
      }
    };

    document.addEventListener("click", handleClick, true);

    return {
      destroy() {
        document.removeEventListener("click", handleClick, true);
      },
    };
  }

  // Handle redirect to login page if not logged in
  $: if (browser) {
    const onLoginPage = $page.url.pathname === "/login";
    const loginSuccess = $page.url.searchParams.get("login") === "success";

    // IMPORTANT: do not redirect until auth is "ready"
    if ($authReady && !$isLoggedIn && !loginSuccess && !onLoginPage) {
      goto("/login");
    }
  }

  // Determine if we should check the user
  $: shouldCheckUser = !$user || ($user?.expiresAt ?? 0) < Date.now();

  onMount(async () => {
    await i18nInit();

    if (shouldCheckUser) {
      await checkUser();
    }

    if (browser && $page.url.searchParams.get("login") === "success") {
      toast.push($_("header.toast"));
    }

    // Optional safety net: re-check the session periodically
    const intervalId = setInterval(() => {
      checkUser();
    }, 60_000); // every 60 seconds

    return () => clearInterval(intervalId);
  });
  
  function redirectToHome() {
    goto('/'); // Redirect to the home page
  }
</script>

{#if !$isLoading}
  <header class="bg-teal-500 h-16 flex items-center justify-between px-6">
    <!-- Logo -->
    <div class="flex items-center space-x-4">
      <!-- svelte-ignore a11y_click_events_have_key_events -->
      <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
      <img alt="Logo MD" src="/logo-blanc.png" class="h-10" 
      onclick={() => redirectToHome()}/>
      <I18nSelector />
    </div>

    <!-- Navigation Bar -->
    {#if $page.url.pathname !== "/login"}
      <nav class="hidden md:flex space-x-6">
        {#if $isLoggedIn}
          <a href="/" class="text-white hover:text-teal-300 transition">{$_('header.home')}</a>
          <a href="/searches" class="text-white hover:text-teal-300 transition">{$_('header.search')}</a>
        {/if}

        {#if $isAdmin || $isWebmaster}
          <div class="relative">
            <button
              bind:this={dataButton}
              onclick={toggleDataMenu}
              class="text-white hover:text-teal-300 transition flex items-center gap-1"
            >
              {$_('header.data')}
              <span class="text-xs">{showDataMenu ? "▲" : "▼"}</span>
            </button>

            {#if showDataMenu}
              <!-- svelte-ignore a11y_no_static_element_interactions -->
              <div
                class="absolute right-0 bg-teal-500 text-white shadow-lg rounded mt-2 w-32 z-50 text-sm"
                use:clickOutside={{ exclude: [dataButton] }}
                onclick_outside={closeMenu}
              >
                <a href="/admin/import" class="block px-4 py-2 hover:bg-teal-400 transition"
                  >Import</a
                >
                <a href="/admin/export" class="block px-4 py-2 hover:bg-teal-400 transition"
                  >Export</a
                >
              </div>
            {/if}
          </div>
        {/if}

        <!-- More Menu -->
        <div class="relative">
          <button
            bind:this={moreButton}
            onclick={toggleMoreMenu}
            class="text-white hover:text-teal-300 transition flex items-center gap-1"
          >
            {$_('header.more.title')}
            <span class="text-xs">{showMoreMenu ? "▲" : "▼"}</span>
          </button>

          {#if showMoreMenu}
            <!-- svelte-ignore a11y_no_static_element_interactions -->
            <div
              class="absolute right-0 bg-teal-500 text-white shadow-lg rounded mt-2 w-40 z-50 text-sm"
              use:clickOutside={{ exclude: [moreButton] }}
              onclick_outside={() => {closeMoreMenu(); closeManageMenu();}}
            >
              {#if $isAdmin || $isWebmaster}
                <a href="/admin/supplier" class="block px-4 py-2 hover:bg-teal-400 transition"
                  onclick={closeMoreMenu}
                  >{$_('header.more.suppliers')}</a
                >
                <a href="/admin/abbreviations" class="block px-4 py-2 hover:bg-teal-400 transition"
                  onclick={closeMoreMenu}
                  >{$_('header.more.abbreviations')}</a
                >
                <a
                  href="/admin/backups"
                  class="block px-4 py-2 hover:bg-teal-400 transition"
                  onclick={() => {
                    closeMoreMenu();
                    closeManageMenu();
                  }}
                >
                  Backups
                </a>
                {#if $isWebmaster}
                  <a href="/webmaster" class="block px-4 py-2 hover:bg-teal-400 transition"
                    onclick={closeMoreMenu}
                    >{$_('header.more.webmaster')}</a
                  >
                {/if}

                <!-- Manage Submenu -->
                <div class="relative">
                  <button
                    onclick={toggleManageMenu}
                    class="block px-4 py-2 hover:bg-teal-400 transition text-left w-full flex items-center gap-1"
                  >
                    {$_('header.more.add')}
                    <span class="text-xs">{showManageMenu ? "▲" : "▼"}</span>
                  </button>

                  {#if showManageMenu}
                    <!-- svelte-ignore a11y_no_static_element_interactions -->
                    <div
                      class="absolute right-0 bg-teal-500 text-white shadow-lg rounded mt-2 w-40 z-50 text-sm"
                    >
                      <button
                        class="block px-4 py-2 hover:bg-teal-400 transition text-left w-full"
                        onclick={() => {
                          closeManageMenu();
                          closeMoreMenu();
                          modals.open(addInstrumentModal, {
                            initInstrument: null,
                            initCategory: null,
                          });
                        }}
                      >
                        {$_('header.more.add_instrument')}
                      </button>
                      <button
                        class="block px-4 py-2 hover:bg-teal-400 transition text-left w-full"
                        onclick={() => {
                          closeManageMenu();
                          closeMoreMenu();
                          modals.open(addSupplierModal);
                        }}
                      >
                        {$_('header.more.add_supplier')}
                      </button>
                      <button
                        class="block px-4 py-2 hover:bg-teal-400 transition text-left w-full"
                        onclick={() => {
                          closeManageMenu();
                          closeMoreMenu();
                          modals.open(addCategoryModal, {fromSearches : false});
                        }}
                      >
                        {$_('header.more.add_category')}
                      </button>
                      <!-- Add Group Button -->
                      <button
                        class="block px-4 py-2 hover:bg-teal-400 transition text-left w-full"
                        onclick={() => {
                          closeManageMenu();
                          closeMoreMenu();
                          modals.open(AddGroupModal);
                        }}
                        >
                        {$_('header.more.add_group')}
                      </button>
                    </div>
                  {/if}
                </div>
              {/if}
            </div>
          {/if}
        </div>
      </nav>
    {/if}

    <div class="flex items-center space-x-4">
      <!-- PASS IN ADMIN MODE in searches-->
      {#if $isAdmin && $page.url.pathname === "/searches"}
        <EditButton />
      {/if}
      <!-- Login / Logout Button -->
      <UserDropdown />
    </div>
  </header>

  <main
    class="h-screen bg-white bg-[radial-gradient(#e5e7eb_1px,transparent_1px)] [background-size:16px_16px]"
  >
    {#key $page.url.pathname}
      <slot />
    {/key}
  </main>

  <SvelteToast />

  <Modals>
    {#snippet backdrop({ close })}
      <button
        class="backdrop"
        onclick={() => close()}
        onkeydown={(event) => event.key === "Escape" && close()}
        aria-label="Close modal"
      ></button>
    {/snippet}
  </Modals>
{:else}
  <div class="flex items-center justify-center h-screen">
    <Loading />
  </div>
{/if}

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
