<script>
  import Loading from "$lib/Loading.svelte";
  import { apiFetch } from "$lib/utils/fetch";
  import { onMount } from "svelte";
  import AbbreviationButton from "./AbbreviationButton.svelte";
  import { createCombobox } from "svelte-headlessui";
  import Transition from "svelte-transition";
  import Icon from "@iconify/svelte";

  let abbreviations = $state();
  let non_existing_abbreviations = $state([]);
  let new_abbreviation = $state("");
  let searchQuery = "";
  let currentPage = $state(1);
  let pageSize = 4;
  let totalPages = $state(0);
  let loading = $state(true); // Track loading state

  $effect(() => {
    if (currentPage != null) {
      fetchAbbreviations();
    }
  });

  async function fetchAbbreviations() {
    try {
      const response = await apiFetch(
        "/api/category/abbreviation/all?page=" + currentPage + "&size=" + pageSize
      );
      if (response.ok) {
        const data = await response.json();
        abbreviations = data.content;
        totalPages = data.totalPages;
      } else {
        throw new Error("Failed to fetch abbreviations");
      }
    } catch (error) {
      console.error(error);
    } finally {
      loading = false; // Set loading to false once the fetch completes
    }
  }

  async function refreshAbbreviations() {
    loading = true; // Set loading to true before fetching
    await fetchAbbreviations();
    await fetchNonExistingAbbreviations();
  }

  async function fetchNonExistingAbbreviations() {
    try {
      const response = await apiFetch("/api/category/abbreviation/non-existing");
      if (response.ok) {
        let abbreviations_ns = await response.json();
        non_existing_abbreviations = abbreviations_ns.sort((a, b) => a.localeCompare(b));
      } else {
        throw new Error("Failed to fetch non-existing abbreviations");
      }
    } catch (error) {
      console.error(error);
    } finally {
      loading = false; // Set loading to false once the fetch completes
    }
  }

  async function addAbbreviation(value, abbreviation) {
    try {
      const response = await apiFetch("/api/category/abbreviation/add", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          value: value,
          abbreviation: abbreviation,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to add abbreviation");
      }
    } catch (error) {
      console.error(error);
    }

    refreshAbbreviations();
  }

  onMount(async () => {
    await fetchAbbreviations();
    await fetchNonExistingAbbreviations();
  });

  const combobox = createCombobox({ label: "Characteristics" });

  let filtered = $derived(
    non_existing_abbreviations.filter((a) =>
      a
        .toLowerCase()
        .replace(/\s+/g, "")
        .includes($combobox.filter.toLowerCase().replace(/\s+/g, ""))
    )
  );
</script>

<svelte:head>
  <title>Abbreviations</title>
  <meta name="description" content="Abbreviations management page" />
</svelte:head>

