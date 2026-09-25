import { describe, expect, it } from "vitest";
import { GUIDE_BYTE_LIMIT, GUIDE_CODE_POINT_LIMIT, expandGuideTemplate, guideTextMetrics, truncateGuideText } from "./guide";

describe("guide templates", () => {
  it("expands fixed tokens and escaped braces", () => {
    expect(expandGuideTemplate("{{stage_name}} {stage_name} {unknown}", { stage_name: "Miner" })).toEqual({
      text: "{stage_name} Miner {unknown}",
      unknown: ["unknown"]
    });
  });

  it("measures unicode by code point and utf 8 byte length", () => {
    const metrics = guideTextMetrics("🪨".repeat(GUIDE_CODE_POINT_LIMIT));
    expect(metrics.codePoints).toBe(GUIDE_CODE_POINT_LIMIT);
    expect(metrics.bytes).toBe(GUIDE_BYTE_LIMIT);
    expect(metrics.valid).toBe(true);
    expect(guideTextMetrics("🪨".repeat(GUIDE_CODE_POINT_LIMIT + 1)).valid).toBe(false);
  });

  it("truncates without splitting unicode or exceeding either limit", () => {
    const value = truncateGuideText("🪨".repeat(GUIDE_CODE_POINT_LIMIT));
    const metrics = guideTextMetrics(value);
    expect(metrics.valid).toBe(true);
    expect([...value].every(character => character === "🪨")).toBe(true);
  });
});
