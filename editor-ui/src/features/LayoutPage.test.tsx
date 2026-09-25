// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import { LayoutPage } from "./LayoutPage";

const editor = vi.hoisted(() => ({
  boot: { draft: { files: {
    "stages/visible/stage.toml": "[guide]\nhow_to_unlock = \"Find the camp.\"\n[display]\nreveal = \"always\"\n",
    "stages/hidden/stage.toml": "[display]\nreveal = \"always\"\n"
  } } },
  stages: [] as Array<Record<string, unknown>>,
  mutateFile: vi.fn(async (..._args: unknown[]) => {}),
  mutateFiles: vi.fn(async (..._args: unknown[]) => {}),
  notify: vi.fn(),
  selectStage: vi.fn(),
  setPage: vi.fn(),
  openDialog: vi.fn()
}));

vi.mock("../store/EditorContext", () => ({ useEditor: () => editor }));
vi.mock("./stages/StageDialogs", () => ({
  ConfirmStageAction: () => null,
  IdentityForm: () => null
}));

afterEach(cleanup);

const stage = (id: string, name: string, hidden = false) => ({
  key: id,
  folder: id,
  stagePath: `stages/${id.split(":")[1]}/stage.toml`,
  rulesPath: "",
  progressionPath: "",
  legacy: false,
  archived: false,
  id,
  name,
  description: `${name} description`,
  icon: "minecraft:stone",
  category: hidden ? "secret" : "base",
  color: "gold",
  hidden,
  ruleCount: 0,
  grantCount: 0,
  revokeCount: 0,
  dependencies: [],
  dependencyMode: "all",
  dependencyCount: 0
});

const dependentStage = (id: string, name: string, dependency: string) => ({
  ...stage(id, name),
  dependencies: [dependency],
  dependencyCount: 1
});

it("keeps player preview read only and hides hidden stages", () => {
  vi.clearAllMocks();
  editor.stages = [stage("pack:visible", "Visible"), stage("pack:hidden", "Hidden", true)];
  render(<LayoutPage />);

  expect(screen.getByRole("button", { name: /visible, base, draft/i })).toBeTruthy();
  expect(screen.getByRole("button", { name: /hidden, secret, draft/i })).toBeTruthy();

  fireEvent.click(screen.getByRole("button", { name: "Player preview" }));
  expect(screen.getByText("Draft player preview.")).toBeTruthy();
  expect(screen.getByRole("button", { name: /visible, base, ready/i })).toBeTruthy();
  expect(screen.queryByRole("button", { name: /hidden, base/i })).toBeNull();
  expect(screen.queryByRole("option", { name: "secret" })).toBeNull();

  fireEvent.click(screen.getByRole("button", { name: /visible, base, ready/i }));
  expect(screen.getByRole("complementary", { name: "Player preview inspector" })).toBeTruthy();
  fireEvent.click(screen.getByRole("button", { name: "How to unlock Visible" }));
  expect(screen.getByText("Find the camp.")).toBeTruthy();
  fireEvent.change(screen.getByDisplayValue("Requirements met"), { target: { value: "unlocked" } });
  expect(screen.queryByRole("button", { name: "How to unlock Visible" })).toBeNull();
  fireEvent.click(screen.getByRole("button", { name: "Edit stage fields" }));
  expect(editor.selectStage).toHaveBeenCalledWith("pack:visible");
  expect(editor.setPage).toHaveBeenCalledWith("stages");
  expect(editor.mutateFile).not.toHaveBeenCalled();
  expect(editor.mutateFiles).not.toHaveBeenCalled();
});

it("shows dependent stages as ready in the requirements met simulation", () => {
  vi.clearAllMocks();
  editor.stages = [stage("pack:root", "Root"), dependentStage("pack:next", "Next", "pack:root")];
  render(<LayoutPage />);

  fireEvent.click(screen.getByRole("button", { name: "Player preview" }));
  expect(screen.getByRole("button", { name: /next, base, ready/i })).toBeTruthy();
});