<div class="p-8 space-y-10">
  <section class="p-6">
    <h2 class="text-2xl font-semibold mb-8 bg-white">Gestion des abbréviations</h2>

    <!-- Abbreviations List Section -->
    <div class="bg-gray-50 border border-gray-300 rounded-lg p-6 size-fit">
      <h3 class="text-lg font-medium">Abbréviations</h3>

      <div class=" my-6 flex flex-row items-end">
        <!-- Search Bar -->
        <!-- <div class="max-w-xs mx-8">
          <label for="search" class="block my-1 text-sm font-medium text-gray-900">Recherche</label>
          <input
            type="text"
            id="search"
            bind:value={searchQuery}
            placeholder="Recherche"
            class="w-full py-2 px-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div> -->
        <!-- Add Abbreviation Button -->
        <div class="max-w-xs mx-2">
          <label for="add-abbreviation" class="block my-1 text-sm font-medium text-gray-900">
            {#if !$combobox.selected}
              Ajouter une abréviation
            {:else}
              Complet
            {/if}
          </label>
          <div class="relative w-full">
            <input
              use:combobox.input
              class="w-full py-2 pr-10 px-4 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              value={$combobox.selected ?? ""}
              id="add-abbreviation"
              placeholder="Ajouter une abréviation"
            />
            <button
              use:combobox.button
              type="button"
              class="absolute inset-y-0 right-2 flex items-center"
            >
              <Icon icon="material-symbols:expand-more" width="24" height="24" />
            </button>
          </div>

          <Transition
            show={$combobox.expanded}
            leave="transition ease-in duration-100"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
            on:after-leave={() => combobox.reset()}
          >
            <ul
              use:combobox.items
              class="absolute mt-1 max-h-60 overflow-auto rounded-md bg-white py-1 text-sm ring-1 shadow-lg ring-black/5 focus:outline-hidden"
            >
              {#each filtered as value}
                <li
                  class="relative cursor-default p-2 px-4 select-none text-gray-900 hover:bg-teal-500"
                  use:combobox.item={{ value }}
                >
                  <span class="block truncate font-normal'}">{value}</span>
                </li>
              {:else}
                <li class="relative cursor-default select-none py-2 pl-10 pr-4 text-gray-900">
                  <span class="block truncate font-normal">Pas de résultats</span>
                </li>
              {/each}
            </ul>
          </Transition>
        </div>
        {#if $combobox.selected}
          <div class="mx-0 max-w-xs">
            <label class="block my-1 text-sm font-medium text-gray-900">Abbréviation</label>
            <input
              type="text"
              bind:value={new_abbreviation}
              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
            />
          </div>
          <div class="mx-2 max-w-xs">
            <button
              class="bg-blue-600 text-white p-2.5 rounded-lg hover:bg-blue-700 flex items-center transform transition"
              onclick={() => {
                addAbbreviation($combobox.selected, new_abbreviation);
                new_abbreviation = "";
                $combobox.selected = "";
              }}
            >
              <span>
                <Icon icon="material-symbols:add" width="24" height="24" />
              </span>
              <span class="pl-2">Ajouter</span>
            </button>
          </div>
        {/if}
      </div>

      <!-- Abbreviations Table -->
      <div class="overflow-x-auto">
        <table class="table-fixed border-collapse">
          <thead>
            <tr class="bg-gray-100 text-left text-sm font-semibold">
              <th class="px-6 py-4 w-96">Complet</th>
              <th class="px-6 py-4 w-96">Abbréviation</th>
              <th class="px-6 py-4 w-96">Actions</th>
            </tr>
          </thead>
          <tbody>
            {#if loading}
              <!-- Loading Message -->
              <tr class="border-b hover:bg-gray-50 transition">
                <td class="px-6 py-10" colspan="3">Chargement... <Loading /></td>
              </tr>
            {:else if abbreviations.length === 0}
              <!-- No abbreviations found message -->
              <tr class="border-b hover:bg-gray-50 transition">
                <td class="px-6 py-4 text-red-500" colspan="3">Aucune abréviation trouvée.</td>
              </tr>
            {:else}
              <!-- Abbreviations List -->
              {#each abbreviations as abbreviation}
                <tr class="border-b hover:bg-gray-50 transition">
                  <td class="px-6 py-4">{abbreviation.value}</td>
                  <td class="px-6 py-4">{abbreviation.abbreviation}</td>
                  <td class="px-6 py-4">
                    <AbbreviationButton {abbreviation} {refreshAbbreviations} />
                  </td>
                </tr>
              {/each}
            {/if}
          </tbody>
        </table>
      </div>

      <!-- Pagination Controls -->
      <div class="flex justify-center items-center space-x-4 mt-4">
        <button
          onclick={() => (currentPage -= 1)}
          disabled={currentPage === 1}
          aria-label="Précédent"
        >
          <Icon icon="material-symbols:arrow-back-ios-rounded" width="24" height="24" />
        </button>
        <span>Page {currentPage} sur {totalPages}</span>
        <button
          onclick={() => (currentPage += 1)}
          disabled={currentPage === totalPages}
          aria-label="Suivant"
        >
          <Icon icon="material-symbols:arrow-forward-ios-rounded" width="24" height="24" />
        </button>
      </div>
    </div>
  </section>
</div>
