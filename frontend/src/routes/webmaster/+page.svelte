<script>
  import { goto } from "$app/navigation"; // Import SvelteKit navigation helper
  import { toast } from "@zerodevx/svelte-toast";

  let users = [
    { id: "1", email: "user1@example.com", roles: ["admin"], enabled: false },
    { id: "2", email: "user2@example.com", roles: ["user"], enabled: true },
    { id: "3", email: "user3@example.com", roles: ["admin", "webmaster"], enabled: true },
    { id: "4", email: "admin@example.com", roles: ["user"], enabled: true },
  ]; // This would normally come from an API

  let email = "";
  let searchQuery = ""; // New search query state
  let loading = false;

  // Action functions
  const createUser = async () => {
    if (!email) {
      toast.push("Please enter a valid email address");
      return;
    }
    loading = true;
    // Simulate API call with a delay
    setTimeout(() => {
      toast.push(`User with email ${email} created!`);
      users.push({ id: (users.length + 1).toString(), email });
      email = "";
      loading = false;
    }, 1500);
  };

  const disableAccount = (userId) => {
    const userIndex = users.findIndex((u) => u.id === userId);
    if (userIndex !== -1) {
      users[userIndex].enabled = !users[userIndex].enabled; // Toggle status
      toast.push(
        users[userIndex].enabled
          ? `Account with ID ${userId} enabled!`
          : `Account with ID ${userId} disabled!`
      );
    }
  };

  const resetPassword = (userId) => {
    if (confirm(`Are you sure you want to reset the password for user ID ${userId}?`)) {
      toast.push(`Password reset for user ID ${userId}`);
    }
  };

  // Navigate to the logs page
  const viewLogs = () => {
    goto("/webmaster/logs");
  };

  const getBadges = (userId) => {
    const user = users.find((u) => u.id === userId);
    if (user) {
      return user.roles.map((role) => {
        if (role === "admin") {
          return {
            text: "Administrateur",
            bg: "bg-red-100",
            textColor: "text-red-800",
            ringColor: "ring-red-800/10",
          };
        } else if (role === "webmaster") {
          return {
            text: "Webmaster",
            bg: "bg-green-100",
            textColor: "text-green-800",
            ringColor: "ring-green-800/10",
          };
        } else if (role === "user") {
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

  // Filter users based on the search query
  $: filteredUsers = users.filter((user) =>
    user.email.toLowerCase().includes(searchQuery.toLowerCase())
  );
</script>

<div class="p-8 space-y-10">
  <section class="bg-white shadow-lg rounded-xl p-6">
    <h2 class="text-2xl font-semibold mb-8">Website Administration</h2>

    <div class="flex flex-col lg:flex-row items-start lg:space-x-6 space-y-6 lg:space-y-0 mb-8">
      <div class="flex-1 max-w-md bg-gray-50 border border-gray-300 rounded-lg p-6">
        <h3 class="text-lg font-medium mb-4">Create User</h3>
        <div class="space-y-4">
          <input
            type="email"
            bind:value={email}
            placeholder="Enter user email"
            class="w-full p-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
          <button
            on:click={createUser}
            class="w-full bg-gradient-to-r from-green-500 to-green-700 text-white px-6 py-3 rounded-lg hover:scale-105 transform transition disabled:opacity-50 flex items-center justify-center space-x-2"
            disabled={loading}
          >
            {#if loading}
              <span class="animate-spin h-5 w-5 border-t-2 border-white rounded-full"></span>
              <span>Creating...</span>
            {:else}
              <span>Create User</span>
            {/if}
          </button>
        </div>
      </div>

      <div class="flex-1 max-w-md bg-gray-50 border border-gray-300 rounded-lg p-6">
        <h3 class="text-lg font-medium mb-4">Consult Logs</h3>
        <button
          on:click={viewLogs}
          class="w-full bg-gradient-to-r from-blue-500 to-blue-700 text-white px-6 py-3 rounded-lg hover:scale-105 transform transition"
        >
          View Logs
        </button>
      </div>
    </div>

    <!-- Users List Section -->
    <div class="bg-gray-50 border border-gray-300 rounded-lg p-6">
      <h3 class="text-lg font-medium mb-6">Existing Users</h3>

      <!-- Search Bar -->
      <div class="mb-6 max-w-xs">
        <input
          type="text"
          bind:value={searchQuery}
          placeholder="Search by email"
          class="w-full p-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <!-- Users Table -->
      <div class="overflow-x-auto">
        <table class="min-w-full table-auto border-collapse">
          <thead>
            <tr class="bg-gray-100 text-left text-sm font-semibold">
              <th class="px-6 py-4">User Email</th>
              <th class="px-6 py-4">Roles</th>
              <th class="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each filteredUsers as user}
              <tr class="border-b hover:bg-gray-50 transition">
                <td class="px-6 py-4">
                  {user.email}
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
                <td class="px-6 py-4 space-x-1">
                  {#each getBadges(user.id) as badge}
                    <span
                      class={`px-2 py-1 rounded-md text-xs font-medium ring-1 ring-inset ${badge.bg} ${badge.textColor} ${badge.ringColor}`}
                    >
                      {badge.text}
                    </span>
                  {/each}
                </td>
                <td class="px-6 py-4 inline-flex rounded-md shadow-sm">
                  <button
                    on:click={() => disableAccount(user.id)}
                    class={`px-5 py-2 rounded-l-lg w-32 flex items-center transform transition ${
                      user.enabled
                        ? "bg-red-600 hover:bg-red-700 text-white"
                        : "bg-green-600 hover:bg-green-700 text-white"
                    }`}
                  >
                    <span
                      ><svg
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
                          d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"
                        />
                      </svg>
                    </span>
                    <span class="pl-2">{user.enabled ? "Disable" : "Enable"}</span>
                  </button>

                  <button
                    on:click={() => resetPassword(user.id)}
                    class="bg-yellow-500 text-white px-5 py-2 rounded-r-lg hover:bg-yellow-600 flex items-center transform transition"
                  >
                    <span
                      ><svg
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
                          d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0 3.181 3.183a8.25 8.25 0 0 0 13.803-3.7M4.031 9.865a8.25 8.25 0 0 1 13.803-3.7l3.181 3.182m0-4.991v4.99"
                        />
                      </svg>
                    </span>
                    <span class="pl-2">Reset Password</span>
                  </button>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  </section>
</div>
