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

  it("writes activation and reset controls for temporary inventory rules", () => {
    expect(serializeInventoryInsertionRule({
      id: "example:ore_bin_window",
      selector: "id:minecraft:diamond",
      targetKind: "block",
      destination: "id:minecraft:chest",
      effect: "lock",
      priority: 100,
      lifetime: "duration",
      duration: "30s",
      condition: '{ type = "dimension", id = "minecraft:the_end" }',
      resetCondition: '{ type = "boolean", expected = false }'
    })).toBe(`[[interactions]]
id = "example:ore_bin_window"
type = "item_into_inventory"
held_item = "id:minecraft:diamond"
target_kind = "block"
target = "id:minecraft:chest"
effect = "lock"
priority = 100
lifetime = "duration"
duration = "30s"
while = { type = "dimension", id = "minecraft:the_end" }
reset_condition = { type = "boolean", expected = false }`);
  });
});
