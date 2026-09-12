// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { discoverStages } from "../../lib/model";
import { parseInteractions } from "../../lib/integrations";
import { IntegrationsPanel } from "./IntegrationsPanel";

const editor = vi.hoisted(() => ({
  boot: { draft: { files: {} as Record<string, string> } },
  mutateFile: vi.fn(async () => {}),
  openDialog: vi.fn(),
  closeDialog: vi.fn()
}));

vi.mock("../../store/EditorContext", () => ({ useEditor: () => editor }));

const stagePath = "stages/chef/stage.toml";
const rulesPath = "stages/chef/rules.toml";
const original = '# Preserve this condition.\n[[interactions]]\ntype = "item_into_inventory"\nheld_item = "id:minecraft:carrot"\ntarget_kind = "block"\ntarget = "id:minecraft:chest"\n[interactions.while]\ntype = "dimension"\nid = "minecraft:the_end"\n';

beforeEach(() => {
  vi.clearAllMocks();
  editor.boot.draft.files = {
    [stagePath]: '[stage]\nid = "chef"\nname = "Chef"\n',
    [rulesPath]: original
  };
});

afterEach(cleanup);

function openInteraction() {
  render(<IntegrationsPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
  fireEvent.click(screen.getByRole("button", { name: "Add interaction" }));
  render(editor.openDialog.mock.calls[0][0].content);
}

describe("guided selective insertion", () => {
  it("saves both bread restrictions in one draft mutation while preserving existing source", async () => {
    openInteraction();
    fireEvent.change(screen.getByLabelText(/Held item selector/), { target: { value: "id:minecraft:bread" } });
    fireEvent.change(screen.getByLabelText(/Target block selector/), { target: { value: "id:selling_bin:selling_bin" } });
    fireEvent.click(screen.getByRole("checkbox", { name: /Also restrict GUI insertion/ }));
    fireEvent.change(screen.getByLabelText(/Inventory rule priority/), { target: { value: "150" } });
    fireEvent.click(screen.getByRole("button", { name: "Save interaction" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile).toHaveBeenCalledOnce();
    const [path, source] = editor.mutateFile.mock.calls[0] as unknown as [string, string];
    expect(path).toBe(rulesPath);
    expect(source.startsWith(original)).toBe(true);
    expect(parseInteractions(source).slice(1)).toMatchObject([
      { type: "item_on_block", heldItem: "id:minecraft:bread", targetBlock: "id:selling_bin:selling_bin" },
      { type: "item_into_inventory", heldItem: "id:minecraft:bread", targetKind: "block", target: "id:selling_bin:selling_bin", priority: 150 }
    ]);
    expect(source).not.toContain("block_right_click");
    expect(source).not.toContain("all:*");
  });

  it("keeps the original wildcard preset a standalone held item rule", async () => {
    openInteraction();
    fireEvent.click(screen.getByRole("button", { name: "All items" }));
    fireEvent.click(screen.getByRole("button", { name: "Save interaction" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const [, source] = editor.mutateFile.mock.calls[0] as unknown as [string, string];
    const rows = parseInteractions(source);
    expect(rows).toHaveLength(2);
    expect(rows[1]).toMatchObject({ type: "item_on_block", heldItem: "all:*", targetBlock: "id:selling_bin:selling_bin" });
  });

  it("discards the insertion pair when the dialog is canceled", () => {
    openInteraction();
    fireEvent.click(screen.getByRole("checkbox", { name: /Also restrict GUI insertion/ }));
    fireEvent.click(screen.getByRole("button", { name: "Cancel" }));
    expect(editor.closeDialog).toHaveBeenCalledOnce();
    expect(editor.mutateFile).not.toHaveBeenCalled();
  });
});
