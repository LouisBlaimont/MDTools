<script>
  //Mockup scripts
  import { tools } from "../../tools.js";
  import { suppliers } from "../../suppliers.js";
  import { getOrder, addTool } from "../../order.js";


  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import { onMount } from "svelte";
  import { preventDefault } from "svelte/legacy";
  import { get } from "svelte/store";
  import { PUBLIC_API_URL } from "$env/static/public";
  import { isEditing, reload } from "$lib/stores/searches";
  import EditButton from "./EditButton.svelte";
  import EditCategoryButton from "./EditCategoryButton.svelte";
  import { toast } from "@zerodevx/svelte-toast";

  let hoveredCategoryIndex = null;
  let hoveredCategoryImageIndex = null;
  let selectedCategoryIndex = null;
  let currentSuppliers = [];
  let charValues = {};

  /**
   * Display the characteristic values of the category at line index in the table.
   * Update categories to have only the selected one.
   * @param index
   */
  async function selectCategoryWithChar(index) {
    selectCategory(index);
    selectedCategoryIndex = index;
    let cat = categories[selectedCategoryIndex];
    let catId = cat.id;
    categories = [cat];

    try{
      const response = await fetch(PUBLIC_API_URL + `/api/category/${catId}`);
      if(!response.ok){
        throw new Error("Failed to fetch characteristics of category");
      }
      const categoryChars = await response.json();
      for (let i = 0; i<categoryChars.length ; i++){
        if (categoryChars[i].name === "Length"){
          const len_val =  categoryChars[i].value.replace(/[^\d.]/g, "");
          document.getElementById(categoryChars[i].name).value = len_val;
          charValues[categoryChars[i].name] = len_val;
        }
        else{
          document.getElementById(categoryChars[i].name).value = categoryChars[i].value;
          charValues[categoryChars[i].name] = categoryChars[i].value;
        }
      }
    }catch(error){
      console.error(error)
      errorMessage = error.message;
    }
    return;

  }

  /**
   * Gets the suppliers of the category given by the line index in the table
   * @param index
   */
  async function selectCategory(index) {
    selectedCategoryIndex = index;

    // selecting the categoryId
    const cat = categories[selectedCategoryIndex]; 
    const categoryId = cat.id;  

    try{
      const response = await fetch(PUBLIC_API_URL + `/api/category/instruments/${categoryId}`);
      if (!response.ok){
        throw new Error("Failed to fetch instruments of category");
      }
      const answer = await response.json();
      currentSuppliers = Array.isArray(answer) ? answer : [answer];
    }catch (error) {
      console.error(error);
      errorMessage = error.message;
    }
    return;
  }

  let hoveredSupplierIndex = null;
  let hoveredSupplierImageIndex = null;
  let selectedSupplierIndex = null;

  function selectSupplier(index) {
    selectedSupplierIndex = index;
  }

  function showBigPicture(img) {
    const pannel = document.getElementById("big-category-pannel");
    const overlay = document.getElementById("overlay");
    const picture = document.getElementById("big-category");
    pannel.style.display = "flex";
    overlay.style.display = "block";
    picture.src = img;
  }
  function closeBigPicture() {
    const pannel = document.getElementById("big-category-pannel");
    const overlay = document.getElementById("overlay");
    pannel.style.display = "none";
    overlay.style.display = "none";
  }

  let toolToAddRef = "";
  let quantity = "";
  let order = getOrder();
  function addToOrderPannel(ref) {
    const pannel = document.getElementById("add-order-pannel");
    const overlay = document.getElementById("overlay");
    toolToAddRef = ref;
    pannel.style.display = "flex";
    overlay.style.display = "block";
  }
  function closeAddToOrder() {
    const pannel = document.getElementById("add-order-pannel");
    const overlay = document.getElementById("overlay");
    pannel.style.display = "none";
    overlay.style.display = "none";
  }
  function addToOrder() {
    const tool_ref = suppliers[selectedCategoryIndex][selectedSupplierIndex].ref;
    const tool_brand = suppliers[selectedCategoryIndex][selectedSupplierIndex].brand;
    const tool_group = tools[selectedCategoryIndex].group;
    const tool_fct = tools[selectedCategoryIndex].fct;
    const tool_name = tools[selectedCategoryIndex].name;
    const tool_form = tools[selectedCategoryIndex].form;
    const tool_dim = tools[selectedCategoryIndex].dim;
    const tool_qte = quantity;
    const tool_pu_htva = suppliers[selectedCategoryIndex][selectedSupplierIndex].price;
    order = addTool(
      tool_ref,
      tool_brand,
      tool_group,
      tool_fct,
      tool_name,
      tool_form,
      tool_dim,
      tool_qte,
      tool_pu_htva
    );
    closeAddToOrder();
  }
  function exportOrderToExcel() {
    //smth to do with the database I think
  }

  /**
   * Delete the characteristic value given by id.
   * @param id
   */
  function deleteCharacteristic(id) {
    const texte = document.getElementById(id);
    texte.value = "";
    charValues[id] = "";
    searchByCharacteristics();
  }
  function deleteAllCharacteristics() {
    for (let i = 0; i < characteristics.length; i++) {
      let texte = document.getElementById(characteristics[i]);
      texte.value = "";
      if (charValues[characteristics[i]]) {
        charValues[characteristics[i]] = "";
      }
    }
    searchByCharacteristics();
  }

  let groups_summary = [];
  let groups = [];
  export let subGroups = [];
  export let characteristics = [];
  export let categories = [];
  let selectedGroup = "";
  let selectedSubGroup = "";
  export let showSubGroups = false;
  export let showCategories = false;
  export let showChars = false;
  let errorMessage = "";

  /**
   * Fetches the group summary.
   */
  async function getGroupsSummary() {
    try {
      const response = await fetch(PUBLIC_API_URL + "/api/groups/summary");

      if (!response.ok) {
        throw new Error(`Failed to fetch groups: ${response.statusText}`);
      }

      groups_summary = await response.json();
      groups = groups_summary.map((group) => group.name);
    } catch (error) {
      console.error(error);
      errorMessage = error.message;
    }
  }

  /**
   * Gets subgroups of group and their categories
   * @param group
   */
  async function findSubGroups(group) {
    if (group == "none") {
      selectedGroup = "";
      selectedSubGroup = "";
      selectedCategoryIndex = null;
      selectedSupplierIndex = null;
      showCategories = false;
      categories=[];
      showSubGroups = false;
      subGroups=[];
      showChars = false;
      charValues=[];
      characteristics=[];
      currentSuppliers = [];
      return;
    }
    const previousGroup = selectedGroup;
    selectedGroup = group;
    showSubGroups = true;
    showCategories = true;
    
    currentSuppliers = [];
    selectedSupplierIndex="";
    selectedCategoryIndex="";
    // Only reset subgroup if the group has changed
    if (previousGroup !== group) {
      selectedSubGroup = "";
    }
    showChars = false;
    characteristics = [];
    charValues=[];

    let subGroups_all_info = [];
    try {
      const response = await fetch(PUBLIC_API_URL + `/api/subgroups/group/${group}`);
      const response_2 = await fetch(PUBLIC_API_URL + `/api/category/group/${group}`);

      if (!response.ok) {
        throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
      }
      if (!response_2.ok) {
        throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
      }

      subGroups_all_info = await response.json();
      categories = await response_2.json();
    } catch (error) {
      console.error(error);
      errorMessage = error.message;
    }
    subGroups = subGroups_all_info.map((subgroup) => subgroup.name);
    return;
  }

  /**
   * Fetch when page is rendered.
   */
  async function fetchData() {
    await getGroupsSummary();

    const group = $page.url.searchParams.get("group");
    const subgroup = $page.url.searchParams.get("subgroup");

    if (group) {
      await findSubGroups(group);
      // If a subgroup exists in the URL, load its characteristics
      if (subgroup) {
        await findCharacteristics(subgroup);
      }
    }
  }

  
  onMount(() => fetchData());

  reload.subscribe( (v) => {
    if (v) {
      fetchData();
      reload.set(false);
    }
  });

  /**
   * Gets characteristics and categories of subgroup with the name subGroup
   * @param subGroup
   */
  async function findCharacteristics(subGroup) {
    if (subGroup == "none") {
      selectedSubGroup = "";
      findSubGroups(selectedGroup);
      return;
    }
    selectedSubGroup = subGroup;
    showChars = true;

    charValues = [];
    selectedCategoryIndex = "";
    currentSuppliers = [];
    selectedSupplierIndex = "";
    let subgroup = [];

    try {
      const response = await fetch(PUBLIC_API_URL + `/api/subgroups/${subGroup}`);
      const response_2 = await fetch(PUBLIC_API_URL + `/api/category/subgroup/${subGroup}`);

      if (!response.ok) {
        throw new Error(`Failed to fetch characteristics : ${response.statusText}`);
      }
      if (!response_2.ok) {
        throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
      }

      subgroup = await response.json();
      categories = await response_2.json();
    } catch (error) {
      console.log(error);
      errorMessage = error.message;
    }

    characteristics = subgroup.subGroupCharacteristics;
    return;
  }

  /**
   * Filters the categories depending on the input of the user.
   */
  function searchByCharacteristics() {
    let char_vals = [];
    for (let i = 0; i < characteristics.length; i++) {
      if (characteristics[i] === "Function" || characteristics[i] === "Name") {
        continue;
      }
      if (characteristics[i] === "Length" && charValues[characteristics[i]]) {
        let char = {
          name: characteristics[i],
          value: charValues[characteristics[i]] + "cm",
          abrev: "",
        };
        char_vals.push(char);
      } else if (charValues[characteristics[i]]) {
        let char = {
          name: characteristics[i],
          value: charValues[characteristics[i]],
          abrev: "",
        };
        char_vals.push(char);
      } else {
        let char = {
          name: characteristics[i],
          value: "",
          abrev: "",
        };
        char_vals.push(char);
      }
    }
    const data = {
      groupName: selectedGroup,
      subGroupName: selectedSubGroup,
      function: charValues["Function"] || "",
      name: charValues["Name"] || "",
      characteristics: char_vals,
    };

    return fetch(PUBLIC_API_URL + "/api/category/search/by-characteristics", {
      method: "POST",
      headers: { "Content-type": "application/json" },
      body: JSON.stringify(data),
    })
      .then((response) => {
        if (!response.ok) {
          categories = [];
          toast.push("Aucun résultat trouvé");
          throw new Error(`Failed to search by characteristics : ${response.status}`);
        }
        return response.json();
      })
      .then((result) => {
        categories = result;
      })
      .catch((error) => {
        console.log("Error :", error);
      });
  }
