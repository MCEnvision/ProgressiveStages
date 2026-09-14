// @vitest-environment jsdom
import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import type { FieldDiagnostic } from "../types";
import { FieldDiagnostics, ValidationMessages, fieldDiagnostics } from "./ValidationMessages";

const diagnostics: FieldDiagnostic[] = [
  { severity: "ERROR", file: "stages/chef/stage.toml", field: "luckperms.inbound[0].groups", ruleId: "chef_rank", code: "invalid_type", message: "Groups must be an array of strings." },
  { severity: "WARNING", file: "stages/chef/stage.toml", field: "luckperms.inbound", ruleId: "chef_rank", code: "missing_group", message: "The chef group is missing." },
  { severity: "ERROR", file: "stages/other/stage.toml", field: "luckperms.inbound[0].groups", ruleId: "chef_rank", code: "invalid_type", message: "Another stage needs correction." }
];
const state = vi.hoisted(() => ({ validationResult: null as { diagnostics: FieldDiagnostic[] } | null }));
vi.mock("../store/EditorContext", () => ({ useEditor: () => state }));
afterEach(() => { cleanup(); state.validationResult = null; });

it("routes indexed errors and stable row warnings without leaking another file or row", () => {
  expect(fieldDiagnostics(diagnostics, "stages/chef/stage.toml", ["luckperms.inbound[0]"], "chef_rank")).toEqual(diagnostics.slice(0, 2));
  expect(fieldDiagnostics(diagnostics, "stages/chef/stage.toml", ["luckperms.inbound[1]"], "other")).toEqual([]);
  expect(fieldDiagnostics(diagnostics, "stages/chef/stage.toml", ["luckperms"])).toEqual([]);
});

it("shows clear severity and field details beside the owning row", () => {
  state.validationResult = { diagnostics };
  render(<FieldDiagnostics file="stages/chef/stage.toml" fields={["luckperms.inbound[0]"]} ruleId="chef_rank"/>);
  expect(screen.getByText("Error")).toBeTruthy();
  expect(screen.getByText("Warning")).toBeTruthy();
  expect(screen.getByText("Groups must be an array of strings.")).toBeTruthy();
  expect(screen.queryByText("Another stage needs correction.")).toBeNull();
});

it("preserves summary errors without structured locations and renders older responses", () => {
  render(<ValidationMessages validation={{ valid: false, errors: ["Invalid package include"], warnings: ["Ignored helper"], stages: 0, validatedRevision: 9 }}/>);
  expect(screen.getByText("Invalid package include")).toBeTruthy();
  expect(screen.getByText("Ignored helper")).toBeTruthy();
  expect(screen.queryByText("Fields to review")).toBeNull();
});
