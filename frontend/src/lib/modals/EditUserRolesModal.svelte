<script>
  import { createListbox } from "svelte-headlessui";
  import Transition from "svelte-transition";
  import Icon from "@iconify/svelte";
  import { onMount } from "svelte";
  import { apiFetch } from "$lib/utils/fetch";

  const {
    // provided by <Modals />
    isOpen,
    close,

    // your props
    user,
    roles
  } = $props();

  const rolesUpper = roles.map((role) => role.toUpperCase());
  console.log("roles", rolesUpper);

  const listbox = createListbox({ label: "Roles", selected: user.roles });

  async function handleSubmit(event) {
    event.preventDefault();
    console.log("submit", $listbox.selected);
   //  user.roles = $listbox.selected;
    apiFetch(`/api/user/`+ encodeURI(user.id) +`/roles`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify($listbox.selected),
    });
    close();
  }

  function onChange(e) {
    console.log("select", e.detail.selected);
  }
</script>

{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>

    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
        <div
          class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg"
          style:height={$listbox.expanded ? "auto" : "initial"}
        >
          <form onsubmit={handleSubmit} class="max-w-md mx-auto bg-white p-6 rounded-2xl space-y-4">
            <h2 class="text-xl font-semibold text-gray-700">
              Modifier les rôles de {user.username}
            </h2>

            <div class="relative pt-2 pb-6">
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
                      <span class="flex items-center gap-1 rounded-sm px-2 py-0.5">Empty</span>
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
                  class="absolute mt-1 w-full overflow-auto rounded-md bg-white py-1 text-sm ring-1 shadow-lg ring-black/5 focus:outline-hidden"
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

            <div class="flex gap-4">
              <button
                type="submit"
                class="w-full bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 transition"
                onsubmit={() => handleSubmit()}
              >
                Sauvegarder
              </button>
              <button
                onclick={() => close()}
                class="w-full bg-gray-300 text-gray-800 py-2 px-4 rounded-lg hover:bg-gray-400 transition"
              >
                Annuler
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
{/if}
