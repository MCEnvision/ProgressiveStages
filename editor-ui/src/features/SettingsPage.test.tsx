// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { SettingsPage } from "./SettingsPage";

const editor = vi.hoisted(() => ({
  boot: {
    draft: { files: { "progressivestages.toml": "[enforcement]\nregion_tick_frequency = 5\n" } },
    schemas: [{
      id: "progressivestages:settings/enforcement/region_tick_frequency",
      file: "progressivestages.toml",
      path: "enforcement.region_tick_frequency",
      label: "Region tick frequency",
      help: "How often regions are checked.",
      type: "INTEGER",
      defaultValue: 20,
      required: false,
      prefixModes: [],
      enumValues: [],
      capabilities: [],
      restartRequirement: "NONE",
      controlHints: { min: 1, max: 100, generated: true }
    }]
  },
  hasLocalErrors: false,
  setLocalError: vi.fn(),
  validate: vi.fn(),
  mutateFile: vi.fn(async (..._args: unknown[]) => {})
}));

vi.mock("../store/EditorContext", () => ({ useEditor: () => editor }));

beforeEach(() => {
  vi.clearAllMocks();
  editor.mutateFile.mockResolvedValue(undefined);
});
afterEach(cleanup);

describe("settings range controls", () => {
  it("rejects values outside the schema range before mutating the draft", async () => {
    render(<SettingsPage />);
    const input = screen.getByDisplayValue("5");
    fireEvent.change(input, { target: { value: "0" } });
    fireEvent.blur(input);
    expect((await screen.findByRole("alert")).textContent).toContain("at least 1");
    expect(editor.mutateFile).not.toHaveBeenCalled();

    fireEvent.change(input, { target: { value: "8" } });
    fireEvent.blur(input);
    await waitFor(() => expect(editor.mutateFile).toHaveBeenCalledOnce());
  });
});
