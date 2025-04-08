<script>
  import { apiFetch } from "$lib/utils/fetch";
  import { toast } from "@zerodevx/svelte-toast";
  import { onMount } from "svelte";
  import { writable } from "svelte/store";
  import EditSupplierButton from "./EditSupplierButton.svelte";
  import AddSupplierModal from "$lib/modals/addSupplierModal.svelte";
  import { modals } from "svelte-modals";

  let suppliers = $state();
  let searchQuery = "";
  let currentPage = 1;
  let totalPages = 0;
  const itemsPerPage = 10;

  let isEditModalOpen = false;
  let selectedSupplier = null;

  async function fetchSuppliers(page = 1) {
    try {
      const response = await apiFetch(`/api/supplier?page=${page - 1}&size=${itemsPerPage}`);
      if (response.ok) {
        const data = await response.json();
        suppliers = data.content;
        totalPages = data.totalPages;
      } else {
        throw new Error("Failed to fetch suppliers.");
      }
    } catch (error) {
      toast.push("An error occurred while fetching suppliers. Please try again.<br> Erreur:" + error);
    }
  }

  function goToPage(page) {
    if (page >= 1 && page <= totalPages) {
      currentPage = page;
      fetchSuppliers(currentPage);
    }
  }

  function openEditModal(supplier) {
    selectedSupplier = { ...supplier };
    isEditModalOpen = true;
  }

  function closeEditModal() {
    isEditModalOpen = false;
    selectedSupplier = null;
  }

  async function saveSupplier() {
    try {
      const response = await apiFetch(`/api/supplier/${selectedSupplier.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(selectedSupplier),
      });
      if (response.ok) {
        toast.push("Fournisseur mis à jour avec succès.");
        fetchSuppliers(currentPage);
        closeEditModal();
      } else {
        throw new Error("Failed to update supplier.");
      }
    } catch (error) {
      toast.push("Une erreur s'est produite lors de la mise à jour du fournisseur.<br> Erreur:" + error);
    }
  }

  onMount(() => {
    fetchSuppliers();
  });
</script>

<svelte:head>
  <title>Gestion des fournisseurs</title>
</svelte:head>

<div class="p-8 space-y-10">
  <section class="bg-white shadow-lg rounded-xl p-6">
    <h2 class="text-2xl font-semibold mb-8">Gestion des fournisseurs</h2>

    <div class="flex flex-col lg:flex-row items-start lg:space-x-6 space-y-6 lg:space-y-0 mb-8">
      <!-- Add Supplier Section -->
      <div class="flex-1 max-w-md bg-gray-50 border border-gray-300 rounded-lg p-6">
        <h3 class="text-lg font-medium mb-4">Ajouter un fournisseur</h3>
        <button
          class="w-full bg-gradient-to-r from-blue-500 to-blue-700 text-white px-6 py-3 rounded-lg hover:scale-105 transform transition"µ
          onclick={() => modals.open(AddSupplierModal)}
        >
          Ajouter un fournisseur
        </button>
      </div>
    </div>

    <!-- Suppliers List Section -->
    <div class="bg-gray-50 border border-gray-300 rounded-lg p-6">
      <h3 class="text-lg font-medium mb-6">Liste des fournisseurs</h3>

      <!-- Search Bar -->
      <div class="mb-6 max-w-xs">
        <input
          type="text"
          bind:value={searchQuery}
          placeholder="Rechercher un fournisseur"
          class="w-full p-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <!-- Suppliers Table -->
      <div class="overflow-x-auto">
        <table class="min-w-full table-auto border-collapse">
          <thead>
            <tr class="bg-gray-100 text-left text-sm font-semibold">
              <th class="px-6 py-4 w-96">Nom du fournisseur</th>
              <th class="px-6 py-4 w-96">Vendu</th>
              <th class="px-6 py-4 w-96">Statut</th>
              <th class="px-6 py-4 w-36">Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each suppliers as supplier (supplier.id)}
              <tr class="border-b hover:bg-gray-50 transition">
                <td class="px-6 py-4">{supplier.name}</td>
                <td class="px-6 py-4">
                  {#if supplier.soldByMd}
                    <span class="rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-700/10">Oui</span>
                  {:else}
                    <span class="rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-700/10">Non</span>
                  {/if}
                </td>
                <td class="px-6 py-4">
                  {#if supplier.closed}
                    <span class="rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-700/10">Fermé</span>
                  {:else}
                    <span class="rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-700/10">Ouvert</span>
                  {/if}
                </td>
                <td class="px-6 py-4 inline-flex rounded-md shadow-sm">
                  <EditSupplierButton supplier={supplier} />
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
