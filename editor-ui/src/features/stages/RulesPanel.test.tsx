// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { discoverStages, ruleModels } from "../../lib/model";
import { RulesPanel } from "./RulesPanel";

const editor = vi.hoisted(() => ({
  boot: { draft: { files: {} as Record<string, string> } },
  mutateFile: vi.fn(async (..._args: unknown[]) => {}), openDialog: vi.fn(), closeDialog: vi.fn(), runDraftAction: vi.fn()
}));
vi.mock("../../store/EditorContext", () => ({ useEditor: () => editor }));
vi.mock("../../components/CatalogPicker", () => ({ InlineCatalogSearch: () => null }));
const stagePath = "stages/chef/stage.toml";
const rulesPath = "stages/chef/rules.toml";
const sources = [
  '# Keep.\r\n[[ "interactions" ]]\r\ntype="item_into_inventory"\r\nheld_item="id:minecraft:bread"\r\ntarget_kind="block"\r\ntarget="id:selling_bin:selling_bin"\r\npriority=150 # Keep priority note.\r\ndescription="Keep this description"\r\n[ "interactions" . "while" ] # Keep activation note.\r\ntype="dimension"\r\nid="minecraft:overworld"\r\nextension={note="keep"}\r\n\r\n[extension]\r\nvalue="keep"\r\n',
  'interactions=[{type="item_into_inventory", held_item="id:minecraft:bread", target_kind="block", target="id:selling_bin:selling_bin", priority=150, when={type="dimension", id="minecraft:overworld", extension={note="keep"}}, description="Keep this description"}] # Keep.\n'
];

beforeEach(() => {
  vi.clearAllMocks();
  editor.mutateFile.mockReset().mockResolvedValue(undefined);
  editor.boot.draft.files = { [stagePath]: '[stage]\nid="chef"\n', [rulesPath]: "" };
});
afterEach(cleanup);

function openRule(source: string) {
  editor.boot.draft.files[rulesPath] = source;
  render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
  fireEvent.click(screen.getByRole("button", { name: "Edit" }));
  render(editor.openDialog.mock.calls[0][0].content);
}

