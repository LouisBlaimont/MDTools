<script>
  import { goto } from "$app/navigation";
  import { apiFetch } from "$lib/utils/fetch";
  import Icon from "@iconify/svelte";
  import Loading from "$lib/Loading.svelte";
  import { createListbox } from "svelte-headlessui";
  import { Transition } from "svelte-transition";
  import { reload } from "$lib/stores/searches";
  import { _ } from "svelte-i18n";

  // Destructure the props provided by <Modals />
  const {
    isOpen, // Indicates if the modal is open
    close,  // Function to close the modal
    roles,
    user,  // The user object to be edited
    message,
  } = $props();

  const rolesUpper = roles.map((role) => role.toUpperCase());
  const listbox = createListbox({ label: "Roles", selected: user.roles });

  let characteristics = $state([]);
  let users = $state([]);

  let inputSize;

  
  // Function to handle form submission
  async function handleSubmit(event) {
    event.preventDefault();
    try {
      const characteristicsObject = Object.fromEntries(
        characteristics.map(c => [c.name, c.value])
      );

      const requestBody = {
        ...characteristicsObject, // Spread the characteristics (e.g., username, email)
        roles: $listbox.selected, // Add the selected roles
      };

      await apiFetch(`/api/user/${encodeURIComponent(user.id)}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      reload.set(true); // Trigger a reload of the users list
      close();
      setTimeout(() => {
        reload.set(true);
        goto("/webmaster", {
          replaceState: true,
        });
      }, 100);
    } catch (error) {
      console.error("Failed to update user:", error);
    }
  }

  function onChange(e) {
    console.log("select", e.detail.selected);
  }

  function canceling(){
    close();
  }

  // Function to fetch the user data
  async function fetchUser() {
    try {
      const response = await apiFetch("/api/user?user_id=" + encodeURIComponent(user.id), {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
      );
      if (!response.ok) {
        throw new Error(`Failed to fetch user: ${response.statusText}`);
      }

      const data = await response.json();

      // Convert object to an array of { name, value }
      characteristics = Object.entries(data).map(([key, value]) => ({
        name: key,
        value: value
      }));

    } catch (error) {
      console.error(error);
    }
  }

  let userEdited = false;

  // Computed property for binding
  let userBoundValue = $state(null);
  let promise = fetchUser();

  // Function to handle instrument deletion
  async function handleDelete() {
      if (confirm($_('modals.edituser.confirm'))) {
          try {
              const response = await apiFetch("/api/user/" + encodeURIComponent(user.username), {
                  method: "DELETE",
              });
              if (!response.ok) {
                  throw new Error("Failed to delete the user");
              }
              close(); // Close the modal
              reload.update(value => !value); // Toggle the reload store to trigger reactivity
          } catch (error) {
              console.error("Error:", error);
          }
      }
  }

  // Dragging functionality to match addModal
  let posX = $state(0);
  let posY = $state(0); 
  let offsetX = 0;
  let offsetY = 0;
  let isDragging = false;

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
  
</script>

{#if isOpen}
<div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div 
        class="fixed inset-0 z-10 flex items-center justify-center"
        onmousemove={drag}
        onmouseup={stopDrag}
    >
        <div 
            class="bg-white rounded-lg shadow-lg max-h-[80vh] overflow-y-auto absolute"
            style="transform: translate({posX}px, {posY}px); max-width: 40vw;"
        >
            <div 
                class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg select-none"
                onmousedown={startDrag}
            >
                <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.edituser.modif')}{user.username}</h2>
                <!-- Edit Icon -->
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="teal-500"
                  version="1.1"
                  id="Capa_1"
                  viewBox="0 0 494.936 494.936"
                  class="w-6 h-6 ml-2"
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
            {#if message}
              <p class="text-red-400 text-lg mt-4 ml-3">{message}</p>
            {/if}
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
                    <span class="sr-only">{$_('modals.edituser.loading')}</span>
                </div>
            {:then}
                <form onsubmit={handleSubmit} preventDefault class="bg-gray-100 p-6 rounded-b-lg">
                    <div class="mt-2">
                      {#each characteristics as characteristic}
                        <div>
                          {#if characteristic.name === "username"}
                            <!-- Division for Username -->
                            <div class="space-y-2">
                              <label for="username" class="font-semibold text-lg">{$_('modals.edituser.username')}</label>
                              <input
                                type="text"
                                id="username"
                                bind:value={characteristic.value}
                                bind:this={inputSize}
                                onchange={() => (userEdited = true)}
                                class="w-full p-2 mt-1 mb-3 border rounded"
                                placeholder={$_('modals.edituser.enter_name')}
                              />
                          </div>
                          {:else if characteristic.name === "email"}

                          <!-- Division for Email -->
                          <div class="space-y-2 mt-2">
                            <label for="email" class="font-semibold text-lg">{$_('modals.edituser.email')}</label>
                            <!-- svelte-ignore event_directive_deprecated -->
                            <input
                              type="email"
                              id="email"
                              bind:value={characteristic.value}
                              bind:this={inputSize}
                              onchange={() => (userEdited = true)}
                              class="w-full p-2 mt-1 mb-3 border rounded"
                              placeholder={$_('modals.edituser.email')}
                            />
                        </div>
                      {:else if characteristic.name === "roles"}
                          <!-- Division for Roles -->
                            <div class="space-y-2 mt-2">
                              <label for="roles" class="font-semibold text-lg">{$_('modals.edituser.role')}</label>
                              <div class="relative">
                              <span class="inline-block w-full">
                                  <button
                                  use:listbox.button
                                  onchange={onChange}
                                  class="focus:shadow-outline-blue relative w-full cursor-default rounded-md border border-gray-300 bg-white py-2 pr-10 pl-2 text-left text-sm transition duration-150 ease-in-out focus:border-blue-300 focus:outline-hidden sm:leading-5"
                                  >
                                  <div class="flex flex-wrap gap-2">
                                      {#each $listbox.selected as selected}
                                      <span class="flex items-center gap-1 rounded-sm bg-blue-50 px-2 py-0.5">
                                          <span>{selected}</span>
                                          {#if selected !== "ROLE_USER"}
                                          <div use:listbox.deselect={selected}>
                                              <Icon icon="material-symbols:close-rounded" width="24" height="24" />
                                          </div>
                                          {/if}
                                      </span>
                                      {:else}
                                      <span class="flex items-center gap-1 rounded-sm px-2 py-0.5">{$_('modals.edituser.empty')}</span>
                                      {/each}
                                  </div>
                                  <span
                                      class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2"
                                  >
                                      <Icon icon="material-symbols:expand-all-rounded" width="24" height="24" />
                                  </span>
                                  </button>
                              </span>

                              <Transition
                                  show={$listbox.expanded}
                                  leave="transition ease-in duration-100"
                                  leaveFrom="opacity-100"
                                  leaveTo="opacity-0"
                              >
                                  <ul
                                  use:listbox.items
                                  class="absolute mt-1 w-full max-h-60 overflow-y-auto rounded-md bg-white py-1 text-sm ring-1 shadow-lg ring-black/5 focus:outline-hidden"
                                  >
                                  {#each roles as value}
                                      {@const active = $listbox.active === value}
                                      {@const selected = $listbox.selected.includes(value)}
                                      {#if value != "ROLE_USER"}
                                      <li
                                          class="relative cursor-default py-2 pr-9 pl-4 select-none focus:outline-hidden {active
                                          ? 'bg-blue-100 text-blue-900'
                                          : 'text-gray-900'}"
                                          use:listbox.item={{ value }}
                                      >
                                          <span class="block truncate {selected ? 'font-semibold' : 'font-normal'}">
                                            {value}
                                          </span>
                                          {#if selected}
                                          <span
                                              class="absolute inset-y-0 right-0 flex items-center pr-3 text-blue-600"
                                          >
                                              <Icon icon="material-symbols:check-rounded" width="24" height="24" />
                                          </span>
                                          {/if}
                                      </li>
                                      {/if}
                                  {/each}
                                  </ul>
                              </Transition>
                              </div>
                          </div>
                        {/if}
                        </div>
                      {/each}
                      <div>
                    
                    <div class="mt-4 space-x-4 flex justify-end">
                        <button type="button" onclick={handleDelete} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">{$_('modals.edituser.delete')}</button>
                        <button type="button" onclick={canceling} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">{$_('modals.edituser.cancel')}</button>
                        <button type="submit" class="bg-teal-500 text-white px-4 py-2 rounded hover:bg-teal-700">{$_('modals.edituser.save')}</button>
                    </div>
                </form>
            {/await}
        </div>
    </div>
</div>
{/if}