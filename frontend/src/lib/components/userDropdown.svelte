<script>
  import { onMount, onDestroy } from "svelte";
  import { browser } from "$app/environment";
  import { goto } from "$app/navigation";
  import { user, isLoggedIn } from "$lib/stores/user_stores";
  import { handleLogout } from "../../auth";
  import Icon from "@iconify/svelte";
  import { _ } from "svelte-i18n";

  let isOpen = false; // Track dropdown state

  // Toggle dropdown visibility
  function toggleDropdown() {
    isOpen = !isOpen;
  }

  // Close the dropdown when clicking outside of it
  function handleClickOutside(event) {
    if (!event.target.closest(".dropdown")) {
      isOpen = false;
    }
  }

  onMount(() => {
    if (browser) document.addEventListener("click", handleClickOutside);
  });
  onDestroy(() => {
    if (browser) document.removeEventListener("click", handleClickOutside);
  });
</script>

<div class="relative inline-block text-left dropdown">
  {#if $isLoggedIn && $user}
    <div>
      <button
        type="button"
        class="inline-flex w-full justify-center gap-x-1.5 rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50"
        aria-expanded={isOpen ? "true" : "false"}
        aria-haspopup="true"
        onclick={toggleDropdown}
      >
        {$user.name}
        <Icon icon="material-symbols:keyboard-arrow-down-rounded" width="24" height="24" />
      </button>
    </div>
  {/if}

  {#if isOpen}
    <div
      class="absolute right-0 z-10 mt-2 w-56 origin-top-right divide-y divide-gray-300 border border-gray-300 rounded-md bg-white shadow-lg ring-black/5 focus:outline-hidden"
      role="menu"
      aria-orientation="vertical"
      aria-labelledby="menu-button"
      tabindex="-1"
    >
      <div class="py-1" role="none">
        <a
          href="/users"
          class="block px-4 py-2 text-sm text-gray-700"
          role="menuitem"
          tabindex="-1"
          id="menu-item-0"
          onclick={() => {
            isOpen = false;
          }}
        >
          {$_('profile')}</a
        >
      </div>
      <div class="" role="none">
        <button
          onclick={() => {
            handleLogout();
            isOpen = false;
          }}
          class="block mx-4 my-2 text-sm text-gray-700 w-full h-full text-left"
          role="menuitem"
          tabindex="-1"
          id="menu-item-1">{$_('log_out')}</button
        >
      </div>
    </div>
  {/if}
</div>
