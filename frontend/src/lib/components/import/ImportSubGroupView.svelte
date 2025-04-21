<script>
    import { modals } from "svelte-modals";
    import AddGroupModal from "$lib/modals/AddGroupModal.svelte";
    import AddSubGroupModal from "$lib/modals/AddSubGroupModal.svelte";
    import { createEventDispatcher } from "svelte";

  
    export let groups = [];
    export let subGroups = {};
    export let selectedGroup = "";
    export let selectedSubGroup = "";
  
    const dispatch = createEventDispatcher();
  
    function handleGroupInput(e) {
      dispatch("groupChange", e.target.value);
    }
  
    function handleSubGroupInput(e) {
      dispatch("subGroupChange", e.target.value);
    }
  
    function filteredGroups() {
      return groups.filter(g => g.toLowerCase().includes(selectedGroup.toLowerCase()));
    }
  
    function filteredSubGroups() {
      return (subGroups[selectedGroup] || []).filter(sg =>
        sg.toLowerCase().includes(selectedSubGroup.toLowerCase())
      );
    }
  </script>
  
  <div class="flex items-center mb-4">
    <button class="text-gray-700 mr-4" onclick={() => dispatch("back")}>←</button>
    <h2 class="text-2xl font-bold">Sélectionnez le groupe et sous-groupe</h2>
  </div>
  
  <div class="mb-6">
    <label for="group-select" class="block mb-2 text-gray-700">Groupe :</label>
    <input
      id="group-select"
      type="text"
      bind:value={selectedGroup}
      class="w-full p-3 border rounded"
      placeholder="Entrez un groupe"
      oninput={handleGroupInput}
      list="group-options"
    />
    <datalist id="group-options">
      {#each filteredGroups() as group}
        <option value={group}>{group}</option>
      {/each}
    </datalist>
  </div>
  
  <div class="mb-6">
    <label for="subgroup-select" class="block mb-2 text-gray-700">Sous-groupe :</label>
    <input
      id="subgroup-select"
      type="text"
      bind:value={selectedSubGroup}
      class="w-full p-3 border rounded"
      placeholder="Entrez un sous-groupe"
      oninput={handleSubGroupInput}
      list="subgroup-options"
      disabled={selectedGroup === ""}
    />
    <datalist id="subgroup-options">
      {#each filteredSubGroups() as subGroup}
        <option value={subGroup}>{subGroup}</option>
      {/each}
    </datalist>
  
    <div class="flex justify-start gap-3 mt-3">
      <button
        class="px-3 py-2 bg-yellow-300 text-black rounded hover:bg-yellow-500 transition"
        onclick={() => modals.open(AddGroupModal)}
      >
        Ajouter un groupe
      </button>
  
      {#if selectedGroup !== ""}
        <button
          class="px-3 py-2 bg-yellow-300 text-black rounded hover:bg-yellow-500 transition"
          onclick={() => modals.open(AddSubGroupModal)}
        >
          Ajouter un sous-groupe
        </button>
      {/if}
    </div>
  </div>
  