<script>
  import { apiFetch } from "$lib/utils/fetch";
  import { toast } from "@zerodevx/svelte-toast";
  import { onMount } from "svelte";
  import { writable } from "svelte/store";
  import EditSupplierButton from "./EditSupplierButton.svelte";
  import AddSupplierModal from "$lib/modals/addSupplierModal.svelte";
  import { modals } from "svelte-modals";
  import { _ } from "svelte-i18n";
  import { reload } from "$lib/stores/searches";

  let suppliers = $state();
  let searchQuery = $state("");
  let isSearching = $state(false);
  let searchTimeout = null;
  
  let currentPage = $state(1); // Current page number
  let pageSize = $state(10); // Number of items per page - default to 10
  let totalPages = $state(0);

  let isEditModalOpen = false;
  let selectedSupplier = null;

  $effect(() => {
    if ($reload) {
      fetchSuppliers();
      reload.set(false);
    }
  });

  // Effect to handle search query changes with debounce
  $effect(() => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      currentPage = 1; // Reset to first page when searching
      fetchSuppliers();
    }, 300); // 300ms debounce
  });

  onMount(() => {
    fetchSuppliers();
  });

  async function fetchSuppliers() {
    try {
      isSearching = true;
      let url = searchQuery.trim() 
        ? `/api/supplier/search?query=${encodeURIComponent(searchQuery)}&page=${currentPage - 1}&size=${pageSize}`
        : `/api/supplier?page=${currentPage - 1}&size=${pageSize}`;
      
      const response = await apiFetch(url);
      if (response.ok) {
        const data = await response.json();
        suppliers = data.content;
        totalPages = data.totalPages;
        currentPage = data.number + 1; // Adjust for 0-based index
      } else {
        throw new Error("Failed to fetch suppliers.");
      }
    } catch (error) {
      toast.push("An error occurred while fetching suppliers. Please try again.<br> Erreur:" + error);
    } finally {
      isSearching = false;
    }
  }

  const goToPage = (page) => {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    fetchSuppliers();
  };

  const clearSearch = () => {
    searchQuery = "";
    fetchSuppliers();
  };
</script>

<svelte:head>
  <title>{$_('admin.supplier_page.title')}</title>
</svelte:head>

<div class="p-8 space-y-10">
  <section class="bg-white shadow-lg rounded-xl p-6">
    <h2 class="text-2xl font-semibold mb-8">{$_('admin.supplier_page.title')}</h2>

    <div class="flex flex-col lg:flex-row items-start lg:space-x-6 space-y-6 lg:space-y-0 mb-8">
      <!-- Add Supplier Section -->
      <div class="flex-1 max-w-md bg-gray-50 border border-gray-300 rounded-lg p-6">
        <h3 class="text-lg font-medium mb-4">{$_('admin.supplier_page.button.add_supplier')}</h3>
        <button
          class="w-full bg-gradient-to-r from-blue-500 to-blue-700 text-white px-6 py-3 rounded-lg hover:scale-105 transform transition"
          onclick={() => {modals.open(AddSupplierModal); reload.set(true);}}
        >
          {$_('admin.supplier_page.button.add_supplier')}
        </button>
      </div>
    </div>

    <!-- Suppliers List Section -->
    <div class="bg-gray-50 border border-gray-300 rounded-lg p-6">
      <h3 class="text-lg font-medium mb-6">{$_('admin.supplier_page.table.title')}</h3>

      <!-- Search Bar -->
      <div class="mb-6 relative max-w-xs">
        <input
          type="text"
          bind:value={searchQuery}
          oninput={() => {currentPage = 1; fetchSuppliers();}}
          placeholder={$_('admin.supplier_page.placeholder.search')}
          class="w-full p-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
        {#if searchQuery}
          <button 
            class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
            onclick={clearSearch}
            aria-label="Clear search"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </button>
        {/if}
      </div>

      <!-- Loading indicator -->
      {#if isSearching}
        <div class="flex justify-center my-4">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-700"></div>
        </div>
      {/if}

      <!-- Suppliers Table -->
      <div class="overflow-x-auto">
        <table class="min-w-full table-auto border-collapse">
          <thead>
            <tr class="bg-gray-100 text-left text-sm font-semibold">
              <th class="px-6 py-4 w-96">{$_('admin.supplier_page.table.name')}</th>
              <th class="px-6 py-4 w-96">{$_('admin.supplier_page.table.sold')}</th>
              <th class="px-6 py-4 w-96">{$_('admin.supplier_page.table.status')}</th>
              <th class="px-6 py-4 w-36">{$_('admin.supplier_page.table.actions')}</th>
            </tr>
          </thead>
          <tbody>
            {#if suppliers && suppliers.length > 0}
              {#each suppliers as supplier (supplier.id)}
                <tr class="border-b hover:bg-gray-50 transition">
                  <td class="px-6 py-4">{supplier.name}</td>
                  <td class="px-6 py-4">
                    {#if supplier.soldByMd}
                      <span class="rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-700/10">{$_('admin.supplier_page.table.yes')}</span>
                    {:else}
                      <span class="rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-700/10">{$_('admin.supplier_page.table.no')}</span>
                    {/if}
                  </td>
                  <td class="px-6 py-4">
                    {#if supplier.closed}
                      <span class="rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-700/10">{$_('admin.supplier_page.table.closed')}</span>
                    {:else}
                      <span class="rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-700/10">{$_('admin.supplier_page.table.open')}</span>
                    {/if}
                  </td>
                  <td class="px-6 py-4 inline-flex rounded-md shadow-sm">
                    <EditSupplierButton supplier={supplier} />
                  </td>
                </tr>
              {/each}
            {:else}
              <tr>
                <td colspan="4" class="px-6 py-4 text-center text-gray-500">
                  {searchQuery ? `No suppliers found matching "${searchQuery}"` : 'No suppliers available'}
                </td>
              </tr>
            {/if}
          </tbody>
        </table>
      </div>

      <!-- Pagination Controls -->
      <div class="flex justify-between items-center mt-4">
        <!-- Items per page selector -->
        <div class="flex items-center gap-2">
          <span>{$_('logs.show')}</span>
          <select 
            bind:value={pageSize} 
            onchange={() => {goToPage(1)}}
            class="bg-white border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {#each [5, 10, 25, 50, 100] as size}
              <option value={size}>{size}</option>
            {/each}
          </select>
          <span>{$_('logs.items')}</span>
        </div>

        <!-- Pagination controls -->
        <div class="flex justify-center items-center space-x-4 absolute left-1/2 transform -translate-x-1/2">
          <button
            onclick={() => goToPage(currentPage - 1)}
            disabled={currentPage === 1}
            aria-label={$_('pagination.previous')}
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
          <div class="flex items-center gap-2">
            <span>{$_('pagination.page')}</span>
            <select 
              bind:value={currentPage} 
              onchange={() => goToPage(currentPage)}
              class="bg-white border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {#each Array(totalPages) as _, i}
                <option value={i + 1}>{i + 1}</option>
              {/each}
            </select>
            <span>{$_('pagination.on')} {totalPages}</span>
          </div>
          <button
            onclick={() => goToPage(currentPage + 1)}
            disabled={currentPage === totalPages}
            aria-label={$_('pagination.next')}
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
    </div>
  </section>
</div>