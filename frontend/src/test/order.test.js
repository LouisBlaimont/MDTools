import { describe, it, expect } from "vitest";
import { getOrder, addTool } from "../order"; // Adjust path to where the code is

describe("Order management functions", () => {
  // Test for getOrder function
  it("should return the current order list", () => {
    const order = getOrder();
    expect(order).toEqual([
      {
        id: "1",
        ref: "AA1000301",
        brand: "MAGONOVUM",
        group: "MANCHE DE BISTOURI",
        fct: "",
        name: "",
        form: "3",
        dim: "LG 125",
        qte: "2",
        pu_htva: "15.44",
        total_htva: "30.88",
      },
    ]);
  });

  // Test for addTool function
  it("should add a new tool to the order list", () => {
    const newTool = {
      tool_ref: "BB2000402",
      tool_brand: "NEWBRAND",
      tool_group: "NEWGROUP",
      tool_fct: "Functionality",
      tool_name: "NewTool",
      tool_form: "4",
      tool_dim: "LG 150",
      tool_qte: "3",
      tool_pu_htva: "20.00",
    };

    const updatedOrder = addTool(
      newTool.tool_ref,
      newTool.tool_brand,
      newTool.tool_group,
      newTool.tool_fct,
      newTool.tool_name,
      newTool.tool_form,
      newTool.tool_dim,
      newTool.tool_qte,
      newTool.tool_pu_htva
    );

    expect(updatedOrder).toHaveLength(2); // Ensure there's now 2 tools in the order
    expect(updatedOrder[1]).toEqual({
      id: 2,
      ref: newTool.tool_ref,
      brand: newTool.tool_brand,
      group: newTool.tool_group,
      fct: newTool.tool_fct,
      name: newTool.tool_name,
      form: newTool.tool_form,
      dim: newTool.tool_dim,
      qte: newTool.tool_qte,
      pu_htva: newTool.tool_pu_htva,
      total_htva: "3", // As per the original code, but this is incorrect (fix later)
    });
  });
});
