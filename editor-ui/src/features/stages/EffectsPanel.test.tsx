// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import { discoverStages } from "../../lib/model";
import { EffectsPanel } from "./EffectsPanel";

const editor = vi.hoisted(() => ({
  boot: { draft: { files: {} as Record<string, string> } },
  mutateFile: vi.fn(async (..._args: unknown[]) => {}), openDialog: vi.fn(), closeDialog: vi.fn()
}));
vi.mock("../../store/EditorContext", () => ({ useEditor: () => editor }));
vi.mock("../../components/CatalogPicker", () => ({ InlineCatalogSearch: () => null }));
afterEach(cleanup);

it.each([false, true])("saves stage attributes in the rules file with legacy mode %s", async legacy => {
  vi.clearAllMocks();
  const stagePath = legacy ? "stages/chef.toml" : "stages/chef/stage.toml";
  const rulesPath = legacy ? stagePath : "stages/chef/rules.toml";
  const identity = '[stage]\nid="chef"\n';
  editor.boot.draft.files = { [stagePath]: identity, ...(!legacy ? { [rulesPath]: "# Existing rules.\n" } : {}) };
  render(<EffectsPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
  fireEvent.click(screen.getByRole("button", { name: "Stage attribute" }));
  render(editor.openDialog.mock.calls[0][0].content);
  fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "6" } });
  fireEvent.click(screen.getByRole("button", { name: "Add attribute" }));
  await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
  expect(editor.mutateFile).toHaveBeenCalledWith(rulesPath, expect.stringContaining("amount = 6"), "Stage attribute added");
  expect(editor.boot.draft.files[stagePath]).toBe(identity);
});
