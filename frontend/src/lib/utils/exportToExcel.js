import * as XLSX from "xlsx";
import {
  fetchAlternatives,
  fetchTools,
  fetchInstrumentsBySubGroup,
  fetchSupplierByName,
  fetchInstrumentsBySupplier,
  fetchCharacteristicValuesByCategory
} from "../../api.js";
import { toast } from "@zerodevx/svelte-toast";


/**
 * Handles Excel export based on the selected mode.
 *
 * @param {Object} params
 * @param {"SubGroup" | "Catalogue" | "Alternatives" | "Crossref" | "Full"} params.mode
 * @param {string|null} params.contextName
 * @param {string[]} params.selectedColumns
 * @param {string[]} params.selectedCharacteristics
 */
export async function handleExport({
  mode,
  contextName,
  selectedColumns,
  selectedCharacteristics,
  exportAllSuppliers = true,
  selectedCrossrefSuppliers = []
}) {
  try {
    // --------- Crossref Export ----------
    if (mode === "Crossref") {
      const instruments = await fetchTools();
    
      const suppliersSet = new Set();
      const categoriesMap = new Map();

      // Group instruments by category and supplier
      for (const instrument of instruments) {
        const { categoryId, supplier, reference } = instrument;
        if (!categoryId || !supplier) continue;

        suppliersSet.add(supplier);

        if (!categoriesMap.has(categoryId)) {
          categoriesMap.set(categoryId, new Map());
        }

        const supplierMap = categoriesMap.get(categoryId);
        if (!supplierMap.has(supplier)) supplierMap.set(supplier, []);
        supplierMap.get(supplier).push(reference);
      }

      // Only keep categories with at least 2 suppliers
      const rows = [];
      for (const [categoryId, supplierMap] of categoriesMap.entries()) {
        const relevantSuppliers = exportAllSuppliers
          ? [...supplierMap.keys()]
          : [...supplierMap.keys()].filter(s => selectedCrossrefSuppliers.includes(s));
    
        if (relevantSuppliers.length < 2) continue;
    
        const row = { Category: categoryId };
        for (const supplier of (exportAllSuppliers ? suppliersSet : selectedCrossrefSuppliers)) {
          row[supplier] = supplierMap.get(supplier)?.join(", ") || "";
        }
        rows.push(row);
      }
    
      if (rows.length === 0) {
        toast.push($_('admin.export.no_crossref_supplier'), {
          theme: {
            '--toastBackground': 'red',
            '--toastColor': 'white'
          }
        });
        return;
      }

      const ws = XLSX.utils.json_to_sheet(rows);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, "Crossref");
      XLSX.writeFile(wb, "export_crossref.xlsx");
      return;
    }    

    // --------- Alternatives Export ----------
    if (mode === "Alternatives") {
      const alternatives = await fetchAlternatives();
      if (!alternatives?.length) {
        alert("No alternatives found.");
        return;
      }

      const data = alternatives.map(({ reference_1, reference_2 }) => ({
        Reference_1: reference_1,
        Reference_2: reference_2
      }));

      const ws = XLSX.utils.json_to_sheet(data);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, "Alternatives");
      XLSX.writeFile(wb, "export_alternatives.xlsx");
      return;
    }

    const isCatalogue = mode === "Catalogue";
    const isFull = mode === "Full";

    // --------- Check mandatory selections ----------
    if ((isCatalogue || mode === "SubGroup") && !contextName) {
      alert(`Please select a ${isCatalogue ? "supplier" : "subgroup"} before exporting.`);
      return;
    }

    if (!selectedColumns?.length && !isFull) {
      alert("Please select at least one column to export.");
      return;
    }

    // --------- Fetch instruments based on mode ----------
    const instruments = isCatalogue
      ? await fetchInstrumentsBySupplier(contextName)
      : isFull
        ? await fetchTools()
        : await fetchInstrumentsBySubGroup(contextName);

    if (!instruments?.length) {
      alert("No data available to export.");
      return;
    }

    // --------- Format rows ----------
    const rows = [];

    for (const instrument of instruments) {
      if (isCatalogue && !selectedColumns.includes("obsolete") && instrument.obsolete) continue;

      let row = {};

      // Copy selected columns
      selectedColumns.forEach((col) => {
        if (instrument.hasOwnProperty(col)) {
          row[col] = instrument[col];
        }
      });

      // Enrich with supplier metadata if needed
      if (selectedColumns.includes("closed") || selectedColumns.includes("sold_by_md")) {
        const supplier = await fetchSupplierByName(instrument.supplier);
        if (selectedColumns.includes("closed")) row.closed = supplier.closed;
        if (selectedColumns.includes("sold_by_md")) row.sold_by_md = supplier.soldByMd;
      }

      // Add characteristic values if applicable
      if (!isCatalogue && selectedCharacteristics?.length && instrument.categoryId) {
        const values = await fetchCharacteristicValuesByCategory(instrument.categoryId);
      
        const valueMap = Object.fromEntries(
          values.map(v => [v.name, v.value])
        );
      
        for (const char of selectedCharacteristics) {
          row[char] = valueMap?.[char] ?? "";
        }
      }      

      rows.push(row);
    }

    // --------- Export to XLSX ----------
    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Instruments");
    XLSX.writeFile(wb, `export_${contextName || "all_instruments"}.xlsx`);
  } catch (err) {
    console.error("Export error:", err);
    alert("An error occurred while exporting.");
  }
}
