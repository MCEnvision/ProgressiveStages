import { describe, expect, it } from "vitest";
import { serializeInventoryCondition, serializeInventoryInsertionRule } from "./inventoryInsertion";

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

  it("preserves supported nested condition values when another field is edited", () => {
    expect(serializeInventoryCondition('{ type = "boolean", expected = false }', "boolean", "", 1))
      .toBe('{ type = "boolean", expected = false }');
  });

  it("updates a quoted condition target without replacing its alias or nested fields", () => {
    const source = '{ "type" = "script", "callback" = "first", extension = { type = "dimension", value = "keep" } }';
    expect(serializeInventoryCondition(source, "script", "second", 1))
      .toBe(source.replace('"first"', '"second"'));
  });

  it("does not mistake a nested type for the condition type", () => {
    const source = '{ extension = { type = "dimension", id = "nested" }, type = "boolean", expected = false }';
    expect(serializeInventoryCondition(source, "boolean", "", 1)).toBe(source);
  });
});
