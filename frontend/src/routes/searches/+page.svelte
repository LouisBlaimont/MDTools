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
  import { checkRole } from "$lib/rbacUtils";
	import { ROLES } from "../../constants";
	import { user } from "$lib/stores/user_stores"; 
  import CategoryComponent from "$lib/components/category_component.svelte";
  import InstrumentComponent from "$lib/components/instrument_component.svelte";
  import OrderComponent from "$lib/components/order_component.svelte";
  //import SearchComponent from "$lib/components/search_component.svelte";


  // RBAC 
  let userValue;
  user.subscribe(value => {
    userValue = value;
  });
  // returns true if user is admin
  let isAdmin = checkRole(userValue, ROLES.ADMIN);

  // writable
  let selectedCategoryIndex = null;
  let currentSuppliers = [];
  let charValues = {};
  let groups = [];
  export let subGroups = [];
  export let characteristics = [];
  export let categories = [];
  let hoveredSupplierIndex = null;
  let hoveredSupplierImageIndex = null;
  let selectedSupplierIndex = null;
  let groups_summary = [];  
  let hoveredCategoryIndex = null;
  let hoveredCategoryImageIndex = null;  
  let selectedGroup = "";
  let selectedSubGroup = "";
  export let showSubGroups = false;
  export let showCategories = false;
  export let showChars = false;
  let errorMessage = "";
  let toolToAddRef = "";
  let quantity = "";
  let order = getOrder();


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

  function openEditPage(toolId) {
      goto(`/admin/instrument_edit/${toolId}`);
  }

  function openAddInstrumentPage() {
      goto('/admin/add_instrument');
  }  

  let resizing = null;
  let startX, startY, startWidth, startHeight;
  let div1, div2, div3, div4, div5;
  
  function startResize(event, div) {
      resizing = div;
      startX = event.clientX;
      startY = event.clientY;
      startWidth = div.offsetWidth;
      startHeight = div.offsetHeight;
      document.addEventListener('mousemove', resize);
      document.addEventListener('mouseup', stopResize);
  }
  
  function resize(event) {
      if (!resizing) return;
      const deltaX = event.clientX - startX;
      const deltaY = event.clientY - startY;
      resizing.style.width = `${startWidth + deltaX}px`;
      resizing.style.height = `${startHeight + deltaY}px`;
  }
  
  function stopResize() {
      resizing = null;
      document.removeEventListener('mousemove', resize);
      document.removeEventListener('mouseup', stopResize);
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
      <div class="flex-[1.3] h-full ml-3 p-2 bg-gray-100 rounded-lg shadow-md resizable" bind:this={div1}>
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
        <div class="resize-handle" on:mousedown={(e) => startResize(e, div1)}></div>
      </div>

    <CategoryComponent />
    
    <InstrumentComponent />

</div>

    <!-- BOTTOM PART -->
    <OrderComponent />
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

<style>
  .resizable {
      position: relative;
      resize: both;
      overflow: auto;
  }
  .resize-handle {
      position: absolute;
      width: 10px;
      height: 10px;
      background: gray;
      bottom: 0;
      right: 0;
      cursor: nwse-resize;
  }
</style>