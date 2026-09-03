import { describe, expect, it } from "vitest";
import {
  appendTomlBlock, extractArrayGroups, parseSimpleArray, readTomlValue, removeTomlSection,
  replaceArrayGroups, stringValue, upsertToml
} from "./toml";

describe("preservation oriented toml helpers", () => {
  it("updates one known value without removing comments or extension sections", () => {
    const source = `# pack note\n[stage]\nid = "pack:mage"\ndisplay_name = "Mage"\n\n[custom.extension]\nvalue = "keep me"\n`;
    const updated = upsertToml(source, "stage.display_name", "Archmage");
    expect(stringValue(readTomlValue(updated, "stage.display_name"))).toBe("Archmage");
    expect(updated).toContain("# pack note");
    expect(updated).toContain("[custom.extension]");
    expect(updated).toContain('value = "keep me"');
  });

  it("keeps nested item modifier tables grouped with their parent", () => {
    let source = "[stage]\nid = \"pack:test\"\n";
    source = appendTomlBlock(source, `[[item_modifiers]]\nid = "pack:first"\nitems = ["id:minecraft:stick"]\n\n[[item_modifiers.attributes]]\nid = "minecraft:generic.attack_damage"\namount = 1`);
    source = appendTomlBlock(source, `[[item_modifiers]]\nid = "pack:second"\nitems = ["id:minecraft:bow"]\n\n[[item_modifiers.effects]]\nid = "minecraft:speed"\namplifier = 1`);
    const groups = extractArrayGroups(source, "item_modifiers");
    expect(groups).toHaveLength(2);
    expect(groups[0].text).toContain("item_modifiers.attributes");
    const updated = replaceArrayGroups(source, "item_modifiers", [groups[1].text]);
    expect(updated).not.toContain("pack:first");
    expect(updated).toContain("pack:second");
    expect(updated).toContain("item_modifiers.effects");
  });

  it("replaces an interaction without consuming the next top level table", () => {
    const source = `[[interactions]]\ntype = "item_into_inventory"\n\n[interactions.while]\ntype = "dimension"\nid = "minecraft:the_end"\n\n[recipes]\nlocked_items = ["id:minecraft:diamond"]\n`;
    const groups = extractArrayGroups(source, "interactions");
    expect(groups).toHaveLength(1);
    expect(groups[0].text).toContain("[interactions.while]");
    expect(groups[0].text).not.toContain("[recipes]");
    const updated = replaceArrayGroups(source, "interactions", [`[[interactions]]\ntype = "item_into_inventory"\neffect = "deny"`]);
    expect(updated).toContain("[recipes]");
    expect(updated).toContain('locked_items = ["id:minecraft:diamond"]');
  });

  it("removes a guided section while preserving later sections", () => {
    const source = `[rewards]\nitems = ["minecraft:diamond:2"]\n\n[formulas]\nscore = "kills * 2"\n`;
    const updated = removeTomlSection(source, "rewards");
    expect(updated).not.toContain("minecraft:diamond");
    expect(updated).toContain("[formulas]");
  });

  it("parses selector arrays with quoted values", () => {
    expect(parseSimpleArray('["id:minecraft:stone", "tag:c:ores", "name:Iron Ingot"]')).toEqual([
      "id:minecraft:stone", "tag:c:ores", "name:Iron Ingot"
    ]);
  });
});
