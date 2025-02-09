<script>
  import "../app.css";
  import { SvelteToast } from "@zerodevx/svelte-toast";

  let { children } = $props();
</script>

<header class="bg-teal-500 h-16 flex items-center justify-between px-6">
  <!-- Logo -->
  <a href="\">
    <img alt="Logo MD" src="logo-blanc.png" class="h-full" />
  </a>

  <!-- Navigation Bar -->
  <nav class="space-x-6 hidden md:flex">
    <!-- Hide nav items on small screens (md and below) -->
    <a href="/" class="text-white hover:text-teal-200 transition duration-300">
      Home Client
    </a>
    <a href="/home_2" class="text-white hover:text-teal-200 transition duration-300">
      Home Admin
    </a>
    <a href="/add_group" class="text-white hover:text-teal-200 transition duration-300">
      Add Group
      </a>
    <a href="/login" class="text-white hover:text-teal-200 transition duration-300">
      Login
    </a>
    <a href="/psw_forgot" class="text-white hover:text-teal-200 transition duration-300">
      Password Forgot
      </a>
    <a href="/sign_up" class="text-white hover:text-teal-200 transition duration-300">
      Sign Up
    </a>
    <a href="/searches" class="text-white hover:text-teal-200 transition duration-300">
      Searches Client
      </a>
    <a href="/searches_admin" class="text-white hover:text-teal-200 transition duration-300">
      Searches Admin
      </a>
    <a href="/webmaster" class="text-white hover:text-teal-200 transition duration-300">
      Webmaster</a>
    <a href="/import?" class="text-white hover:text-teal-200 transition duration-300">
      Import</a>
  </nav>
  <a class="mr-6" href="/">
		<button class="text-black bg-gray-300 rounded hover:bg-gray-700" type="submit">Log out</button>
	</a>
</header>

{@render children()}

<SvelteToast />

<style>
  :root {
    --toastContainerTop: 4.25rem;
    --toastContainerRight: 1rem;
    --toastContainerBottom: auto;
    --toastContainerLeft: auto;
  }
</style>
