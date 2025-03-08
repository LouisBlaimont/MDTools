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
  import { isEditing, reload, isAdmin, groups_summary, groups, 
    errorMessage, findSubGroupsStore, findCharacteristicsStore } from "$lib/stores/searches";
  import EditButton from "./EditButton.svelte";
  import EditCategoryButton from "./EditCategoryButton.svelte";
  import EditInstrumentButton from "./EditInstrumentButton.svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { checkRole } from "$lib/rbacUtils";
  import { ROLES } from "../../constants";
  import { user } from "$lib/stores/user_stores";
  import CategoryComponent from "$lib/components/category_component.svelte";
  import InstrumentComponent from "$lib/components/instrument_component.svelte";
  import OrderComponent from "$lib/components/order_component.svelte";
  import SearchComponent from "$lib/components/search_component.svelte";

  import { modals } from "svelte-modals";
  import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";

  // RBAC
  let userValue;
  user.subscribe((value) => {
    userValue = value;
  });
  // returns true if user is admin
  isAdmin.set(checkRole(userValue, ROLES.ADMIN));

  /**
   * Fetches the group summary.
   */
  async function getGroupsSummary() {
    try {
      const response = await fetch(PUBLIC_API_URL + "/api/groups/summary");

      if (!response.ok) {
        throw new Error(`Failed to fetch groups: ${response.statusText}`);
      }

      groups_summary.set(await response.json());
      groups.set($groups_summary.map(($group) => $group.name));
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }

  let findSubGroups = null;
  let findCharacteristics = null;
  let initialized = false;
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

  function tryFetchData() {
    if (findSubGroups && findCharacteristics && !initialized) {
      initialized = true; 
      fetchData();
    }
  }

  onMount(() => {
    findSubGroupsStore.subscribe(value => {
      if (value) {
        findSubGroups = value;
        tryFetchData(); 
      }
    });
    findCharacteristicsStore.subscribe(value => {
      if (value) {
        findCharacteristics = value;
        tryFetchData();
      }
    });
  });

  reload.subscribe((v) => {
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

</script>

<svelte:head>
  <title>Recherches</title>
</svelte:head>

<div id="container" class="text-[14px]">
  <div class="flex flex-col gap-[5px] box-border w-full">
    <!-- TOP PART -->
    <div class="flex-[5] flex flex-row mt-3 h-max-[50vh]">
      <!-- FORM OF SEARCHES -->
      <SearchComponent />

      <!-- TABLE AND PICTURES OF CATEGORIES CORRESPONDING TO RESEARCH  -->
      <CategoryComponent />
      
      <!-- TABLE AND PICTURES OF THE SUPPLIERS -->
      <InstrumentComponent />

    </div>

    <!-- BOTTOM PART -->
    <!-- ORDERS  -->
    <OrderComponent />

  </div>
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>

