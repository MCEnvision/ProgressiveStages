// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import { EditorApi, EditorApiError } from "../lib/api";
import type { Bootstrap, ReviewResult } from "../types";
import { EditorProvider, useEditor } from "./EditorContext";

const validation = { valid: true, errors: [], warnings: [], stages: 1, revision: 4 };
const bootstrap = {
  draft: { files: {}, revision: 9, canUndo: false, canRedo: false },
  catalog: { revision: 1 }, capabilities: []
} as unknown as Bootstrap;

function Controls() {
  const editor = useEditor();
  return <>
    <button onClick={() => void editor.openReview()}>Review</button>
    <button onClick={() => void editor.apply()}>Apply</button>
    <output>{editor.review ? `Review ${editor.review.revision}` : "No review"}</output>
    {editor.notices.map(notice => <p key={notice.id}>{notice.message}</p>)}
  </>;
}

beforeEach(() => {
  vi.spyOn(EditorApi.prototype, "bootstrap").mockResolvedValue(bootstrap);
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

it("applies the displayed review revision and updates it when a new review opens", async () => {
  let revision = 4;
  const request = vi.spyOn(EditorApi.prototype, "request").mockImplementation(async payload => {
    if (payload.action === "review") return { revision, diff: [], validation } as ReviewResult;
    return { success: true, configurationRevision: 2 };
  });
  render(<EditorProvider><Controls/></EditorProvider>);
  fireEvent.click(screen.getByRole("button", { name: "Review" }));
  await screen.findByText("Review 4");
  fireEvent.click(screen.getByRole("button", { name: "Apply" }));
  await waitFor(() => expect(request).toHaveBeenCalledWith({ action: "apply", revision: 4, confirmed: true }));
  revision = 5;
  fireEvent.click(screen.getByRole("button", { name: "Review" }));
  await screen.findByText("Review 5");
  fireEvent.click(screen.getByRole("button", { name: "Apply" }));
  await waitFor(() => expect(request).toHaveBeenCalledWith({ action: "apply", revision: 5, confirmed: true }));
});

it("discards a rejected stale review and reloads the server draft before another apply", async () => {
  const request = vi.spyOn(EditorApi.prototype, "request").mockImplementation(async payload => {
    if (payload.action === "review") return { revision: 4, diff: [], validation };
    throw new EditorApiError("draft_conflict", "Review the current changes.", { currentRevision: 5 });
  });
  render(<EditorProvider><Controls/></EditorProvider>);
  fireEvent.click(screen.getByRole("button", { name: "Review" }));
  await screen.findByText("Review 4");
  fireEvent.click(screen.getByRole("button", { name: "Apply" }));
  await screen.findByText("Review the current changes.");
  expect(screen.getByText("No review")).toBeTruthy();
  expect(EditorApi.prototype.bootstrap).toHaveBeenCalledTimes(2);
  request.mockClear();
  fireEvent.click(screen.getByRole("button", { name: "Apply" }));
  expect(request).not.toHaveBeenCalled();
});