describe("generic rule editing", () => {
  const source = '# Keep this rule.\r\n[[rules]]\r\nid="chef/bread"\r\neffect="lock"\r\naction="use"\r\npriority=100 # Keep priority.\r\nstage_state="missing"\r\ncooldown="10s"\r\ntargets.items=["id:minecraft:bread", "id:minecraft:apple"]\r\nwhile={all=[{type="weather",value="rain"},{type="dimension",id="minecraft:overworld"}]}\r\npresentation.jei="hide"\r\npresentation.emi="overlay"\r\ncustom="keep"\r\n[[rules.exceptions]]\r\neffect="exclude"\r\npriority=101\r\ntargets.items=["id:minecraft:apple"]\r\n';

  it("retains the complete rule group when saved unchanged", async () => {
    openRule(source);
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile).toHaveBeenCalledWith(rulesPath, source, "Rule saved to the draft");
  });

  it("edits priority without losing compound conditions or additional settings", async () => {
    openRule(source);
    fireEvent.change(screen.getByLabelText(/^Priority/), { target: { value: "175" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile).toHaveBeenCalledWith(rulesPath, source.replace("priority=100", "priority=175"), "Rule saved to the draft");
  });

  it("does not change missing stage ownership when adding a permanent condition", async () => {
    openRule('[[rules]]\nid="chef/bread"\neffect="lock"\naction="use"\ntargets.items=["id:minecraft:bread"]\n');
    fireEvent.change(screen.getByLabelText("Activation condition"), { target: { value: "weather" } });
    fireEvent.change(screen.getByLabelText(/^Condition target/), { target: { value: "rain" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const saved = editor.mutateFile.mock.calls[0][1];
    expect(saved).toContain("[[rules]]");
    expect(saved).not.toContain("[[temporary_rules]]");
    expect(saved).toContain('value = "rain"');
  });

  it("does not leave an old generic rule behind when changing to a crafting list", async () => {
    openRule('[[rules]]\nid="chef/bread"\neffect="lock"\naction="use"\ntargets.items=["id:minecraft:bread"]\n');
    fireEvent.change(screen.getByLabelText("Rule category"), { target: { value: "recipes" } });
    fireEvent.change(screen.getByLabelText("Recipe output item"), { target: { value: "id:minecraft:bread" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("explicit Source edit"));
    expect(editor.mutateFile).not.toHaveBeenCalled();
    expect(editor.closeDialog).not.toHaveBeenCalled();
  });

  it("does not discard generic settings when changing to inventory insertion", async () => {
    openRule(source);
    fireEvent.change(screen.getByLabelText("Rule category"), { target: { value: "interactions" } });
    fireEvent.change(screen.getByLabelText(/^Inserted item/), { target: { value: "id:minecraft:bread" } });
    fireEvent.change(screen.getByRole("textbox", { name: /^Destination/ }), { target: { value: "id:minecraft:chest" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("explicit Source edit"));
    expect(editor.mutateFile).not.toHaveBeenCalled();
    expect(editor.closeDialog).not.toHaveBeenCalled();
  });

  it("keeps explicit ownership while changing a rule lifetime", async () => {
    openRule(source);
    fireEvent.change(screen.getByLabelText("Lifetime"), { target: { value: "live" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const saved = String(editor.mutateFile.mock.calls[0][1]);
    expect(saved).toContain("[[temporary_rules]]");
    expect(saved).toContain('stage_state="missing"');
    expect(saved).toContain("[[temporary_rules.exceptions]]");
    expect(saved).toContain('cooldown="10s"');
    expect(saved).toContain('presentation.emi="overlay"');
  });

  it("edits a nested weather value without replacing sibling condition settings", async () => {
    const nested = '[[rules]]\nid="chef/bread"\neffect="lock"\naction="use"\n[rules.targets]\nitems=["id:minecraft:bread", "id:minecraft:apple"]\n[rules.while]\ntype="weather"\nvalue="rain" # Keep this note.\nextension="keep"\n';
    openRule(nested);
    fireEvent.change(screen.getByLabelText(/^Condition target/), { target: { value: "thunder" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile.mock.calls[0][1]).toBe(nested.replace('value="rain"', 'value="thunder"'));
  });

  it("replaces a scalar target instead of adding an ignored fallback", async () => {
    const scalar = '[[rules]]\nid="chef/bread"\neffect="lock"\naction="use"\ntargets.items="id:minecraft:bread"\n';
    openRule(scalar);
    fireEvent.change(screen.getByLabelText("Selected target"), { target: { value: "id:minecraft:apple" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile.mock.calls[0][1]).toBe(scalar.replace('id:minecraft:bread', 'id:minecraft:apple'));
  });

  it("refuses category changes that would detach an exception", async () => {
    openRule(source.replace(', "id:minecraft:apple"', ''));
    fireEvent.change(screen.getByLabelText("Rule category"), { target: { value: "blocks" } });
    fireEvent.change(screen.getByLabelText("Selected target"), { target: { value: "id:minecraft:stone" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("dependent targets or exceptions"));
    expect(editor.mutateFile).not.toHaveBeenCalled();
  });

  it("refuses stale generic edits", async () => {
    openRule(source);
    editor.boot.draft.files[rulesPath] = source.replace('cooldown="10s"', 'cooldown="20s"');
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("changed in another edit"));
    expect(editor.mutateFile).not.toHaveBeenCalled();
  });

  it("rejects conditional crafting instead of saving compiler rejected syntax", async () => {
    openRule('[recipes]\nlocked_items=["id:minecraft:bread"]\n');
    fireEvent.change(screen.getByLabelText("Activation condition"), { target: { value: "weather" } });
    fireEvent.change(screen.getByLabelText(/^Condition target/), { target: { value: "rain" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("Crafting locks"));
    expect(editor.mutateFile).not.toHaveBeenCalled();
  });

  it("keeps legacy enchant edits in the enforced classic category", async () => {
    const path = "stages/chef.toml";
    editor.boot.draft.files = { [path]: '[stage]\nid="chef"\n[enchants]\nlocked=["id:minecraft:sharpness|priority=100"]\n' };
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    fireEvent.click(screen.getByRole("button", { name: "Edit" }));
    render(editor.openDialog.mock.calls[0][0].content);
    fireEvent.change(screen.getByLabelText(/^Priority/), { target: { value: "175" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const saved = editor.mutateFile.mock.calls[0][1];
    expect(saved).toContain('"id:minecraft:sharpness|priority=175"');
    expect(saved).not.toContain("[[rules]]");
  });
});

describe("inventory condition authoring", () => {
  it.each(sources)("saves an unchanged rule without replacing its source", async source => {
    openRule(source);
    expect(screen.getByLabelText("Activation condition")).toHaveProperty("value", "dimension");
    expect(screen.getByLabelText(/^Condition target/)).toHaveProperty("value", "minecraft:overworld");
    expect(screen.getByLabelText("Required amount")).toHaveProperty("value", "1");
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile).toHaveBeenCalledWith(rulesPath, source, "Rule saved to the draft");
  });

  it.each(sources)("changes only the condition target", async source => {
    openRule(source);
    fireEvent.change(screen.getByLabelText(/^Condition target/), { target: { value: "minecraft:the_nether" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile).toHaveBeenCalledWith(rulesPath, source.replace("minecraft:overworld", "minecraft:the_nether"), "Rule saved to the draft");
  });

  it.each(sources)("changes priority while retaining the activation and unknown fields", async source => {
    openRule(source);
    fireEvent.change(screen.getByLabelText(/^Priority/), { target: { value: "175" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile).toHaveBeenCalledWith(rulesPath, source.replace("priority=150", "priority=175"), "Rule saved to the draft");
  });

  it.each(sources)("removes activation only when explicitly selected", async source => {
    openRule(source);
    fireEvent.change(screen.getByLabelText("Activation condition"), { target: { value: "none" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const [, updated] = editor.mutateFile.mock.calls[0] as unknown as [string, string];
    expect(ruleModels(updated)[0].conditionType).toBe("none");
    expect(updated).toContain('description="Keep this description"');
    if (source.includes("[extension]")) expect(updated).toContain('[extension]\r\nvalue="keep"');
    if (source.includes("# Keep activation note.")) expect(updated).toContain("# Keep activation note.");
  });

  it("keeps the edited form available after a failed save", async () => {
    editor.mutateFile.mockRejectedValueOnce(new Error("Draft storage unavailable."));
    openRule(sources[0]);
    fireEvent.change(screen.getByLabelText(/^Condition target/), { target: { value: "minecraft:the_nether" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("Draft storage unavailable."));
    expect(screen.getByLabelText(/^Condition target/)).toHaveProperty("value", "minecraft:the_nether");
    expect(editor.closeDialog).not.toHaveBeenCalled();
  });

  it("rejects a stale row instead of overwriting a concurrent source edit", async () => {
    openRule(sources[0]);
    editor.boot.draft.files[rulesPath] = sources[0].replace("Keep this description", "Another edit");
    fireEvent.change(screen.getByLabelText(/^Priority/), { target: { value: "175" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("changed in another edit"));
    expect(editor.mutateFile).not.toHaveBeenCalled();
    expect(editor.closeDialog).not.toHaveBeenCalled();
  });

  it("keeps the replacement rule when changing away from inventory insertion", async () => {
    openRule(sources[1]);
    fireEvent.change(screen.getByLabelText("Rule category"), { target: { value: "blocks" } });
    fireEvent.change(screen.getByLabelText("Selected target"), { target: { value: "id:minecraft:stone" } });
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const [, updated] = editor.mutateFile.mock.calls[0] as unknown as [string, string];
    expect(ruleModels(updated)).toHaveLength(1);
    expect(ruleModels(updated)[0]).toMatchObject({ category: "blocks", selector: "id:minecraft:stone" });
  });

  it("removes a rule without consuming its neighboring top level section", async () => {
    const source = '# Keep.\r\n[[ "rules" ]]\r\naction="use"\r\ntargets.items=["id:minecraft:bread"]\r\n[rules.extension]\r\nnote="keep with the rule"\r\n[extension]\r\nvalue="keep outside the rule"\r\n';
    editor.boot.draft.files[rulesPath] = source;
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    fireEvent.click(screen.getByRole("button", { name: "Remove" }));
    render(editor.openDialog.mock.calls[0][0].content);
    fireEvent.click(screen.getByRole("button", { name: "Remove rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    expect(editor.mutateFile).toHaveBeenCalledWith(rulesPath, '# Keep.\r\n[extension]\r\nvalue="keep outside the rule"\r\n', "The rule was removed");
  });
});
