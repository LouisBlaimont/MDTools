<script>
    import { _ } from "svelte-i18n";
    import { onMount, onDestroy } from "svelte";
    import { apiFetch } from "$lib/utils/fetch";

    let progress = 0;
    let processed = 0;
    let total = 0;
    let eta = null;
    let speed = null;
    let finished = false;

    let interval;

    // Poll the backend every 1s
    async function fetchProgress() {
      try {
        const res = await apiFetch("/api/import/progress", {
          credentials: "include"
        });

        if (!res.ok) return;

        const data = await res.json();

        progress = data.progress || 0;
        processed = data.processed || 0;
        total = data.total || 0;
        eta = data.eta || null;
        speed = data.speed || null;
        finished = data.finished || false;

        if (finished === true) {
          clearInterval(interval);
          dispatchEvent(new CustomEvent("finished"));
        }

      } catch (e) {
        console.error("Progress fetch failed:", e);
      }
    }

    onMount(() => {
      fetchProgress(); // first call
      interval = setInterval(fetchProgress, 1000);
    });

    onDestroy(() => {
      clearInterval(interval);
    });

    function formatETA(seconds) {
      if (!seconds || seconds <= 0) return "-";
      
      if (seconds < 60) return `${seconds}s`;

      const m = Math.floor(seconds / 60);
      const s = seconds % 60;
      return `${m}m${s}s`;
    }
  </script>

  <!-- Loading container -->
  <div class="flex flex-col items-center justify-center h-96 w-full space-y-4">

    <!-- Spinner -->
    <div class="loader rounded-full border-8 border-t-8 border-gray-200 h-20 w-20"></div>

    <!-- Title -->
    <p class="text-gray-800 text-xl font-semibold">
      {$_('import_pages.loading.chargement')}
    </p>

    <!-- Progress -->
    <p class="text-gray-700 text-sm">
      {processed} / {total} ({progress}%)
    </p>

    <!-- ETA -->
    <p class="text-gray-700 text-sm">
      ETA: {eta ? formatETA(eta) : "-"}
    </p>

    <!-- Speed -->
    {#if speed}
      <p class="text-gray-700 text-sm">
        {speed} items/s
      </p>
    {/if}
  </div>

  <style>
    .loader {
      border-top-color: #4299e1;
      animation: spin 1.2s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
</style>
