<script>
  import { goto } from '$app/navigation'; // Import SvelteKit navigation helper

  let users = [
    { id: "1", email: "user1@example.com" },
    { id: "2", email: "user2@example.com" },
    { id: "3", email: "user3@example.com" },
    { id: "4", email: "admin@example.com" }
  ]; // This would normally come from an API

  let email = "";
  let searchQuery = "";  // New search query state
  let loading = false;

  // Action functions
  const createUser = async () => {
    if (!email) {
      alert('Please enter an email address.');
      return;
    }
    loading = true;
    // Simulate API call with a delay
    setTimeout(() => {
      alert(`User with email ${email} created!`);
      users.push({ id: (users.length + 1).toString(), email });
      email = "";
      loading = false;
    }, 1500);
  };

  const disableAccount = (userId) => {
    if (confirm(`Are you sure you want to disable the account with ID ${userId}?`)) {
      alert(`Account with ID ${userId} disabled!`);
    }
  };

  const resetPassword = (userId) => {
    if (confirm(`Are you sure you want to reset the password for user ID ${userId}?`)) {
      alert(`Password reset for user ID ${userId}`);
    }
  };

  // Navigate to the logs page
  const viewLogs = () => {
    goto('/webmaster/logs');
  };

  // Filter users based on the search query
  $: filteredUsers = users.filter(user => user.email.toLowerCase().includes(searchQuery.toLowerCase()));
</script>

<div class="p-8 space-y-10">
  <!-- User Management Section -->
  <section class="bg-white shadow-lg rounded-xl p-6">
    <h2 class="text-xl font-semibold mb-8">Website Administration</h2>

    <!-- Create User Form and Consult Logs Button in the same row -->
    <div class="flex items-start space-x-6 mb-8">
      <div class="flex-1 max-w-xs bg-gray-50 border border-gray-300 rounded-lg p-6">
        <h3 class="text-lg font-semibold mb-4">Create User</h3>
        <div class="space-y-6">
          <input
            type="email"
            bind:value={email}
            placeholder="Enter user email"
            class="w-full p-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
          <button
            on:click={createUser}
            class="w-full bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition disabled:opacity-50"
            disabled={loading}
          >
            {#if loading}
              Creating...
            {:else}
              Create User
            {/if}
          </button>
        </div>
      </div>

      <!-- Consult Logs Button -->
      <div class="flex-1 max-w-xs bg-gray-50 border border-gray-300 rounded-lg p-6">
        <h3 class="text-lg font-semibold mb-4">Consult Logs</h3> <!-- Empty header for alignment -->
        <button
          on:click={viewLogs}
          class="w-full bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition ease-in-out duration-200 transform hover:scale-105"
        >
          View Logs
        </button>
      </div>
    </div>

    <!-- Users Table -->
    <h3 class="text-lg font-semibold mb-6">Existing Users</h3>
    
    <!-- Search Field -->
    <div class="mb-6 max-w-xs">
      <input
        type="text"
        bind:value={searchQuery}
        placeholder="Search by email"
        class="w-full p-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
      />
    </div>

    <div class="overflow-x-auto">
      <table class="min-w-full table-auto border-collapse">
        <thead>
          <tr class="bg-gray-100 text-left text-sm font-semibold">
            <th class="px-6 py-4">User Email</th>
            <th class="px-6 py-4">Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each filteredUsers as user}
            <tr class="border-b hover:bg-gray-50">
              <td class="px-6 py-4">{user.email}</td>
              <td class="px-6 py-4 space-x-3">
                <button
                  on:click={() => disableAccount(user.id)}
                  class="bg-red-600 text-white px-5 py-2 rounded-lg hover:bg-red-700 transition ease-in-out duration-200 transform hover:scale-105"
                >
                  Disable
                </button>
                <button
                  on:click={() => resetPassword(user.id)}
                  class="bg-yellow-500 text-white px-5 py-2 rounded-lg hover:bg-yellow-600 transition ease-in-out duration-200 transform hover:scale-105"
                >
                  Reset Password
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </section>
</div>
