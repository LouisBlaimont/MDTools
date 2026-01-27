<script>
  import { user, isLoggedIn, isAdmin, isUser, isWebmaster } from "$lib/stores/user_stores";
  import { login, checkUser, handleLogin } from "../../auth";
  import { apiFetch } from "$lib/utils/fetch";
  import { toast } from "@zerodevx/svelte-toast";
  import { goto } from "$app/navigation";
  import { browser } from "$app/environment";
  import { page } from "$app/stores";
  import { onMount } from "svelte";
  import { _ } from "svelte-i18n";
  import { preventDefault } from "svelte/legacy";

</script>

<svelte:head>
  <title>{$_('login.connection')}</title>
</svelte:head>

<main class="flex flex-col md:flex-row justify-center w-11/12 lg:w-3/4 mx-auto mt-6 p-6">
  <div
    class="flex w-full md:w-1/3 lg:w-1/4 mb-6 md:mb-0 justify-center items-center border-3 rounded-lg bg-teal-500 shadow-lg shadow-teal-500"
  >
    <div class="flex flex-col items-center w-full p-6">
      <h1 class="text-2xl text-center mb-2 text-white">{$_('login.welcome')}</h1>
      <span class="text-center text-white"
        >{$_('login.ways')}</span
      >
    </div>
  </div>

  <div class="border-3 rounded-lg w-full md:w-2/3 lg:w-1/2 p-6 mx-auto bg-gray-100 shadow-lg">
    <div class="text-center mb-4">
      <img
        alt="The project logo"
        src="MDlogo.jpg"
        class="w-1/2 md:w-1/3 lg:w-1/4 rounded-full mx-auto"
      />
    </div>

    <div class="space-y-3">
      <!-- DEX Login Button -->
      <div class="flex justify-center mt-4">
        <button
          onclick={() => handleLogin()}
          class="grow relative mt-4 inline-flex items-center justify-center p-0.5 mb-2 me-2 overflow-hidden text-sm font-medium text-gray-900 rounded-lg group bg-gradient-to-br from-cyan-500 to-blue-500 group-hover:from-cyan-500 group-hover:to-blue-500 hover:text-white focus:ring-4 focus:outline-none focus:ring-cyan-200"
        >
          <span
            class="w-full h-16 px-5 py-2.5 flex items-center relative transition-all ease-in duration-75 bg-white rounded-md group-hover:bg-transparent text-lg overflow-hidden"
          >
            <p class="flex=none">{$_('login.keycloack')}</p>
            <img
              src="logo-keycloack.png"
              alt="DEX Logo"
              class="flex-1 h-10 absolute right-4 transition-all duration-300 md:transform group-hover:-translate-x-full group-hover:rotate-[360deg] group-hover:scale-110 group-hover:bg-white rounded-full p-0.5 group-hover:shadow-lg"
            />
          </span>
        </button>
      </div>
    </div>
  </div>
</main>
