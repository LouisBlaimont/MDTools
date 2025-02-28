<script>
   import { onMount } from 'svelte';
   import { PUBLIC_API_URL } from "$env/static/public";
    
   export let log;
</script>

<li class="p-2 bg-gray-100 rounded border">
   <p><strong>Type:</strong> {log.action}</p>
   <p><strong>User:</strong> {log.username}</p>
   <p><strong>Timestamp:</strong> {new Date(log.timestamp).toLocaleString()}</p>
</li>
