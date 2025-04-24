<script>
  import { onMount } from "svelte";
  import { apiFetch } from "$lib/utils/fetch";
  import { reload } from "$lib/stores/searches";
  import { goto } from "$app/navigation";
  import { _ } from "svelte-i18n";

  const {
    isOpen, // Indicates if the modal is open
    close,  // Function to close the modal
    supplier, // The instrument data passed as a prop
  } = $props();

  let name = supplier.name;
  let details = $state([]);
  let detailsEdited = false;

  let inputSize;

  let posX = $state(0);
  let posY = $state(0);
  let offsetX = 0;
  let offsetY = 0;
  let isDragging = false;

  let autocompleteOptions = $state({});
  let autocompleteInput = $state("");
  let filteredAutocompleteOptions = $state([]);
  let showAutocompleteDropdown = $state(false);
  let currentAutocompleteField = $state(null);

  async function fetchDetails() {
    try {
      const response = await apiFetch(`/api/supplier/${encodeURIComponent(supplier.id)}`);
      if (!response.ok) throw new Error("Failed to fetch supplier details");
      const data = await response.json();
      details = Object.entries(data).map(([key, value]) => ({ name: key, value }));

      details = details.filter(detail => detail.name !== "id");
    } catch (error) {
      console.error(error);
    }
  }

  async function fetchAutocompleteOptions(characteristic) {
    try {
      const charateritsicEndpoints = {
        'name': {
            endpoint: `supplier`,
            extractValue: (item) => item.name,
        }
      }
      const config = charateritsicEndpoints[characteristic];
      if (!config) {
            console.warn(`No endpoint configured for ${characteristic}`);
            return;
      }

      const response = await apiFetch(`/api/${config.endpoint}/all`, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error(`Failed to fetch options}`);

      const options = await response.json();

      // Extract values using the provided function
      const extractedOptions = options.map(config.extractValue);

      autocompleteOptions[characteristic] = extractedOptions;
      return extractedOptions;
    } catch (error) {
      console.error(error);
    }
  }

  async function triggerAutocomplete(characteristicName) {
    if (currentAutocompleteField === characteristicName && showAutocompleteDropdown) {
      // If the same field is clicked again while dropdown is open, close it
      showAutocompleteDropdown = false;
      currentCategory = null;
      currentAutocompleteField = null;
      return;
    }
    // If a different field is clicked, fetch options for the new field
    currentAutocompleteField = characteristicName;
    
    // Fetch options if not already loaded
    if (!autocompleteOptions[characteristicName]) {
      await fetchAutocompleteOptions(characteristicName);
    }

    autocompleteInput = "";
    filteredAutocompleteOptions = (autocompleteOptions[characteristicName] || []);

    showAutocompleteDropdown = true;
}

  function handleAutocompleteInput(event) {
    const inputValue = event.target.value;
    autocompleteInput = inputValue;
    showAutocompleteDropdown = true;
    filteredAutocompleteOptions = (autocompleteOptions[currentAutocompleteField] || []).filter(option =>
      String(option).toLowerCase().includes(inputValue.toLowerCase())
    );
  }

  function selectAutocompleteOption(option) {
    const detailIndex = details.findIndex(d => d.name === currentAutocompleteField);
    if (detailIndex !== -1) {
      details[detailIndex].value = option;
      detailsEdited = true;
    }
    autocompleteInput = "";
    showAutocompleteDropdown = false;
    currentAutocompleteField = null;
  }

  function closeAutocomplete() {
    showAutocompleteDropdown = false;
    currentAutocompleteField = null;
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (detailsEdited) {
      try {
        const detailsData = Object.fromEntries(details.map(d => [d.name, d.value]));
        const response = await apiFetch(`/api/supplier/${encodeURIComponent(supplier.id)}`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(detailsData),
        });
        if (!response.ok) throw new Error("Failed to update supplier details");
      } catch (error) {
        console.error(error);
      }
    }

    reload.set(true);
    close();
  }

  async function handleDelete() {
    if (confirm("Êtes-vous sûr de vouloir supprimer ce fournisseur ?")) {
      try {
        const response = await apiFetch(`/api/supplier/${encodeURIComponent(supplier.id)}`, {
          method: "DELETE",
        });
        if (!response.ok) throw new Error("Failed to delete the supplier");
        close();
        reload.set(true);
      } catch (error) {
        console.error(error);
      }
    }
  }

  function startDrag(event) {
    isDragging = true;
    offsetX = event.clientX - posX;
    offsetY = event.clientY - posY;
  }

  function drag(event) {
    if (isDragging) {
      posX = event.clientX - offsetX;
      posY = event.clientY - offsetY;
    }
  }

  function stopDrag() {
    isDragging = false;
  }

  let promise = fetchDetails();
</script>

