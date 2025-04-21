<script>
    import { createEventDispatcher, onMount } from "svelte";
    import * as XLSX from "xlsx";
    import ImportMainView from "$lib/components/import/ImportMainView.svelte";
    import ImportSubGroupView from "$lib/components/import/ImportSubGroupView.svelte";
    import ImportSupplierView from "$lib/components/import/ImportSupplierView.svelte";
    import ImportVerificationView from "$lib/components/import/ImportVerificationView.svelte";
    import ImportLoadingView from "$lib/components/import/ImportLoadingView.svelte";
    import AddCharacteristicModal from "$lib/modals/AddCharacteristicModal.svelte";
    import AddGroupModal from "$lib/modals/AddGroupModal.svelte";
    import AddSubGroupModal from "$lib/modals/AddSubGroupModal.svelte";
    import { modals } from "svelte-modals";
    import { toast } from "@zerodevx/svelte-toast";
  
    import {
      fetchGroups,
      fetchSuppliers,
      fetchCharacteristics,
      sendExcelToBackend
    } from "../../api.js";
  
    export let file;
    const dispatch = createEventDispatcher();
  
    let currentView = "main";
    let viewHistory = [];
  
    let selectedOption = "";
    let selectedGroup = "";
    let selectedSubGroup = "";
    let selectedSupplier = "";
    let isNextEnabled = false;
    let showAddCharacteristicModal = false;
  
    let isLoading = false;
  
    let jsonData = null;
    let columnMapping = {};
    let requiredColumns = [];
  
    let groups = [];
    let subGroups = {};
    let suppliers = [];
  
    let modalPositions = {
      importModal: { x: 100, y: 100 }
    };
    let draggingModal = null;
    let dragOffset = { x: 0, y: 0 };
    let loadingMessage = "Chargement...";

  
    onMount(async () => {
      window.addEventListener("mousemove", handleModalMouseMove);
      window.addEventListener("mouseup", handleModalMouseUp);
  
      try {
        const groupData = await fetchGroups();
        groups = groupData.map(g => g.name);
        subGroups = Object.fromEntries(
          groupData.map(group => [group.name, (group.subGroups || []).map(sg => sg.name)])
        );
  
        suppliers = await fetchSuppliers();
      } catch (err) {
        console.error("Error loading initial data", err);
      }
    });
    /**
     * Handle mouse down on the modal for dragging.
     * @param {MouseEvent} event - The mouse down event.
     * @param {string} modalKey - The modal identifier key.
     */
    function handleModalMouseDown(event, modalKey = "importModal") {
      draggingModal = modalKey;
      dragOffset = {
        x: event.clientX - modalPositions[modalKey].x,
        y: event.clientY - modalPositions[modalKey].y,
      };
    }
  
    /**
     * Update modal position on mouse move.
     * @param {MouseEvent} event - The mouse move event.
     */
    function handleModalMouseMove(event) {
      if (draggingModal) {
        modalPositions[draggingModal] = {
          x: event.clientX - dragOffset.x,
          y: event.clientY - dragOffset.y,
        };
      }
    }
  
    /**
     * Reset dragging state on mouse up.
     */
    function handleModalMouseUp() {
      draggingModal = null;
    }
  
    /**
     * Change current view and save history.
     * @param {string} view - Target view to display.
     */
    function goTo(view) {
      viewHistory.push(currentView);
      currentView = view;
      isNextEnabled = false;
    }
  
    /**
     * Go back to the previous view in history.
     */
    function goBack() {
      currentView = viewHistory.pop() || "main";
    }
  
    /**
     * Handle selection of an import option.
     * @param {string} option - Selected option value.
     */
    function handleSelectGroup(option) {
      selectedOption = option;
      isNextEnabled = true;
    }
  
    /**
     * Handle change of group input value.
     * @param {string} value - New group value.
     */
    function handleGroupChange(value) {
      selectedGroup = value;
      selectedSubGroup = "";
      isNextEnabled = false;
    }
  
    /**
     * Handle change of sub-group input value.
     * @param {string} value - New sub-group value.
     */
    function handleSubGroupChange(value) {
      selectedSubGroup = value;
      isNextEnabled = selectedGroup && selectedSubGroup;
      loadCharacteristics(value);
    }
  
    /**
     * Handle supplier input selection.
     * @param {string} value - New supplier name.
     */
    function handleSupplierChange(value) {
      selectedSupplier = value;
      isNextEnabled = !!value;
      setRequiredColumns();
    }
  
    /**
     * Load characteristics from API and update required columns.
     * @param {string} subGroup - Selected sub-group name.
     */

    async function loadCharacteristics(subGroup) {
      const list = await fetchCharacteristics(subGroup);
      requiredColumns = [
        "reference", "supplier", "sold_by_md", "closed", "group_name", "supplier_description", "price", "obsolete",
        ...list.map(c => c.name),
        ...list.map(c => `abbreviation_${c.name}`),
      ];
    }
  
    /**
     * Set required columns depending on selected import option.
     */
    async function setRequiredColumns() {
      if (selectedOption === "NonCategorized") {
        requiredColumns = ["reference", "supplier", "sold_by_md", "closed", "supplier_description", "price", "obsolete"];
      } else if (selectedOption === "Catalogue") {
        requiredColumns = ["reference", "sold_by_md", "closed", "supplier_description", "price", "obsolete"];
      } else if (selectedOption === "Crossref") {
        requiredColumns = suppliers.map(s => s.name);
      } else if (selectedOption === "Alternatives") {
        requiredColumns = ["ref_1", "ref_2"];
      }
    }
  
    /**
     * Proceed to next modal view, loading necessary data when needed.
     */
    async function handleNext() {
      viewHistory.push(currentView);
  
      if (currentView === "main" && isNextEnabled) {
        if (selectedOption === "Catalogue") {
          currentView = "Supplier";
          selectedSupplier = "";
          isNextEnabled = false;
        } else if (selectedOption === "SubGroup") {
          currentView = "SubGroup";
          selectedGroup = "";
          selectedSubGroup = "";
          isNextEnabled = false;
        } else if (["Crossref", "Alternatives", "NonCategorized"].includes(selectedOption)) {
          isLoading = true;
          loadingMessage = "Chargement des données...";
          await setRequiredColumns();
          await extractExcelDataToJson();
        }
      } else if (currentView === "Supplier" && isNextEnabled) {
        isLoading = true;
        loadingMessage = "Chargement des données...";
        await extractExcelDataToJson();
      } else if (currentView === "SubGroup" && isNextEnabled) {
        isLoading = true;
        loadingMessage = "Chargement des données...";
        await loadCharacteristics(selectedSubGroup);
        await extractExcelDataToJson();
      }
    }
  
    /**
     * Normalize Excel header name for comparison.
     * @param {string} header - Raw header from Excel file.
     * @returns {string} Normalized header.
     */
    function normalizeHeader(header) {
      return header.normalize("NFD").replace(/\p{Diacritic}/gu, "").trim().toLowerCase().replace(/\s+/g, "_");
    }
  
    /**
     * Parse Excel file and map headers to required columns.
     * @returns {Promise<void>}
     */
    async function extractExcelDataToJson() {
      return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = async (e) => {
          const data = new Uint8Array(e.target.result);
          const workbook = XLSX.read(data, { type: 'array' });
          const worksheet = workbook.Sheets[workbook.SheetNames[0]];
          const raw = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
  
          jsonData = [...raw];
          columnMapping = {};
  
          if (selectedOption === "Alternatives") {
            jsonData[0] = ["ref_1", "ref_2"];
            columnMapping = { 0: "ref_1", 1: "ref_2" };
          } else {
            jsonData[0].forEach((header, index) => {
              const match = requiredColumns.find(col => normalizeHeader(col) === normalizeHeader(header));
              if (match) columnMapping[index] = match;
            });
          }
  
          isLoading = false;
          currentView = "verification";
          resolve();
        };
        reader.readAsArrayBuffer(file);
      });
    }
  
    /**
     * Submit Excel data to backend and display feedback.
     */
    async function handleImportFinal() {
        try {
            isLoading = true;
            loadingMessage = "Importation en cours...";

            const response = await sendExcelToBackend(
            jsonData,
            columnMapping,
            selectedOption,
            selectedGroup,
            selectedSubGroup,
            selectedSupplier
            );

            isLoading = false;

            if (response.success) {
            toast.push("Importation réussie !");
            dispatch("close");
            } else {
            toast.push(`Erreur d'import : ${response.message}`);
            }
        } catch (err) {
            isLoading = false;
            toast.push(`Erreur lors de l'envoi : ${err.message}`);
        }
    }
  </script>
  
  <!-- Modal Container -->
  <div class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-10 z-40">
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div
      class="bg-white rounded-lg shadow-xl w-3/4 p-6 relative"
      style="left: {modalPositions.importModal.x}px; top: {modalPositions.importModal.y}px; position: absolute;"
      on:mousedown={(e) => handleModalMouseDown(e, "importModal")}
    >
      <button class="absolute top-2 right-2 text-gray-600" on:click={() => dispatch("close")}>✖</button>
  
      {#if isLoading}
        <ImportLoadingView message={loadingMessage} />
      {:else if currentView === "main"}
        <ImportMainView {selectedOption} on:select={(e) => { selectedOption = e.detail; isNextEnabled = true; }} />
      {:else if currentView === "SubGroup"}
        <ImportSubGroupView
          {groups}
          {subGroups}
          bind:selectedGroup
          bind:selectedSubGroup
          on:groupChange={(e) => { selectedGroup = e.detail; selectedSubGroup = ""; isNextEnabled = false; }}
          on:subGroupChange={(e) => {
            selectedSubGroup = e.detail;
            isNextEnabled = selectedGroup && selectedSubGroup;
            loadCharacteristics(e.detail);
          }}
          on:back={goBack}
        />
      {:else if currentView === "Supplier"}
        <ImportSupplierView
          {suppliers}
          bind:selectedSupplier
          on:supplierChange={(e) => handleSupplierChange(e.detail)}
          on:back={goBack}
        />
      {:else if currentView === "verification"}
        <ImportVerificationView
          {jsonData}
          {requiredColumns}
          {columnMapping}
          {selectedOption}
          bind:showAddCharacteristicModal
          on:import={handleImportFinal}
          on:back={goBack}
        />
      {/if}
  
      {#if !isLoading && currentView !== "verification"}
        <div class="flex justify-end mt-6">
          <button
            class="px-4 py-2 rounded transition-all"
            class:bg-blue-500={isNextEnabled}
            class:bg-gray-300={!isNextEnabled}
            class:text-white={isNextEnabled}
            class:text-gray-600={!isNextEnabled}
            class:cursor-pointer={isNextEnabled}
            class:cursor-not-allowed={!isNextEnabled}
            on:click={handleNext}
            disabled={!isNextEnabled}
          >
            Suivant
          </button>
        </div>
      {/if}
    </div>
  
    <AddCharacteristicModal
      isOpen={showAddCharacteristicModal}
      onClose={() => showAddCharacteristicModal = false}
      {selectedSubGroup}
      on:added={async () => {
        await loadCharacteristics(selectedSubGroup);
        if (jsonData && jsonData.length > 0) {
          jsonData[0].forEach((header, index) => {
            const match = requiredColumns.find(col => normalizeHeader(col) === normalizeHeader(header));
            if (match && !columnMapping[index]) {
              columnMapping[index] = match;
            }
          });
        }
      }}
    />
  </div>
  