<script>
   import { onMount } from 'svelte';
   
   let logs = [];
 
   // Fetch logs when the component is mounted
   onMount(async () => {
     // Replace this with actual API call for fetching logs
     logs = [
       { id: 1, type: "Login Failed", user: "user1@example.com", timestamp: "2024-11-18T12:00:00Z" },
       { id: 2, type: "Account Created", user: "user2@example.com", timestamp: "2024-11-17T08:30:00Z" }
     ];
   });
 </script>
 
 <div class="p-6 space-y-8">
   <!-- Logs Section -->
   <section class="bg-white shadow rounded-lg p-6">
     <h2 class="text-lg font-semibold mb-4">Logs</h2>
     <ul class="space-y-2">
       {#each logs as log}
         <li class="p-2 bg-gray-100 rounded border">
           <p><strong>Type:</strong> {log.type}</p>
           <p><strong>User:</strong> {log.user}</p>
           <p><strong>Timestamp:</strong> {new Date(log.timestamp).toLocaleString()}</p>
         </li>
       {/each}
       {#if logs.length === 0}
         <p class="text-gray-500">No logs available.</p>
       {/if}
     </ul>
   </section>
 </div>
 