{#if isOpen}
  <div
    class="relative z-10"
    aria-labelledby="modal-title"
    role="dialog"
    aria-modal="true"
  >
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div
      class="fixed inset-0 z-10 flex items-center justify-center"
      on:mousemove={drag}
      on:mouseup={stopDrag}
  >
      <div 
          class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
          style="transform: translate({posX}px, {posY}px);"
      >
      <div
          class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg"
          on:mousedown={startDrag}
      >
        <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.edit_supplier.modif')}{name}</h2>
        <!-- Edit Icon -->
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="teal-500"
          version="1.1"
          id="Capa_1"
          viewBox="0 0 494.936 494.936"
          class="w-6 h-6"
        >
          <g>
            <g>
              <path
                d="M389.844,182.85c-6.743,0-12.21,5.467-12.21,12.21v222.968c0,23.562-19.174,42.735-42.736,42.735H67.157 c-23.562,0-42.736-19.174-42.736-42.735V150.285c0-23.562,19.174-42.735,42.736-42.735h267.741c6.743,0,12.21-5.467,12.21-12.21 s-5.467-12.21-12.21-12.21H67.157C30.126,83.13,0,113.255,0,150.285v267.743c0,37.029,30.126,67.155,67.157,67.155h267.741 c37.03,0,67.156-30.126,67.156-67.155V195.061C402.054,188.318,396.587,182.85,389.844,182.85z"
              />
              <path
                d="M483.876,20.791c-14.72-14.72-38.669-14.714-53.377,0L221.352,229.944c-0.28,0.28-3.434,3.559-4.251,5.396l-28.963,65.069 c-2.057,4.619-1.056,10.027,2.521,13.6c2.337,2.336,5.461,3.576,8.639,3.576c1.675,0,3.362-0.346,4.96-1.057l65.07-28.963 c1.83-0.815,5.114-3.97,5.396-4.25L483.876,74.169c7.131-7.131,11.06-16.61,11.06-26.692 C494.936,37.396,491.007,27.915,483.876,20.791z M466.61,56.897L257.457,266.05c-0.035,0.036-0.055,0.078-0.089,0.107 l-33.989,15.131L238.51,247.3c0.03-0.036,0.071-0.055,0.107-0.09L447.765,38.058c5.038-5.039,13.819-5.033,18.846,0.005 c2.518,2.51,3.905,5.855,3.905,9.414C470.516,51.036,469.127,54.38,466.61,56.897z"
              />
            </g>
          </g>
        </svg>
      </div>
      {#await promise}
        <div role="status" class="my-6 flex items-center justify-center p-4">
            <svg
                aria-hidden="true"
                class="w-16 h-16 text-gray-200 animate-spin dark:text-gray-600 fill-blue-600"
                viewBox="0 0 100 101"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
            >
                <path
                    d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"
                    fill="currentColor"
                />
                <path
                    d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"
                    fill="currentFill"
                />
            </svg>
            <span class="sr-only">{$_('modals.edit_supplier.loading')}.</span>
        </div>
      {:then} 
      <form on:submit|preventDefault={handleSubmit} class="bg-gray-100 p-6 rounded-b-lg">
          {#each details as detail}
            {#if detail.name !== "id"}
                <div>
                {#if detail.name === "name"}
                    <label for="name" class="font-semibold text-lg">{$_('modals.edit_supplier.name')}</label>
                    <input
                      type="text"
                      bind:value={detail.value}
                      bind:this={inputSize}
                      on:change={() => (detailsEdited = true)}
                      on:focus={() => {
                          currentAutocompleteField = detail.name;
                          triggerAutocomplete(detail.name);
                      }}
                      on:input={handleAutocompleteInput}
                      on:blur={closeAutocomplete}
                      class="w-full p-2 border rounded mb-4"
                      placeholder="Entrer le nom du fournisseur"
                    />
                    {#if showAutocompleteDropdown && currentAutocompleteField === detail.name}
                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                    <ul
                        class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto"
                        style="width: {inputSize?.offsetWidth || 'auto'}px;"
                        on:mousedown={event => event.preventDefault()}
                    >
                        {#each filteredAutocompleteOptions as option}
                        <li
                            class="px-4 py-2 hover:bg-gray-200 cursor-pointer"
                            on:click={() => selectAutocompleteOption(option)}
                        >
                            {option}
                        </li>
                        {/each}
                    </ul>
                    {/if}
                {:else if detail.name === "soldByMd"}
                    <label for="soldByMd" class="font-semibold text-lg">{$_('modals.edit_supplier.sold')}</label>
                    <div class="flex gap-4 mb-4">
                    <label>
                        <input
                            type="radio"
                            bind:group={detail.value}
                            value={true}
                            on:change={() => (detailsEdited = true)}
                        />
                        {$_('modals.edit_supplier.yes')}
                    </label>
                    <label>
                        <input
                        type="radio"
                        bind:group={detail.value}
                        value={false}
                        on:change={() => (detailsEdited = true)}
                        />
                        {$_('modals.edit_supplier.no')}
                    </label>
                    </div>
                {:else if detail.name === "closed"}
                    <label for="closed" class="font-semibold text-lg">{$_('modals.edit_supplier.status')}</label>
                    <div class="flex gap-4 mb-4">
                    <label>
                        <input
                        type="radio"
                        bind:group={detail.value}
                        value={true}
                        on:change={() => (detailsEdited = true)}
                        />
                        {$_('modals.edit_supplier.closed')}
                    </label>
                    <label>
                        <input
                        type="radio"
                        bind:group={detail.value}
                        value={false}
                        on:change={() => (detailsEdited = true)}
                        />
                        {$_('modals.edit_supplier.open')}
                    </label>
                    </div>
                {:else}
                    <label class="font-semibold text-lg">{detail.name}:</label>
                    <input
                    type="text"
                    bind:value={detail.value}
                    on:change={() => (detailsEdited = true)}
                    class="w-full p-2 border rounded mb-4"
                    />
                {/if}
                </div>
            {/if}
          {/each}
        
        <div class="flex justify-end gap-4 mt-4">
          <button type="button" on:click={handleDelete} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">{$_('modals.edit_supplier.delete')}</button>
          <button type="button" on:click={close} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">{$_('modals.edit_supplier.cancel')}</button>
          <button type="submit" class="bg-teal-500 text-white px-4 py-2 rounded hover:bg-teal-700">{$_('modals.edit_supplier.save')}</button>
        </div>
      </form>
      {/await}
    </div>
  </div>
</div>
{/if}
