<script>
    import { selectedCategoryIndex, alternatives, categories, selectedGroup, selectedSubGroup, hoveredAlternativeIndex} from "$lib/stores/searches";
    import { modals } from "svelte-modals";
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";

    const category = $categories[$selectedCategoryIndex]; 

    async function selectAlternative(row, index){
        const categoryId = row.categoryId;
        const instrumentId = row.id;
        window.open(`/searches?group=${encodeURIComponent($selectedGroup)}&subgroup=${encodeURIComponent($selectedSubGroup)}&category=${encodeURIComponent(categoryId)}&instrument=${encodeURIComponent(instrumentId)}`, '_blank');
        return;
    }
  </script>
  
  <div class="flex justify-center items-center p-4">
    <div class="bg-white shadow-lg rounded-lg p-6 w-full max-w-6xl border border-gray-300 flex flex-col">
        <div class="mb-6 border-b pb-4">
            <h2 class="text-lg font-bold text-black">Category :</h2>
            <div class="inline-flex space-x-0 border-2 border-black rounded-lg">
                <div class="px-4 py-2 border-r-2 border-black">{category.groupName}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.subGroupName}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.function}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.name}</div>
                <div class="px-4 py-2 border-r-2 border-black">{category.shape}</div>
                <div class="px-4 py-2">{category.lenAbrv}</div>

            </div>

            <h2 class="text-lg font-bold text-black mt-4">Alternatives :</h2>
            <table class="w-full border border-gray-200 text-sm">
            <thead>
              <tr class="bg-gray-200">
                <th class="p-2 text-left">Référence</th>
                <th class="p-2 text-left">Marque</th>
                <th class="p-2 text-left">Description</th>
                <th class="p-2 text-left">Prix</th>
                <th class="p-2 text-left">Obsolète</th>
              </tr>
            </thead>
            <tbody>
              {#each $alternatives as row, index}
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr class="border-t"
                class:bg-[lightgray]={$hoveredAlternativeIndex === index}
                ondblclick={() => selectAlternative(row, index)}
                onmouseover={() => (hoveredAlternativeIndex.set(index))}
                onmouseout={() => (hoveredAlternativeIndex.set(null))}
                >
                  <td class="p-2">{row.reference}</td>
                  <td class="p-2">{row.supplier}</td>
                  <td class="p-2">{row.supplierDescription}</td>
                  <td class="p-2">{row.price}</td>
                  <td class="p-2">{row.obsolete}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
    </div>
  </div>
  