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
