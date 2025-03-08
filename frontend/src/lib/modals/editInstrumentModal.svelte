<script>
    import { PUBLIC_API_URL } from "$env/static/public";
    import { onMount, getContext } from "svelte";
    import { reload } from "$lib/stores/searches";
  
    // Destructure the props provided by <Modals />
    const {
      isOpen, // Indicates if the modal is open
      close,  // Function to close the modal
      instrument, // The instrument data passed as a prop
    } = $props();
  
    let file; // Variable to store the selected file


    let reference = $state(instrument.reference); // State for the instrument reference
    let characteristics = $state([]); // State for the instrument characteristics
  
    // Function to handle form submission
    async function handleSubmit(event) {
      event.preventDefault(); // Prevent the default form submission behavior
  
      // If a file is selected, upload it
      if (file) {
        try {
          const fileData = new FormData();
          fileData.append("file", file);
          const response = await fetch(
            PUBLIC_API_URL + "/api/instrument/" + encodeURIComponent(instrument.id) + "/picture",
            {
              method: "POST",
              body: fileData,
            }
          );
          if (!response.ok) {
            throw new Error("Failed to update the image");
          }
        } catch (error) {
          console.error("Error:", error);
        }
      }
  
      // If characteristics are edited, update them
      if (characteristicsEdited) {
        try {
          const response = await fetch(
            PUBLIC_API_URL + "/api/instrument/" + encodeURIComponent(instrument.id),
            {
              method: "PATCH",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify(
            Object.fromEntries(characteristics.map(c => [c.name, c.value])) // Convert array back to object
          ),
            }
          );
          console.log(response);
          if (!response.ok) {
            throw new Error("Failed to update the instrument characteristics");
          }
        } catch (error) {
          console.error("Error:", error);
        }
      }
      reload.set(true); // Trigger a reload
      close(); // Close the modal
    }
  
    // Function to fetch the characteristics of the instrument
    async function fetchCharacteristics() {
  try {
    const response = await fetch(
      PUBLIC_API_URL + "/api/instrument/" + encodeURIComponent(instrument.id)
    );
    if (!response.ok) {
      throw new Error(`Failed to fetch characteristics: ${response.statusText}`);
    }

    const data = await response.json();

    // Convert object to an array of { name, value }
    characteristics = Object.entries(data).map(([key, value]) => ({
      name: key,
      value: value
    }));

    console.log(characteristics);
  } catch (error) {
    console.error(error);
  }
}
  
    let characteristicsEdited = false; // Flag to track if characteristics are edited
    console.log(characteristics);
    console.log("caractéristiques ou pas ?");
    //let promise = fetch(PUBLIC_API_URL + "/api/instrument/" + encodeURIComponent(instrument.id)).then(characteristics => characteristics.json()); // Fetch characteristics on component mount
    let promise = fetchCharacteristics();
    console.log(characteristics);
    console.log("Toujours pas ?");
</script>
  
{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>
  
    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
        <div
          class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg  lg:max-w-4xl"
        >
          <form class="max-w-4xl mx-auto" onsubmit={handleSubmit}>
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <div class="sm:flex sm:items-start">
                <div
                  class="mx-auto flex size-12 shrink-0 items-center justify-center rounded-full bg-orange-100 sm:mx-0 sm:size-10"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="w-5 h-5"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M12 20h9" />
                    <path d="M16.5 3.5a2.121 2.121 0 1 1 3 3L6 20l-4 1 1-4L16.5 3.5z" />
                  </svg>
                </div>
                <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                  <h3 class="text-base font-semibold text-gray-900" id="modal-title">
                    Edit an instrument
                  </h3>
                  {#await promise}
                    <div role="status" class="my-6 flex items-center justify-center">
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
                      <span class="sr-only">Loading...</span>
                    </div>
                  {:then}
                    <div class="mt-2">
                      <label class="block my-2 text-sm font-medium text-gray-900" for="user_avatar"
                        >Reference</label
                      >
                      <input
                        type="text"
                        bind:value={reference}
                        class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
                      />

                      <label class="block my-2 text-sm font-medium text-gray-900" for="user_avatar"
                        >Image</label
                      >
                      <input
                        class="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none p-2.5"
                        type="file"
                        onchange={(e) => (file = e.target.files[0])}
                      />
                      {#if !instrument.pictureId}
                        <div class="mt-1 text-sm text-red-500">
                          An image already exists for this instrument, by indicating an image above, the current image will be deleted.
                        </div>
                      {/if}
                    </div>
                    <div class="my-2">
                      <h3 class="text-base text-gray-900">Characteristics</h3>
                      <div class="grid grid-cols-2 gap-4">
                        {#each characteristics as characteristic}
                          <div>
                            <label class="text-sm font-medium text-gray-900" for="user_avatar"
                              >{characteristic.name}</label
                            >
                            <input
                              type="text"
                              bind:value={characteristic.value} onchange={() => (characteristicsEdited = true)}
                              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
                            />
                          </div>
                        {/each}
                      </div>
                    </div>
                  {/await}
                </div>
              </div>
              <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                <button
                  type="submit"
                  class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto"
                  >Save</button
                >
                <button
                  type="button"
                  class="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50 sm:mt-0 sm:w-auto"
                  onclick={() => close()}>Cancel</button
                >
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
{/if}