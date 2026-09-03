import { describe, expect, it } from "vitest";
import { ruleEffects } from "./data";

describe("rule effect choices", () => {
  it("only exposes exclusion for paired inventory insertion rules", () => {
    expect(ruleEffects("items", "use").map(effect => effect.value)).not.toContain("exclude");
    expect(ruleEffects("interactions", "item_into_inventory").map(effect => effect.value)).toContain("exclude");
  });
});
