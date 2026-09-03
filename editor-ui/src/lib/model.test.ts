import { describe, expect, it } from "vitest";
import { dependencySummary, discoverStages, normalizeSelector, progressionModels, ruleModels, selectorMode, stageDependsOn, stageIdentity } from "./model";

const files = {
  "stages/mage/stage.toml": `[stage]\nid = "classes:mage"\ndisplay_name = "Mage"\ncategory = "Classes"\n`,
  "stages/mage/rules.toml": `[[rules]]\nid = "classes:mage/rule"\ncategory = "abilities"\naction = "use"\neffect = "lock"\nselector = "id:swim"\npriority = 20\n`,
  "stages/mage/progression.toml": `[[grants]]\nid = "classes:mage/grant"\ncondition = { type = "advancement", id = "minecraft:story/enchant_item" }\n`,
  "stages/wizard/stage.toml": `[stage]\nid = "classes:wizard"\ndisplay_name = "Wizard"\ndependencies = ["classes:mage"]\ndependency_mode = "all"\n`,
  "stages/wizard/rules.toml": "",
  "stages/wizard/progression.toml": ""
};

describe("react editor stage models", () => {
  it("discovers three file packages and their counts", () => {
    const stages = discoverStages(files);
    expect(stages).toHaveLength(2);
    expect(stages[0].name).toBe("Mage");
    expect(stages[0].ruleCount).toBe(1);
    expect(stages[0].grantCount).toBe(1);
    expect(stages[1].dependencies).toEqual(["classes:mage"]);
  });

  it("understands branches and prevents cycles", () => {
    const stages = discoverStages(files);
    const wizard = stages.find(stage => stage.id === "classes:wizard")!;
    expect(dependencySummary(stages, wizard)).toBe("Requires Mage");
    expect(stageDependsOn(stages, "classes:wizard", "classes:mage")).toBe(true);
    expect(stageDependsOn(stages, "classes:mage", "classes:wizard")).toBe(false);
  });

  it("parses rules and progression entries for guided cards", () => {
    expect(ruleModels(files["stages/mage/rules.toml"])[0]).toMatchObject({ category: "abilities", effect: "lock", priority: 20 });
    expect(progressionModels(files["stages/mage/progression.toml"])[0]).toMatchObject({ kind: "grants", conditionType: "advancement", conditionTarget: "minecraft:story/enchant_item" });
  });

  it("keeps recipe outputs and exact recipe identifiers as separate canonical rules", () => {
    const rules = ruleModels(`[recipes]\nlocked_items = ["id:minecraft:diamond_sword|priority=120"]\nlocked_ids = ["minecraft:diamond_sword|priority=240"]\n`);
    expect(rules).toContainEqual(expect.objectContaining({ table: "recipe_items", recipeKind: "output", selector: "id:minecraft:diamond_sword", priority: 120 }));
    expect(rules).toContainEqual(expect.objectContaining({ table: "recipe_ids", recipeKind: "identifier", selector: "minecraft:diamond_sword", priority: 240 }));
  });

  it("marks the legacy generic recipe field as ambiguous instead of treating it as an output item", () => {
    const [rule] = ruleModels(`[recipes]\nlocked = ["minecraft:diamond_sword"]\n`);
    expect(rule).toMatchObject({ table: "classic", category: "recipes", ambiguous: true });
    expect(rule.recipeKind).toBeUndefined();
  });

  it("keeps inventory insertion source and destination selectors paired through nested conditions", () => {
    const rules = ruleModels(`[[interactions]]\nid = "example:ore_bin_window"\ntype = "item_into_inventory"\nheld_item = "tag:c:ores"\ntarget_kind = "block"\ntarget = "id:example:selling_bin"\neffect = "deny"\npriority = 250\nlifetime = "duration"\nduration = "30s"\n\n[interactions.while]\ntype = "dimension"\nid = "minecraft:the_end"\n\n[interactions.reset_condition]\ntype = "boolean"\nexpected = false\n`);
    expect(rules).toContainEqual(expect.objectContaining({
      table: "interactions",
      action: "item_into_inventory",
      selector: "tag:c:ores",
      targetKind: "block",
      destination: "id:example:selling_bin",
      effect: "deny",
      priority: 250,
      id: "example:ore_bin_window",
      lifetime: "duration",
      duration: "30s",
      conditionType: "dimension",
      conditionTarget: "minecraft:the_end",
      resetConditionType: "boolean"
    }));
    expect(rules[0].resetConditionSource).toContain("expected = false");
  });

  it("creates interchangeable namespaces without a forced pack prefix", () => {
    expect(stageIdentity("Warlock", "wizard")).toMatchObject({ id: "wizard:warlock", path: "warlock" });
  });

  it("uses all star for the entire selected registry category", () => {
    expect(normalizeSelector("all", "ignored")).toBe("all:*");
    expect(selectorMode("all:*")).toBe("all");
  });
});
