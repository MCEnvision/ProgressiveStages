import { describe, expect, it } from "vitest";
import {
  appendTomlBlock, extractArrayGroups, parseSimpleArray, readTomlValue, removeTomlSection,
  replaceArrayGroups, stringValue, upsertToml, removeTomlValue
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

describe("TOML source boundaries", () => {
  it("adds a sibling to a dotted assignment without redeclaring its table", () => {
    const source = "stage.id = 'chef'\n[extension]\nvalue = 'keep'\n";
    expect(upsertToml(source, "stage.team_stage", false))
      .toBe("stage.id = 'chef'\nstage.team_stage = false\n[extension]\nvalue = 'keep'\n");
    expect(upsertToml("stage.display.name = 'Chef'\n", "stage.team_stage", false))
      .toBe("stage.display.name = 'Chef'\nstage.team_stage = false\n");
    expect(upsertToml("stage = { id = 'chef' }\n", "stage.team_stage", false))
      .toBe("stage = { id = 'chef', team_stage = false }\n");
  });

  it("refuses an edit when an unterminated value makes source boundaries ambiguous", () => {
    const source = '[stage]\nnotes = """\nteam_stage = false\n';
    expect(() => upsertToml(source, "stage.team_stage", true)).toThrow(/not closed/);
    expect(() => replaceArrayGroups(source, "interactions", [])).toThrow(/not closed/);
  });
  it.each(["stage", '"stage"', "'stage'", '"sta\\u0067e"'])("reads and updates a quoted table and key in %s", header => {
    const source = `# Keep the preface.\r\n[ ${header} ] # Keep the header.\r\n  "team_stage"  = false  # Personal.\r\n\r\n\r\n[extension]\r\nvalue = 'keep'\r\n`;
    expect(readTomlValue(source, "stage.team_stage")).toBe("false");
    expect(upsertToml(source, "stage.team_stage", true)).toBe(source.replace("= false", "= true"));
    const removed = removeTomlValue(source, "stage.team_stage");
    expect(removed).not.toContain('"team_stage"');
    expect(removed).toContain("# Personal.");
    expect(removed).toContain("\r\n\r\n\r\n[extension]");
  });

  it.each(['"""', "'''" ])("ignores fake headers and assignments in a %s string", quote => {
    const source = `[stage]\nnotes = ${quote}\n[[interactions]]\n[stage]\nteam_stage = true\n${quote}\nteam_stage = false # Keep this choice.\n`;
    expect(readTomlValue(source, "stage.team_stage")).toBe("false");
    expect(extractArrayGroups(source, "interactions")).toHaveLength(0);
    expect(upsertToml(source, "stage.team_stage", true)).toBe(source.replace("= false", "= true"));
  });

  it("distinguishes quoted dotted keys from dotted paths and skips nested arrays", () => {
    const source = `["stage.extra"]\nteam_stage = true\n[stage]\nnotes = [\n  ["a", "b"], # [stage]\n  ["c"],\n]\nteam_stage = false\n`;
    expect(readTomlValue(source, "stage.team_stage")).toBe("false");
    expect(upsertToml(source, "stage.team_stage", true)).toBe(source.replace("= false", "= true"));
    expect(removeTomlSection(source, "stage")).toBe('["stage.extra"]\nteam_stage = true\n');
  });

  it("preserves every byte on a no op array rewrite including blank lines inside values", () => {
    const source = `# Preface.\r\n[[ "interactions" ]] # First.\r\ntype = 'item_on_block'\r\nnotes = """first\r\n\r\n\r\nlast"""\r\n[ 'interactions' . "while" ]\r\ntype = 'dimension'\r\nid = 'minecraft:the_end'\r\n\r\n\r\n[[interactions]]\r\ntype = 'block_right_click'\r\n\r\n[extension]\r\nvalue = 'keep'`;
    const groups = extractArrayGroups(source, "interactions");
    expect(groups).toHaveLength(2);
    expect(groups[0].text).toContain('[ \'interactions\' . "while" ]');
    expect(replaceArrayGroups(source, "interactions", groups.map(group => group.text))).toBe(source);
    const updated = replaceArrayGroups(source, "interactions", groups.map((group, index) => index ? group.text : group.text.replace("item_on_block", "item_on_entity")));
    expect(updated).toBe(source.replace("item_on_block", "item_on_entity"));
  });
});
