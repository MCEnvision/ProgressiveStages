// @vitest-environment jsdom
import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import type { StageCapabilities } from "../types";
import { ownershipDescription, ProviderFeedback } from "./StageCapabilityFeedback";

const editor = vi.hoisted(() => ({ validationResult: null as null | { stageCapabilities: { luckPerms: StageCapabilities["luckPerms"] } } }));
vi.mock("../store/EditorContext", () => ({ useEditor: () => editor }));
afterEach(() => { cleanup(); editor.validationResult = null; });

it("explains inherited solo ownership and explicit team fallback without changing personal or server scope", () => {
  expect(ownershipDescription("inherit", "solo", "READY")).toContain("solo ownership");
  expect(ownershipDescription("inherit", "ftb_teams", "ABSENT")).toContain("solo fallback");
  expect(ownershipDescription("team", "solo", "READY")).toContain("current FTB team");
  expect(ownershipDescription("team", "solo", "DISABLED")).toContain("disabled");
  expect(ownershipDescription("personal", "ftb_teams", "READY")).toContain("separately by each player");
  expect(ownershipDescription("server", "ftb_teams", "ABSENT")).toContain("whole server");
  expect(ownershipDescription("team", "solo")).toContain("Validate");
});

it("shows each provider state and asks for validation when the draft observation is stale", () => {
  const view = render(<ProviderFeedback/>);
  expect(screen.getByText(/Validate this draft/)).toBeTruthy();
  for (const [state, expected] of [
    ["ABSENT", "not installed"], ["DISABLED", "disabled"], ["STARTING", "starting"],
    ["READY", "ready"], ["FAILED", "provider failure"]
  ] as const) {
    editor.validationResult = { stageCapabilities: { luckPerms: state } };
    view.rerender(<ProviderFeedback/>);
    expect(screen.getByText(new RegExp(expected))).toBeTruthy();
  }
  editor.validationResult = null;
  view.rerender(<ProviderFeedback/>);
  expect(screen.queryByText(/provider failure/)).toBeNull();
  expect(screen.getByText(/Validate this draft/)).toBeTruthy();
});
