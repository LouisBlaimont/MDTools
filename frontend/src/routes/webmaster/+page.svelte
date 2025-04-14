<script>
  import { goto } from "$app/navigation";
  import { toast } from "@zerodevx/svelte-toast";
  import { apiFetch } from "$lib/utils/fetch";
  import { user, isLoggedIn, isAdmin, isUser, isWebmaster } from "$lib/stores/user_stores";
  import { onMount } from "svelte";
  import UserButton from "./UserButton.svelte";

  import Icon from "@iconify/svelte";
  import { reload } from "$lib/stores/searches";

  onMount(() => {
    if (!$isWebmaster || !$isAdmin) {
      goto("/unauthorized");
    }
    reload.set(true);
  });

  let users = $state();
  let searchQuery = "";
  let roles = [];

  const itemsPerPage = 3; // Number of items per page
  let currentPage = 1; // Current page number

  const totalPages = 0

  const viewLogs = () => {
    goto("/webmaster/logs");
  };

  const getBadges = (user) => {
    if (user) {
      return user.roles.map((role) => {
        if (role === "ROLE_ADMIN") {
          return {
            text: "Administrateur",
            bg: "bg-red-100",
            textColor: "text-red-800",
            ringColor: "ring-red-800/10",
          };
        } else if (role === "ROLE_WEBMASTER") {
          return {
            text: "Webmaster",
            bg: "bg-green-100",
            textColor: "text-green-800",
            ringColor: "ring-green-800/10",
          };
        } else if (role === "ROLE_USER") {
          return {
            text: "Utilisateur",
            bg: "bg-blue-100",
            textColor: "text-blue-800",
            ringColor: "ring-blue-800/10",
          };
        } else {
          return {
            text: role,
            bg: "bg-gray-100",
            textColor: "text-gray-800",
            ringColor: "ring-gray-800/10",
          };
        }
      });
    }
    return [];
  };

  async function fetchRoles() {
    try {
      const response = await apiFetch("/api/role/list");
      if (response.ok) {
        roles = await response.json();
      } else {
        throw new Error("Failed to fetch roles.");
      }
    } catch (error) {
      toast.push("An error occurred while fetching roles. Please try again.<br> Erreur:" + error);
    }    
  }

  async function fetchUsers() {
    try {
      const response = await apiFetch("/api/user/list");
      if (response.ok) {
        users = await response.json();
      } else {
        throw new Error("Failed to fetch users.");
      }
    } catch (error) {
      toast.push("An error occurred while fetching users. Please try again.<br> Erreur:" + error);
    }
  }

  onMount(() => {
    if (!$isWebmaster || !$isAdmin) {
      goto("/unauthorized");
    }

    fetchUsers();
    fetchRoles();
  });
</script>

<svelte:head>
  <title>Gestion du site</title>
</svelte:head>

<div class="p-8 space-y-10">
  <section class="bg-white shadow-lg rounded-xl p-6">
    <h2 class="text-2xl font-semibold mb-8">Administration du site</h2>

    <div class="flex flex-col lg:flex-row items-start lg:space-x-6 space-y-6 lg:space-y-0 mb-8">
      <!-- Consult Logs Section -->
      <div class="flex-1 max-w-md bg-gray-50 border border-gray-300 rounded-lg p-6">
        <h3 class="text-lg font-medium mb-4">Consulter les logs</h3>
        <button
          onclick={viewLogs}
          class="w-full bg-gradient-to-r from-blue-500 to-blue-700 text-white px-6 py-3 rounded-lg hover:scale-105 transform transition"
        >
          Accéder aux logs
        </button>
      </div>
    </div>

    <!-- Users List Section -->
    <div class="bg-gray-50 border border-gray-300 rounded-lg p-6">
      <h3 class="text-lg font-medium mb-6">Utilisateurs</h3>

      <!-- Search Bar -->
      <div class="mb-6 max-w-xs">
        <input
          type="text"
          bind:value={searchQuery}
          placeholder="Rechercher un utilisateur"
          class="w-full p-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <!-- Users Table -->
      <div class="overflow-x-auto">
        <table class="min-w-full table-auto border-collapse">
          <thead>
            <tr class="bg-gray-100 text-left text-sm font-semibold">
              <th class="px-6 py-4 w-96">Nom d'utilisateur</th>
              <th class="px-6 py-4 w-96">Adresse email</th>
              <th class="px-6 py-4 w-64">Rôles</th>
              <th class="px-6 py-4 w-36">Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each users as user (user.id)}
              <tr class="border-b hover:bg-gray-50 transition">
                <td class="px-6 py-4">
                  {user.username}
                  {#if user.enabled}
                    <span
                      class="rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-700/10"
                      >Actif</span
                    >
                  {:else}
                    <span
                      class="rounded-md bg-purple-50 px-2 py-1 text-xs font-medium text-purple-700 ring-1 ring-inset ring-purple-700/10"
                      >Désactivé</span
                    >
                  {/if}
                </td>
                <td class="px-6 py-4">
                  {user.email}
                </td>
                <td class="px-6 py-4 space-x-1">
                  {#each getBadges(user) as badge}
                    <span
                      class={`px-2 py-1 rounded-md text-xs font-medium ring-1 ring-inset ${badge.bg} ${badge.textColor} ${badge.ringColor}`}
                    >
                      {badge.text}
                    </span>
                  {/each}
                </td>
                <td class="px-6 py-4 inline-flex rounded-md shadow-sm">
                  <UserButton user={user} roles={roles} />
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>

      <!-- Pagination Controls -->
      <div class="flex justify-center items-center space-x-4 mt-4">
        <button
          onclick={() => goToPage(currentPage - 1)}
          disabled={currentPage === 1}
          aria-label="Précédent"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="size-6"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="m18.75 4.5-7.5 7.5 7.5 7.5m-6-15L5.25 12l7.5 7.5"
            />
          </svg>
        </button>
        <span>Page {currentPage} sur {totalPages}</span>
        <button
          onclick={() => goToPage(currentPage + 1)}
          disabled={currentPage === totalPages}
          aria-label="Suivant"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="size-6"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="m5.25 4.5 7.5 7.5-7.5 7.5m6-15 7.5 7.5-7.5 7.5"
            />
          </svg>
        </button>
      </div>
    </div>
  </section>
</div>