</script>

<svelte:head>
  <title>Recherches</title>
</svelte:head>

<div id="container" class="text-[14px]">
  <div class="flex flex-col gap-[5px] box-border w-full">
    <!-- TOP PART -->
    <div class="flex-[5] flex flex-row mt-3 h-max-[50vh]">
      <!-- FORM OF SEARCHES -->
      <div class="flex-[1.3] h-full ml-3 p-2 bg-gray-100 rounded-lg shadow-md">
        <form class="flex flex-col w-[90%] mb-2.5">
          <label for="google-search" class="font-semibold mt-1">Recherche par mot(s) clé(s):</label>
          <input
            type="text"
            class="border border-gray-400 rounded p-0.5 border-solid border-[black]"
            id="google-search"
            name="google-search"
            placeholder="Entrez un mot clé"
          />
        </form>

        <label class="font-semibold">Recherche par caractéristiques:</label>
        <div class="flex items-center">
          <label class="w-2/5 mt-2 mb-2" for="groupOptions">Groupe:</label>
          <select
            id="groupOptions"
            bind:value={selectedGroup}
            on:change={(e) => findSubGroups(e.target.value)}
          >
            <option value="none">---</option>
            {#each groups as group}
              <option value={group}>{group}</option>
            {/each}
          </select>
        </div>

        {#if showSubGroups}
          <div class="flex items-center">
            <label class="w-2/5 mb-2" for="subGroupOptions">Sous gp:</label>
            <select
              id="subGroupOptions"
              bind:value={selectedSubGroup}
              on:change={(e) => findCharacteristics(e.target.value)}
            >
              <option value="none">---</option>
              {#each subGroups as subGroup}
                <option value={subGroup}>{subGroup}</option>
              {/each}
            </select>
          </div>
        {/if}

        {#if showChars}
          <form
            class="flex flex-col w-full gap-2.5"
            on:submit|preventDefault={searchByCharacteristics}
          >
            <div class="flex gap-2 mb-2 mt-4">
              <button
                type="submit"
                class="w-[90px] border border-gray-400 rounded bg-gray-400 border-solid border-[black] rounded-sm"
                >Chercher</button
              >
              <button
                type="button"
                class="w-[90px] border border-red-700 rounded bg-red-700 border-solid border-[black] rounded-sm"
                on:click={deleteAllCharacteristics}>Tout effacer</button
              >
            </div>
            {#each characteristics as char}
              <div class="flex items-center">
                <label for={char} class="w-2/5">{char}:</label>
                <input
                  type={ char === "Length" ? "number" : "text"}
                  class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black] mb-2"
                  id={char}
                  name={char}
                  data-testid={char}
                  bind:value={charValues[char]}
                />
                <button
                  class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer"
                  on:click={() => deleteCharacteristic(char)}>&times;</button
                >
              </div>
            {/each}
          </form>
        {/if}
      </div>

      <!-- TABLE OF CATEGORIES CORRESPONDING TO RESEARCH  -->
      <div class="flex-[3] h-full overflow-y-auto box-border ml-3">
        <table id="tools-table" data-testid="categories-table" class="w-full border-collapse">
          <thead class="bg-teal-400">
            <tr>
              {#if $isEditing}
                <th class="text-center border border-solid border-[black]"></th>
              {/if}
              <th class="text-center border border-solid border-[black]">GROUPE</th>
              <th class="text-center border border-solid border-[black]">SOUS GP</th>
              <th class="text-center border border-solid border-[black]">FCT</th>
              <th class="text-center border border-solid border-[black]">NOM</th>
              <th class="text-center border border-solid border-[black]">FORME</th>
              <th class="text-center border border-solid border-[black]">DIM</th>
            </tr>
          </thead>
          {#if showCategories}
            <tbody>
              {#each categories as row, index}
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr
                  class:bg-[cornflowerblue]={selectedCategoryIndex === index}
                  class:bg-[lightgray]={hoveredCategoryIndex === index &&
                    selectedCategoryIndex !== index}
                  on:click={() => selectCategory(index)}
                  on:dblclick={() => selectCategoryWithChar(index)}
                  on:mouseover={() => (hoveredCategoryIndex = index)}
                  on:mouseout={() => (hoveredCategoryIndex = null)}
                >
                  {#if $isEditing}
                    <EditCategoryButton category={row}/>
                  {/if}
                  <td class="text-center border border-solid border-[black]">{row.groupName}</td>
                  <td class="text-center border border-solid border-[black]">{row.subGroupName}</td>
                  <td class="text-center border border-solid border-[black]">{row.function}</td>
                  <td class="text-center border border-solid border-[black]">{row.name}</td>
                  <td class="text-center border border-solid border-[black]">{row.shape}</td>
                  <td class="text-center border border-solid border-[black]">{row.lenAbrv}</td>
                </tr>
              {/each}
            </tbody>
          {/if}
        </table>

        <!-- PASS IN ADMIN MODE -->
        <EditButton />
      </div>

      <!-- PCITURES CORRESPONDING TO THE CATEGORIES -->
      <div class="flex-1 max-h-[80vh] overflow-y-auto box-border ml-3 max-w-[150px]">
        {#each categories as row, index}
          <!-- svelte-ignore a11y_click_events_have_key_events -->
          <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
          <!-- svelte-ignore a11y_mouse_events_have_key_events -->
          <img
            alt="tool{row.id}"
            src={row.pictureId
              ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}`
              : "/default/no_picture.png"}
            on:click={() =>
              showBigPicture(
                row.pictureId
                  ? PUBLIC_API_URL`/api/pictures/${row.pictureId}`
                  : "/default/no_picture.png"
              )}
            on:mouseover={() => (hoveredCategoryImageIndex = index)}
            on:mouseout={() => (hoveredCategoryImageIndex = null)}
            class="mb-[3px] {selectedCategoryIndex === index
              ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
              : ''} {hoveredCategoryImageIndex === index && selectedCategoryIndex !== index
              ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image'
              : ''}"
          />
        {/each}
      </div>

      <div class="flex-[3] overflow-y-auto box-border m-0 ml-1">
        <!-- PCITURES OF THE SUPPLIERS -->
        <div class="border bg-teal-400 mb-[5px] border-solid border-[black]">
          <span class="p-1">Photos fournisseurs</span>
        </div>
        <div class="flex h-40 max-w-full overflow-x-auto box-border mb-[15px]">
          {#each currentSuppliers as row, index}
            <div
              class="flex shrink-0 flex-col h-[95%] text-center box-border border mr-[3px] border-solid border-[black]"
              on:click={() => showBigPicture(row.src)}
            >
              <img
                alt="supplier{row.id}"
                src={row.src}
                on:click={() => showBigPicture(row.src)}
                on:mouseover={() => (hoveredSupplierImageIndex = index)}
                on:mouseout={() => (hoveredSupplierImageIndex = null)}
                class="h-4/5 {selectedSupplierIndex === index
                  ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
                  : ''} {hoveredSupplierImageIndex === index && selectedSupplierIndex !== index
                  ? 'cursor-pointer border-2 border-solid border-[lightgray]'
                  : ''}"
              />
              <div class="box-border p-[3px] border-t-[black] border-t border-solid">{row.ref}</div>
            </div>
          {/each}
        </div>

        <!-- TABLE OF THE SUPPLIERS -->
        <div class="suppliers-table">
          <table data-testid = "suppliers-table" class="w-full border-collapse">
            <thead class="bg-teal-400">
              <tr>
                <th class="text-center border border-solid border-[black] w-16">AJOUT</th>
                <th class="text-center border border-solid border-[black] w-24">REF</th>
                <th class="text-center border border-solid border-[black] w-32">MARQUE</th>
                <th class="text-center border border-solid border-[black]">DESCRIPTION</th>
                <th class="text-center border border-solid border-[black] w-16">PRIX</th>
                <th class="text-center border border-solid border-[black] w-16">ALT</th>
                <th class="text-center border border-solid border-[black] w-16">OBS</th>
              </tr>
            </thead>
            <tbody>
              {#each currentSuppliers as row, index}
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr
                  class="cursor-pointer"
                  class:bg-[cornflowerblue]={selectedSupplierIndex === index}
                  class:bg-[lightgray]={hoveredSupplierIndex === index &&
                    selectedSupplierIndex !== index}
                  on:click={() => selectSupplier(index)}
                  on:mouseover={() => (hoveredSupplierIndex = index)}
                  on:mouseout={() => (hoveredSupplierIndex = null)}
                >
                  <td
                    class="green text-center border border-solid border-[black]"
                    on:click={() => addToOrderPannel(row.ref)}>+</td
                  >
                  <td class="text-center border border-solid border-[black]">{row.reference}</td>
                  <td class="text-center border border-solid border-[black]">{row.supplier}</td>
                  <td class="text-center border border-solid border-[black]">{row.supplierDescription}</td>
                  <td class="text-center border border-solid border-[black]">{row.price}</td>
                  <td class="text-center border border-solid border-[black]">{row.alt}</td>
                  <td class="text-center border border-solid border-[black]">{row.obsolete}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- BOTTOM PART -->
    <div class="flex-[1] flex flex-col gap-[15px] h-full pl-3 bg-gray-50">
      <div>
        <!-- SEARCH BY ORDER AND EXPORT ORDERS -->
        <div class="flex flex-row justify-between">
          <div class="w-1/2 mr-0">
            <form class="mt-2.5">
              <label for="order-search" id="order-search-label" class="w-2/5"
                >Rechercher une commande :
              </label>
              <input
                type="text"
                class="w-1/3 border border-gray-400 rounded p-0.5 border-solid border-[black]"
                list="commandes"
                id="order-search"
                name="order-search"
                placeholder="Entrez un numéro de commande"
              />
              <datalist id="commandes">
                <option value="#123456"> </option><option value="#123457"> </option><option
                  value="#123458"
                >
                </option><option value="#123459"> </option></datalist
              >
            </form>
          </div>
          <div class="mr-4">
            <button
              class="border bg-green-600 mt-[10px] p-2.5 rounded-[10px] border-solid border-[none] cursor-pointer"
              on:click={() => exportOrderToExcel()}>Exporter</button
            >
          </div>
        </div>

        <!-- TABLE OF ORDERS -->
        <div class="w-[80%] mb-[50px]">
          <table class="w-full border-collapse">
            <thead class="bg-teal-400">
              <tr>
                <th class="text-center border border-solid border-[black]">REF</th>
                <th class="text-center border border-solid border-[black]">MARQUE</th>
                <th class="text-center border border-solid border-[black]">GROUPE</th>
                <th class="text-center border border-solid border-[black]">FONCTION</th>
                <th class="text-center border border-solid border-[black]">NOM</th>
                <th class="text-center border border-solid border-[black]">FORME</th>
                <th class="text-center border border-solid border-[black]">DIMENSION</th>
                <th class="text-center border border-solid border-[black]">QTE</th>
                <th class="text-center border border-solid border-[black]">PU HTVA</th>
                <th class="text-center border border-solid border-[black]">TOTAL HTVA</th>
              </tr>
            </thead>
            <tbody>
              {#each order as row, index}
                <tr>
                  <td class="text-center border border-solid border-[black]">{row.ref}</td>
                  <td class="text-center border border-solid border-[black]">{row.brand}</td>
                  <td class="text-center border border-solid border-[black]">{row.group}</td>
                  <td class="text-center border border-solid border-[black]">{row.fct}</td>
                  <td class="text-center border border-solid border-[black]">{row.name}</td>
                  <td class="text-center border border-solid border-[black]">{row.form}</td>
                  <td class="text-center border border-solid border-[black]">{row.dim}</td>
                  <td class="text-center border border-solid border-[black]">{row.qte}</td>
                  <td class="text-center border border-solid border-[black]">{row.pu_htva}</td>
                  <td class="text-center border border-solid border-[black]">{row.total_htva}</td>
                  <td
                    ><button
                      class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 border-[none] cursor-pointer"
                      >+</button
                    ></td
                  >
                  <td
                    ><button
                      class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px border-[none] cursor-pointer"
                      >-</button
                    ></td
                  >
                  <td
                    ><button
                      class="text-gray-900 rounded text-sm bg-red-600 w-[20px] h-[20px] border-[none] cursor-pointer"
                      >&times;</button
                    ></td
                  >
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>

<div
  class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4"
  id="big-category-pannel"
>
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <span
    class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5 hover:text-[red] cursor-pointer"
    on:click={(event) => {
      event.stopPropagation();
      closeBigPicture();
    }}>&times;</span
  >
  <img class="h-[300px]" id="big-category" alt="big category" />
</div>

<div
  class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4 text-[white] flex-col gap-[15px]"
  id="add-order-pannel"
>
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <span
    class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5"
    on:click={(event) => {
      event.stopPropagation();
      closeAddToOrder();
    }}>&times;</span
  >
  <div>AJOUTER référence "{toolToAddRef}" à la commande:</div>
  <div>
    <label for="qte" class="w-2/5">QUANTITE:</label>
    <input
      type="number"
      id="qte"
      name="qte"
      class="border border-black rounded p-2 text-black bg-white"
      bind:value={quantity}
    />
    <button class="cursor-pointer" pointer on:click={() => addToOrder()}>AJOUT</button>
  </div>
</div>
