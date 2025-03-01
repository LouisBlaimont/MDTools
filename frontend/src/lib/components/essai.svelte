<!-- In any component -->
<script>
    import { sharedData } from '$lib/stores/searches.js';
</script>
  
  <div>
    <!-- Using the $ prefix subscribes to the store -->
    <p>{$sharedData.name}</p>
    <button on:click={() => sharedData.set({ name: 'Alice' })}>Change Name</button>
  </div>
  