<script>
   import { locale, locales } from "svelte-i18n";
   import Icon from '@iconify/svelte';

</script>
 
 <div class="relative inline-block">
   <select
     bind:value={$locale}
     class="w-full appearance-none bg-white border border-gray-300 text-gray-700 px-4 pr-10 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
   >
     {#each $locales as locale}
       <option value={locale}>{locale}</option>
     {/each}
   </select>
 
   <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-500">
     <Icon icon="mdi:chevron-down" class="w-2 h-2" />
   </div>
 </div>
 
 