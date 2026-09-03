import { describe, expect, it } from "vitest";
import { serializeInventoryInsertionRule } from "./inventoryInsertion";

describe("inventory insertion rule serializer", () => {
  it("writes the canonical paired interaction fields", () => {
    expect(serializeInventoryInsertionRule({
      selector: "tag:c:ores",
      targetKind: "block",
      destination: "id:example:selling_bin",
      effect: "deny",
      priority: 250
    })).toBe(`[[interactions]]
type = "item_into_inventory"
held_item = "tag:c:ores"
target_kind = "block"
target = "id:example:selling_bin"
effect = "deny"
priority = 250`);
  });
});
