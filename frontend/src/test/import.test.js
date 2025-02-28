import { render, fireEvent } from "@testing-library/svelte";
import ImportPage from "../routes/admin/import/+page.svelte"; // SvelteKit alias
import { sendExcelToBackend, fetchGroups, fetchCharacteristics } from "../api.js";
import { describe, it, expect, vi, beforeEach } from "vitest";

// Mock `fetch` globally
global.fetch = vi.fn(() =>
  Promise.resolve({
    ok: true,
    json: () => Promise.resolve({ message: "Import successful" }),
  })
);

describe("Import functionality", () => {
  let sampleJsonData;
  let sampleColumnMapping;

  /**
   * Resets mocks and initializes sample data before each test.
   */
  beforeEach(() => {
    vi.clearAllMocks(); // Reset mocks before each test

    // Sample JSON data extracted from an Excel file
    sampleJsonData = [
      ["reference", "supplier_name", "price"],
      ["REF123", "Supplier A", "100"],
      ["REF456", "Supplier B", "200"],
    ];

    // Column mapping
    sampleColumnMapping = {
      0: "reference",
      1: "supplier_name",
      2: "price",
    };
  });

  /**
   * Tests sending formatted data to the backend.
   */
  it("should send formatted data to the backend", async () => {
    const selectedOption = "SubGroup";
    const groupName = "Test Group";
    const subGroupName = "Test Subgroup";

    await sendExcelToBackend(sampleJsonData, sampleColumnMapping, selectedOption, groupName, subGroupName);

    expect(fetch).toHaveBeenCalledTimes(1);
    expect(fetch).toHaveBeenCalledWith("http://localhost:8080/api/import/excel", expect.objectContaining({
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: expect.any(String),
    }));

    // Verify payload structure
    const requestBody = JSON.parse(fetch.mock.calls[0][1].body);
    expect(requestBody).toEqual({
        importType: selectedOption,
        groupName: groupName,
        subGroupName: subGroupName,
        supplier: "", // Ensure this is passed correctly, update if needed
        data: [
          { reference: "REF123", supplier_name: "Supplier A", price: "100" },
          { reference: "REF456", supplier_name: "Supplier B", price: "200" },
        ],
      });
  });

  /**
   * Tests that no data is sent when jsonData is empty.
   */
  it("should not send data if jsonData is empty", async () => {
    sampleJsonData = [];

    await expect(sendExcelToBackend(sampleJsonData, sampleColumnMapping, "SubGroup", "", ""))
      .rejects.toThrow("No data to send.");

    expect(fetch).not.toHaveBeenCalled();
  });

  /**
   * Tests that no data is sent if no import type is selected.
   */
  it("should not send data if no import type is selected", async () => {
    await expect(sendExcelToBackend(sampleJsonData, sampleColumnMapping, "", "", ""))
      .rejects.toThrow("No file type selected.");

    expect(fetch).not.toHaveBeenCalled();
  });

  /**
   * Tests handling of fetch failure.
   */
  it("should handle fetch failure gracefully", async () => {
    fetch.mockImplementationOnce(() => Promise.resolve({ ok: false }));

    await expect(sendExcelToBackend(sampleJsonData, sampleColumnMapping, "SubGroup", "", ""))
      .rejects.toThrow("Failed to import data");

    expect(fetch).toHaveBeenCalledTimes(1);
  });

  /**
   * Tests that fetching groups from the backend works correctly.
   */
  it("should fetch groups correctly", async () => {
    fetch.mockImplementationOnce(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve([
          { name: "Group1", subGroups: [{ name: "SubGroup1" }, { name: "SubGroup2" }] },
          { name: "Group2", subGroups: [{ name: "SubGroup3" }] },
        ]),
      })
    );

    const groups = await fetchGroups();

    expect(groups).toEqual([
      { name: "Group1", subGroups: [{ name: "SubGroup1" }, { name: "SubGroup2" }] },
      { name: "Group2", subGroups: [{ name: "SubGroup3" }] },
    ]);

    expect(fetch).toHaveBeenCalledWith("http://localhost:8080/api/groups", expect.objectContaining({
      method: "GET",
      headers: { "Accept": "application/json" },
    }));
  });

  /**
   * Tests that fetching characteristics for a subgroup works correctly.
   */
  it("should fetch characteristics correctly", async () => {
    fetch.mockImplementationOnce(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          id: 1,
          name: "Plastic Scalpels",
          subGroupCharacteristics: ["Length", "Material", "Sharpness", "Function", "Name"],
        }),
      })
    );

    const characteristics = await fetchCharacteristics("Plastic Scalpels");

    expect(characteristics).toEqual(["Length", "Material", "Sharpness", "Function", "Name"]);

    expect(fetch).toHaveBeenCalledWith("http://localhost:8080/api/subgroups/Plastic%20Scalpels", expect.objectContaining({
      method: "GET",
      headers: { "Accept": "application/json" },
    }));
  });
});
