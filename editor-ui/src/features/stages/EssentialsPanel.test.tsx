// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import { discoverStages } from "../../lib/model";
import { EssentialsPanel } from "./EssentialsPanel";

const editor = vi.hoisted(() => ({
  boot: { draft: { files: {} as Record<string, string> } },
  stages: [] as ReturnType<typeof discoverStages>,
  mutateFile: vi.fn(async (..._args: unknown[]) => {}),
  mutateFiles: vi.fn(async (..._args: unknown[]) => {}),
  openDialog: vi.fn(),
  closeDialog: vi.fn()
}));
vi.mock("../../store/EditorContext", () => ({ useEditor: () => editor }));
vi.mock("../../components/CatalogPicker", () => ({ CatalogPicker: () => null }));
afterEach(cleanup);

it("edits the authored what to do next guide fields", async () => {
  vi.clearAllMocks();
  const stagePath = "stages/guide/stage.toml";
  const files = { [stagePath]: '[stage]\nid = "pack:guide"\ndisplay_name = "Guide"\n' };
  editor.boot.draft.files = files;
  editor.stages = discoverStages(files);
  render(<EssentialsPanel stage={editor.stages[0]}/>);

  fireEvent.change(screen.getByLabelText(/How to unlock/), { target: { value: "Mine the 🪨 vein.\nReturn to camp." } });
  fireEvent.blur(screen.getByLabelText(/How to unlock/));
  fireEvent.change(screen.getByLabelText(/Guide recommendation/), { target: { value: "include" } });

  await waitFor(() => expect(editor.mutateFile).toHaveBeenCalled());
  expect(editor.mutateFile.mock.calls.some(call => String(call[1]).includes("how_to_unlock"))).toBe(true);
  expect(editor.mutateFile.mock.calls.some(call => String(call[1]).includes('recommendation = "include"'))).toBe(true);
});
