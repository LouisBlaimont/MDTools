<script>
    import { selectedCategoryIndex, alternatives, categories, hoveredAlternativeIndex} from "$lib/stores/searches";
    import { modals } from "svelte-modals";
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { selectAlternative, removeAlternative } from "$lib/components/alternatives.js";
    import { isAdmin } from "$lib/stores/user_stores";
    import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";

    const category = $categories[$selectedCategoryIndex]; 
  </script>
  
  <div class="flex justify-center items-center p-4">
    <div class="bg-white shadow-lg rounded-lg p-6 w-full max-w-6xl border border-gray-300 flex flex-col">
        <div class="mb-6 border-b pb-4">
            <h2 class="text-lg font-bold text-black">{$_('alt.type')}</h2>
            <div class="inline-flex space-x-0 border-2 border-black rounded-lg">
                <div class="px-4 py-2 border-r-2 border-black">{category.groupName}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.subGroupName}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.function}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.name}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.shape}</div>
                <div class="px-4 py-2">{category.lenAbrv}</div>

            </div>

            <h2 class="text-lg font-bold text-black mt-4">{$_('alt.alternatives')}</h2>
            <table class="w-full border border-gray-200 text-sm">
            <thead>
              <tr class="bg-gray-200">
                <th class="p-2 text-center">{$_('alt.ref')}</th>
                <th class="p-2 text-center">{$_('alt.brand')}</th>
                <th class="p-2 text-center">{$_('alt.descr')}</th>
                <th class="p-2 text-center">{$_('alt.price')}</th>
                {#if $isAdmin}
                  <th class="p-2 text-center">{$_('alt.delete')}</th>
                {/if}
                <th class="p-2 text-center">{$_('alt.picture')}</th>
              </tr>
            </thead>
            <tbody>
              {#each $alternatives as row, index}
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr class="border-t cursor-pointer {row.obsolete ? 'bg-red-500' : ''}"
                class:bg-[lightgray]={$hoveredAlternativeIndex === index}
                ondblclick={() => selectAlternative(row, index)}
                onmouseover={() => (hoveredAlternativeIndex.set(index))}
                onmouseout={() => (hoveredAlternativeIndex.set(null))}
                >
                  <td class="text-center p-2">{row.reference}</td>
                  <td class="text-center p-2">{row.supplier}</td>
                  <td class="text-center p-2">{row.supplierDescription}</td>
                  <td class="text-center p-2">{row.price}</td>
                  {#if $isAdmin}
                    <td 
                    class="text-center p-2"
                    onclick={() => removeAlternative(row.id)}
                    >
                      <span class="{row.obsolete ? 'text-white' : 'text-red-500'}">&times;</span>
                    </td>
                  {/if}
                  <td 
                  class="text-center p-2"
                  onclick= {() => modals.open(BigPicturesModal, { instrument: row, index : index})}
                  >
                    <div class="flex justify-center items-center">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        fill="currentColor"
                        class="bi bi-camera"
                        viewBox="0 0 16 16"
                      >
                        <path
                          d="M15 12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h1.172a3 3 0 0 0 2.12-.879l.83-.828A1 1 0 0 1 6.827 3h2.344a1 1 0 0 1 .707.293l.828.828A3 3 0 0 0 12.828 5H14a1 1 0 0 1 1 1zM2 4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-1.172a2 2 0 0 1-1.414-.586l-.828-.828A2 2 0 0 0 9.172 2H6.828a2 2 0 0 0-1.414.586l-.828.828A2 2 0 0 1 3.172 4z"
                        />
                        <path
                          d="M8 11a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5m0 1a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7M3 6.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0"
                        />
                      </svg>
                    </div>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
    </div>
  </div>
  