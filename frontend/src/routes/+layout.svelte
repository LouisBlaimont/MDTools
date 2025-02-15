<script>
  import "../app.css";
  import { SvelteToast } from "@zerodevx/svelte-toast";
  import Loading from "$lib/Loading.svelte";
	import { checkRole } from "$lib/rbacUtils";
	import { ROLES } from "../constants";
	import { user } from "../stores"; 

  let { children } = $props();

  // simulating user roles
  let userRole = "admin"; // change to "admin" for admin view
  let userValue;

  // Subscribe to store
  user.subscribe(value => {
    userValue = value;
  });

  // Use reactive declarations
  let isAdmin = checkRole(userValue, ROLES.ADMIN);
  let isCustomer = checkRole(userValue, ROLES.CUSTOMER);
</script>

<header class="bg-teal-500 h-16 flex items-center justify-between px-6">
  <!-- Logo -->
  <a href="/">
    <img alt="Logo MD" src="logo-blanc.png" class="h-full" />
  </a>

  <!-- Navigation Bar -->
  <nav class="space-x-6 hidden md:flex">
    <a href="/login" class="text-white hover:text-teal-200 transition duration-300">
      Login
    </a>
    <!--{#if isCustomer}-->
    {#if userRole == "simpleUser"}
      <a href="/" class="text-white hover:text-teal-200 transition duration-300">
        Home Client
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
    {/if}

    <!--{#if isAdmin}-->
    {#if userRole == "admin"}
      <a href="/home_2" class="text-white hover:text-teal-200 transition duration-300">
        Home Admin
      </a>
      <a href="/add_group" class="text-white hover:text-teal-200 transition duration-300">
        Add Group
      </a>
      <a href="/login" class="text-white hover:text-teal-200 transition duration-300">
        Login
      </a>
      <a href="/searches_admin" class="text-white hover:text-teal-200 transition duration-300">
        Searches Admin
      </a>
      <a href="/webmaster" class="text-white hover:text-teal-200 transition duration-300">
        Webmaster
      </a>
      <a href="/import?" class="text-white hover:text-teal-200 transition duration-300">
        Import
      </a>
    {/if}
  </nav>

  <a class="mr-6" href="/login">
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
