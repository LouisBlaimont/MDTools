<script>
  import { onMount } from 'svelte';
  import { apiFetch } from '$lib/utils/fetch';
  import LogCard from "./LogCard.svelte";

  let logsPromise = []; // Store the promise itself

  async function fetchLogs() {
    try {
      const response = await apiFetch("/api/logs/list");
      if (!response.ok) {
        throw new Error(`Failed to fetch logs: ${response.statusText}`);
      }
      return response.json();
    } catch (error) {
      console.error(error);
      throw error; // Let the error be caught in the #await block
    }
  }

  onMount(() => {
    logsPromise = new Promise(resolve => {
      resolve(fetchLogs());
    });
  });
</script>

<div class="p-6 space-y-8">
  <section class="bg-white shadow rounded-lg p-6">
    <h2 class="text-lg font-semibold mb-4">Logs</h2>
    {#await logsPromise}
      <p>Loading...</p>
    {:then logs}
      <ul class="space-y-2">
        {#each logs as log}
          <LogCard {log} />
        {/each}
        {#if logs.length === 0}
          <p class="text-gray-500">No logs available.</p>
        {/if}
      </ul>
    {:catch error}
      <p class="text-red-500">{error.message}</p>
    {/await}
  </section>
</div>
