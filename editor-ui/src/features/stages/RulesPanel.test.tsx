// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { discoverStages, ruleModels } from "../../lib/model";
import blockOverridesFixture from "./fixtures/block-overrides.toml?raw";
import { RulesPanel } from "./RulesPanel";

const editor = vi.hoisted(() => ({
  boot: { draft: { files: {} as Record<string, string> } },
  mutateFile: vi.fn(async (..._args: unknown[]) => {}), openDialog: vi.fn(), closeDialog: vi.fn(), runDraftAction: vi.fn(), setLocalError: vi.fn()
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
  editor.setLocalError.mockReset();
  editor.boot.draft.files = { [stagePath]: '[stage]\nid="chef"\n', [rulesPath]: "" };
});
afterEach(cleanup);

describe("legacy configuration and block overrides", () => {
  it("does not offer the ignored ores category as a legacy lock", () => {
    const legacyPath = "stages/challenge4.toml";
    editor.boot.draft.files = {
      [legacyPath]: '[stage]\nid = "challenge4"\ndisplay_name = "Industrial Age"\n\n[ores]\nlocked = ["mod:immersiveengineering"]\n'
    };
    const stage = discoverStages(editor.boot.draft.files)[0];
    render(<RulesPanel stage={stage}/>);
    fireEvent.click(screen.getByRole("button", { name: "Add rule" }));
    render(editor.openDialog.mock.calls[0][0].content);
    expect(screen.queryByRole("option", { name: /Ore visuals/ })).toBeNull();
    expect(screen.getByText(/does not create block overrides/i)).toBeTruthy();
    expect(editor.mutateFile).not.toHaveBeenCalled();
  });

  it("saves exact, tag, and mod targets as a usable block override list", async () => {
    const legacyPath = "stages/challenge4.toml";
    editor.boot.draft.files = { [legacyPath]: '[stage]\nid = "challenge4"\n' };
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    fireEvent.click(screen.getAllByRole("button", { name: "Add override" }).at(-1)!);
    render(editor.openDialog.mock.calls[0][0].content);

    fireEvent.change(screen.getByLabelText(/^Target selector/), { target: { value: "minecraft:diamond_ore" } });
    fireEvent.click(screen.getByRole("button", { name: "Add target" }));
    fireEvent.change(screen.getByLabelText(/^Target type/), { target: { value: "tag" } });
    fireEvent.change(screen.getByLabelText(/^Target selector/), { target: { value: "c:ores" } });
    fireEvent.click(screen.getByRole("button", { name: "Add target" }));
    fireEvent.change(screen.getByLabelText(/^Target type/), { target: { value: "mod" } });
    fireEvent.change(screen.getByLabelText(/^Target selector/), { target: { value: "immersiveengineering" } });
    fireEvent.click(screen.getByRole("button", { name: "Add target" }));
    fireEvent.change(screen.getByLabelText(/^Show as block/), { target: { value: "minecraft:stone" } });
    fireEvent.change(screen.getByLabelText(/^Drop item/), { target: { value: "minecraft:cobblestone" } });
    fireEvent.change(screen.getByLabelText(/^Priority/), { target: { value: "-12" } });
    fireEvent.click(screen.getAllByRole("button", { name: "Add override" }).at(-1)!);

    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const saved = String(editor.mutateFile.mock.calls[0][1]);
    expect(saved).toContain("[[blocks.overrides]]");
    expect(saved).toContain('targets = ["id:minecraft:diamond_ore", "tag:c:ores", "mod:immersiveengineering"]');
    expect(saved).toContain('display_as = "minecraft:stone"');
    expect(saved).toContain('drop_as = "minecraft:cobblestone"');
    expect(saved).toContain("priority = -12");
    expect(saved).not.toContain("[ores]");
  });

  it("edits an older ore override in place and preserves its unknown fields", async () => {
    const legacyPath = "stages/challenge4.toml";
    editor.boot.draft.files = { [legacyPath]: blockOverridesFixture };
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    const aliasCard = screen.getByText("Older ore override").closest("article")!;
    fireEvent.click(within(aliasCard).getByRole("button", { name: "Edit" }));
    render(editor.openDialog.mock.calls[0][0].content);
    fireEvent.change(screen.getByLabelText(/^Show as block/), { target: { value: "minecraft:deepslate" } });
    fireEvent.click(screen.getByRole("button", { name: "Save override" }));

    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const saved = String(editor.mutateFile.mock.calls[0][1]);
    expect(saved).toContain('[blocks]\nlocked = ["mod:immersiveengineering"]');
    expect(saved).toContain("[[blocks.overrides]]");
    expect(saved).toContain("[[ores.overrides]]");
    expect(saved).toContain('# Keep this comment.');
    expect(saved).toContain('display_as = "minecraft:deepslate"');
    expect(saved).toContain('custom_note = "keep"');
    expect(saved).toContain('targets = ["tags:c:ores", "mod:immersiveengineering"]');
  });
});

function openRule(source: string) {
  editor.boot.draft.files[rulesPath] = source;
  render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
  fireEvent.click(screen.getByRole("button", { name: "Edit" }));
  return render(editor.openDialog.mock.calls[0][0].content).container;
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
    const form = within(openRule(source));
    fireEvent.change(form.getByLabelText(/^Rule priority/), { target: { value: "175" } });
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

  it("removes an ignored legacy selector priority when editing the enchant lock", async () => {
    const path = "stages/chef.toml";
    editor.boot.draft.files = { [path]: '[stage]\nid="chef"\n[enchants]\nlocked=["id:minecraft:sharpness|priority=100"]\n' };
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    fireEvent.click(screen.getByRole("button", { name: "Edit" }));
    const form = within(render(editor.openDialog.mock.calls[0][0].content).container);
    expect((form.getByLabelText(/^Stage priority/) as HTMLInputElement).disabled).toBe(true);
    fireEvent.click(screen.getByRole("button", { name: "Update rule" }));
    await waitFor(() => expect(editor.closeDialog).toHaveBeenCalledOnce());
    const saved = editor.mutateFile.mock.calls[0][1];
    expect(saved).toContain('"id:minecraft:sharpness"');
    expect(saved).not.toContain("|priority=");
    expect(saved).not.toContain("[[rules]]");
  });

  it("allows structure entry without dropping independent protections", async () => {
    const structureSource = '[structures]\nlocked_entry=["id:minecraft:ancient_city"]\n[structures.rules]\npriority=-5\nprevent_block_place=true\ncustom="keep"\n';
    editor.boot.draft.files[rulesPath] = structureSource;
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    fireEvent.click(screen.getByLabelText("Allow entry"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledOnce());
    const saved = String(editor.mutateFile.mock.calls[0][1]);
    expect(saved).toContain("entry_allowed = true");
    expect(saved).toMatch(/prevent_block_place\s*=\s*true/);
    expect(saved).toMatch(/priority\s*=\s*-5/);
    expect(saved).toContain('custom="keep"');
  });

  it("serializes structure changes from the latest draft and clears the legacy padding fallback", async () => {
    const structureSource = '[structures]\nlocked_entry=["id:minecraft:ancient_city"]\nentry_padding=12\n[structures.rules]\nprevent_block_place=true\n';
    editor.boot.draft.files[rulesPath] = structureSource;
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    const padding = screen.getByDisplayValue("12");
    fireEvent.change(padding, { target: { value: "" } });
    fireEvent.blur(padding);
    fireEvent.click(screen.getByLabelText("Allow entry"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledTimes(2));
    const first = String(editor.mutateFile.mock.calls[0][1]);
    expect(first).not.toContain("entry_padding");
    const second = String(editor.mutateFile.mock.calls[1][1]);
    expect(second).not.toContain("entry_padding");
    expect(second).toContain("entry_allowed = true");
  });

  it("keeps queued structure edits on the accepted content", async () => {
    const resolvers: Array<() => void> = [];
    editor.mutateFile.mockImplementation(async () => new Promise<void>(resolve => resolvers.push(resolve)));
    editor.boot.draft.files[rulesPath] = "[structures]\nlocked_entry=[]\n";
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    fireEvent.click(screen.getByLabelText("Allow entry"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledOnce());
    fireEvent.click(screen.getByLabelText("Prevent block placement"));
    resolvers[0]();
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledTimes(2));
    const second = String(editor.mutateFile.mock.calls[1][1]);
    expect(second).toContain("entry_allowed = true");
    expect(second).toContain("prevent_block_place = true");
    resolvers[1]();
  });

  it("keeps queued structure saves on their own stage path", async () => {
    const resolvers: Array<() => void> = [];
    editor.mutateFile.mockImplementation(async () => new Promise<void>(resolve => resolvers.push(resolve)));
    const stage2Path = "stages/baker/stage.toml";
    const rules2Path = "stages/baker/rules.toml";
    editor.boot.draft.files = {
      [stagePath]: '[stage]\nid="chef"\n',
      [rulesPath]: "[structures]\nlocked_entry=[]\n",
      [stage2Path]: '[stage]\nid="baker"\n',
      [rules2Path]: "[structures]\nlocked_entry=[]\n"
    };
    const stages = discoverStages(editor.boot.draft.files);
    const view = render(<RulesPanel stage={stages.find(stage => stage.rulesPath === rulesPath)!}/>);
    fireEvent.click(screen.getByLabelText("Allow entry"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledOnce());
    view.rerender(<RulesPanel stage={stages.find(stage => stage.rulesPath === rules2Path)!}/>);
    fireEvent.click(screen.getByLabelText("Allow entry"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledTimes(2));
    expect(editor.mutateFile.mock.calls[0][0]).toBe(rulesPath);
    expect(editor.mutateFile.mock.calls[1][0]).toBe(rules2Path);
    resolvers.forEach(resolve => resolve());
  });

  it("reloads structure controls for the selected stage while another save is pending", async () => {
    const resolvers: Array<() => void> = [];
    editor.mutateFile.mockImplementation(async () => new Promise<void>(resolve => resolvers.push(resolve)));
    const stage2Path = "stages/baker/stage.toml";
    const rules2Path = "stages/baker/rules.toml";
    editor.boot.draft.files = {
      [stagePath]: '[stage]\nid="chef"\n',
      [rulesPath]: "[structures]\nlocked_entry=[]\n",
      [stage2Path]: '[stage]\nid="baker"\n',
      [rules2Path]: "[structures]\nlocked_entry=[]\n"
    };
    const stages = discoverStages(editor.boot.draft.files);
    const view = render(<RulesPanel stage={stages.find(stage => stage.rulesPath === rulesPath)!}/>);
    fireEvent.click(screen.getByLabelText("Allow entry"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledOnce());
    view.rerender(<RulesPanel stage={stages.find(stage => stage.rulesPath === rules2Path)!}/>);
    await waitFor(() => expect((screen.getByLabelText("Allow entry") as HTMLInputElement).checked).toBe(false));
    view.rerender(<RulesPanel stage={stages.find(stage => stage.rulesPath === rulesPath)!}/>);
    await waitFor(() => expect((screen.getByLabelText("Allow entry") as HTMLInputElement).checked).toBe(true));
    resolvers[0]();
  });

  it("rejects structure priorities outside the signed integer range", async () => {
    editor.boot.draft.files[rulesPath] = "[structures]\nlocked_entry=[]\n";
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    const priority = screen.getByPlaceholderText("Inherited");
    fireEvent.change(priority, { target: { value: "2147483648" } });
    fireEvent.blur(priority);
    expect((await screen.findByRole("alert")).textContent).toContain("2147483647");
    expect(editor.mutateFile).not.toHaveBeenCalled();
  });

  it("blocks validation when entry padding is not a whole number", async () => {
    editor.boot.draft.files[rulesPath] = "[structures]\nlocked_entry=[]\n";
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    const padding = screen.getByPlaceholderText("0");
    fireEvent.change(padding, { target: { value: "1.5" } });
    fireEvent.blur(padding);
    expect((await screen.findByRole("alert")).textContent).toContain("whole number");
    expect(editor.setLocalError).toHaveBeenCalledWith("structure:stages/chef/rules.toml:padding", "Use a whole number of 0 or more.");
    expect(editor.mutateFile).not.toHaveBeenCalled();
  });

  it("does not reuse a rejected structure edit for the next save", async () => {
    editor.mutateFile.mockRejectedValueOnce(new Error("Draft storage unavailable.")).mockResolvedValue(undefined);
    editor.boot.draft.files[rulesPath] = "[structures]\nlocked_entry=[]\n";
    render(<RulesPanel stage={discoverStages(editor.boot.draft.files)[0]}/>);
    fireEvent.click(screen.getByLabelText("Allow entry"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledOnce());
    fireEvent.click(screen.getByLabelText("Prevent block placement"));
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledTimes(2));
    const second = String(editor.mutateFile.mock.calls[1][1]);
    expect(second).not.toContain("entry_allowed = true");
    expect(second).toContain("prevent_block_place = true");
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
    const form = within(openRule(source));
    fireEvent.change(form.getByLabelText(/^Rule priority/), { target: { value: "175" } });
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
    const form = within(openRule(sources[0]));
    editor.boot.draft.files[rulesPath] = sources[0].replace("Keep this description", "Another edit");
    fireEvent.change(form.getByLabelText(/^Rule priority/), { target: { value: "175" } });
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
