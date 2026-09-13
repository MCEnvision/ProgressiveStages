import { expect, it } from "vitest";
import fixtureText from "./fixtures/toml-preservation.json?raw";
import { parseCommandPermissions, parseInteractions, parseLuckPerms, replaceCommandPermissions, replaceInbound, replaceInteractions, replaceOutbound, updateCommandPermissionBlock, updateInboundBlock, updateInteractionBlock, updateOutboundBlock, writeOwnership } from "./integrations";

const fixtures = JSON.parse(fixtureText) as { name: string; before: string; after: string; operation: string }[];

it.each(fixtures)("preserves source bytes for $name", fixture => {
  const text = fixture.before;
  let changed: string;
  if (fixture.operation === "ownership") changed = writeOwnership(text, "team");
  else if (fixture.operation === "inbound") {
    const rows = parseLuckPerms(text).inbound;
    expect(rows).toHaveLength(1);
    expect(rows[0].contextSourceError).toBeUndefined();
    expect(updateInboundBlock(rows[0].sourceText!, rows[0])).toBe(rows[0].sourceText);
    changed = replaceInbound(text, [updateInboundBlock(rows[0].sourceText!, { ...rows[0], contexts: { world: ["two"] } })]);
  } else if (fixture.operation === "outbound") {
    const rows = parseLuckPerms(text).outbound;
    expect(rows).toHaveLength(1);
    expect(rows[0].contextSourceError).toBeUndefined();
    expect(updateOutboundBlock(rows[0].sourceText!, rows[0])).toBe(rows[0].sourceText);
    changed = replaceOutbound(text, [updateOutboundBlock(rows[0].sourceText!, { ...rows[0], contexts: { "world.name": ["two"] } })]);
  } else if (fixture.operation === "command") {
    const rows = parseCommandPermissions(text);
    expect(rows).toHaveLength(1);
    changed = replaceCommandPermissions(text, [updateCommandPermissionBlock(rows[0].sourceText!, { ...rows[0], path: "home" })]);
  } else {
    const rows = parseInteractions(text);
    expect(rows).toHaveLength(1);
    changed = replaceInteractions(text, [updateInteractionBlock(rows[0].sourceText!, { ...rows[0], heldItem: "id:minecraft:carrot" })]);
  }
  expect(changed).toBe(fixture.after);
});
