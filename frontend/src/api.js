const BASE_URL = "http://localhost:8080/api"; // Ensure this matches your backend URL

export async function fetchTools() {
  const res = await fetch(`${BASE_URL}/instruments`);
  if (!res.ok) throw new Error("Failed to fetch tools");
  return res.json();
}

export async function fetchToolById(id) {
  const res = await fetch(`${BASE_URL}/instruments/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch tool ${id}`);
  return res.json();
}

export async function deleteTool(id) {
  const res = await fetch(`${BASE_URL}/instruments/${id}`, { method: "DELETE" });
  if (!res.ok) throw new Error("Failed to delete tool");
}

export async function editTool(id, tool) {
  const res = await fetch(`${BASE_URL}/instruments/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(tool),
  });
  if (!res.ok) throw new Error("Failed to edit tool");
  return res.json();
}

export async function addTool(tool) {
  const res = await fetch(`${BASE_URL}/instruments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(tool),
  });
  if (!res.ok) throw new Error("Failed to add tool");
  return res.json();
}

/**
 * Fetches groups and their associated sub-groups dynamically from the backend.
 */
export async function fetchGroups() {
  const res = await fetch(`${BASE_URL}/groups`, {
    method: "GET",
    headers: { "Accept": "application/json" },
  });

  if (!res.ok) throw new Error(`Failed to fetch groups: ${res.status}`);

  return res.json();
}


/**
 * Fetches characteristics related to the selected subgroup from the backend.
 * @param {string} subGroup - The selected subgroup name.
 * @returns {Promise<Array>} - A list of characteristics.
 */
export async function fetchCharacteristics(subGroup) {
  if (!subGroup || subGroup.trim() === "") {
    console.warn("No subgroup selected.");
    return [];
  }

  const res = await fetch(`${BASE_URL}/subgroups/${encodeURIComponent(subGroup)}`, {
    method: "GET",
    headers: { "Accept": "application/json" },
  });

  if (!res.ok) throw new Error("Failed to fetch characteristics");

  const data = await res.json();
  
  return data.subGroupCharacteristics;
}

/**
 * Sends the formatted JSON data to the backend for processing.
 * @param {Array} jsonData - The raw JSON data.
 * @param {Object} columnMapping - The mapping of column indices to names.
 * @param {string} selectedOption - The selected import type.
 * @param {string} selectedGroup - The selected group name (if applicable).
 * @param {string} selectedSubGroup - The selected sub-group name (if applicable).
 */
export async function sendExcelToBackend(jsonData, columnMapping, selectedOption, selectedGroup, selectedSubGroup) {
  if (!jsonData || jsonData.length === 0) {
    throw new Error("No data to send.");
  }

  if (!selectedOption || selectedOption.trim() === "") {
    throw new Error("No file type selected.");
  }

  const selectedHeaders = Object.entries(columnMapping)
    .filter(([index, name]) => name && name.trim() !== "")
    .map(([index, name]) => ({ index: parseInt(index), name }));

  console.log("✅ Final columns to send:", selectedHeaders);

  const formattedData = jsonData.slice(1).map(row => {
    let instrument = {};
    selectedHeaders.forEach(({ index, name }) => {
      if (row[index] !== undefined && row[index] !== null && row[index].toString().trim() !== "") {
        instrument[name] = row[index];
      }
    });
    return instrument;
  });

  console.log("📤 Formatted data for submission:", formattedData);

  const groupName = selectedOption === "SubGroup" ? selectedGroup : "";
  const subGroupName = selectedOption === "SubGroup" ? selectedSubGroup : "";

  const requestData = {
    importType: selectedOption,
    groupName: groupName,
    subGroupName: subGroupName,
    data: formattedData,
  };

  const res = await fetch(`${BASE_URL}/import/excel`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(requestData),
  });

  if (!res.ok) {
    throw new Error("Failed to import data.");
  }

  return "Data successfully imported!";
}
