import { expect, it } from "vitest";
import { contextAssignments, contextMap, readContexts, replaceContexts } from "./contexts";
import { parseLuckPerms, serializeInbound, updateInboundBlock, updateOutboundBlock } from "./integrations";

const section = "luckperms.inbound.contexts";
const source = `[[luckperms.inbound]]
id = "rank"
groups = ["chef"]
[${section}]
# Preserve the explanation.
"server.name" = [
  "survival,hard", # Keep this value note.
  'other#world',
] # Keep the field note.
'custom:region' = ["north"]
[luckperms.inbound.future]
unknown = "preserve"
`;

it("reads quoted keys, multiline arrays, literal strings and comments without splitting values", () => {
  expect(readContexts(source, section)).toMatchObject({ values: {
    "server.name": ["survival,hard", "other#world"], "custom:region": ["north"]
  } });
  expect(readContexts(source, section).error).toBeUndefined();
  expect(replaceContexts(source, section, readContexts(source, section).values)).toBe(source);
});

it("edits only changed context assignments and retains comments and unrelated child tables", () => {
  const changed = replaceContexts(source, section, { "server.name": ["new"], "custom:region": ["north"], world: ["one", "two"] });
  expect(changed).toContain("# Keep this value note.");
  expect(changed).toContain("# Keep the field note.");
  expect(changed).toContain("# Preserve the explanation.");
  expect(changed).toContain("'custom:region' = [\"north\"]");
  expect(changed).toContain('[luckperms.inbound.future]\nunknown = "preserve"');
  expect(readContexts(changed, section).values).toEqual({ "server.name": ["new"], "custom:region": ["north"], world: ["one", "two"] });
  const removed = replaceContexts(changed, section, {});
  expect(readContexts(removed, section).values).toEqual({});
  expect(removed).toContain("# Keep this value note.");
  expect(removed).toContain("# Keep the field note.");
});

it("retains blank and invalid values for server validation and rejects duplicate rows before source mutation", () => {
  expect(contextAssignments({ "bad key": [""], empty: [] })).toEqual(['"bad key" = [""]', '"empty" = []']);
  expect(() => contextMap([{ key: "world", values: ["one"] }, { key: "world", values: ["two"] }])).toThrow(/more than once/);
  expect(contextMap([{ key: "__proto__", values: ["safe"] }])).toEqual({ ["__proto__"]: ["safe"] });
  const encoded = serializeInbound({ id: "rank", groups: ["chef"], permissions: [], match: "all", contexts: { "bad\nkey": [""] } });
  expect(encoded).toContain('"bad\\nkey" = [""]');
});

it("preserves malformed source during an unrelated edit and explains why changing its contexts is unsafe", () => {
  const invalid = `[[luckperms.inbound]]\nid = "rank"\ngroups = ["chef"]\n[${section}]\nworld = [true]\n`;
  const row = parseLuckPerms(invalid).inbound[0];
  expect(row.contextSourceError).toContain("quoted string");
  expect(updateInboundBlock(invalid, { ...row, id: "renamed" })).toContain('world = [true]');
  expect(() => updateInboundBlock(invalid, { ...row, contexts: { world: ["overworld"] } })).toThrow(/Correct the existing/);
});

it("reads multiline string variants, escaped Unicode, commas and line continuations", () => {
  const block = `[${section}]\nworld = ["""\nfirst\nsecond""", '''\nliteral\\path''', "\\U0001F600", """one\\\n  two"""]\n`;
  expect(readContexts(block, section).values.world).toEqual(["first\nsecond", "literal\\path", "😀", "onetwo"]);
});

it("normalizes multiline context values without changing their original line endings", () => {
  const block = `[["luckperms".'inbound']]\r\nid = 'chef'\r\n["luckperms".'inbound'.contexts]\r\nworld = ["""\r\nfirst\r\nsecond"""]\r\n`;
  const row = parseLuckPerms(block).inbound[0];
  expect(row.contexts.world).toEqual(["first\nsecond"]);
  expect(updateInboundBlock(block, row)).toBe(block);
  expect(updateInboundBlock(block, { ...row, contexts: { world: ["new"] } }))
    .toBe(block.replace('"""\r\nfirst\r\nsecond"""', '"new"'));
});

it("preserves inline context tables and appends at end of file with a valid newline", () => {
  const inline = '[[luckperms.inbound]]\nid = "rank"\ncontexts = { "world.name" = ["one"], region = ["north"] } # inline note\n';
  expect(readContexts(inline, section).values).toEqual({ "world.name": ["one"], region: ["north"] });
  expect(replaceContexts(inline, section, readContexts(inline, section).values)).toBe(inline);
  const changed = replaceContexts(inline, section, { world: ["two"] });
  expect(changed).toContain('contexts = { "world" = ["two"] } # inline note');
  const appended = replaceContexts(`[${section}]\nworld = ["one"]`, section, { world: ["one"], region: ["north"] });
  expect(readContexts(appended, section).values).toEqual({ world: ["one"], region: ["north"] });
});


it("keeps unrelated child contexts separate and refuses to replace a wrongly typed context field", () => {
  const nested = '[[luckperms.inbound]]\nid = "rank"\n[luckperms.inbound.future]\ncontexts = { world = ["preserve"] }\n';
  expect(readContexts(nested, section).values).toEqual({});
  expect(replaceContexts(nested, section, { region: ["north"] })).toContain('contexts = { world = ["preserve"] }');
  const invalid = '[[luckperms.inbound]]\nid = "rank"\n  "contexts" = "invalid"\n';
  expect(readContexts(invalid, section).error).toContain("table of string arrays");
  expect(() => replaceContexts(invalid, section, { region: ["north"] })).toThrow(/Correct the existing/);
});


it("keeps omitted defaults and unrelated fields intact when saving a context edit", () => {
  const inbound = `[[luckperms.inbound]]
id = 'rank' # Keep the identity note.
groups = ["chef"] # Keep the condition note.
[${section}]
world = ["one"]
`;
  const row = parseLuckPerms(inbound).inbound[0];
  expect(updateInboundBlock(inbound, row)).toBe(inbound);
  const updated = updateInboundBlock(inbound, { ...row, contexts: { world: ["two"] } });
  expect(updated).toBe(inbound.replace('["one"]', '["two"]'));
  expect(updated).not.toContain("permissions =");
  expect(updated).not.toContain("match =");
  const outbound = "[[luckperms.outbound]]\nid = 'grant' # identity\nkind = 'permission'\nvalue = 'profession.chef' # permission\n[luckperms.outbound.contexts]\nworld = [\"one\"]\n";
  const output = parseLuckPerms(outbound).outbound[0];
  expect(updateOutboundBlock(outbound, output)).toBe(outbound);
  expect(updateOutboundBlock(outbound, { ...output, contexts: { world: ["two"] } }))
    .toBe(outbound.replace('["one"]', '["two"]'));
});


it("retains comments inside inline context arrays when the map changes", () => {
  const inline = "[[luckperms.inbound]]\ncontexts = { world = [\n  'one', # Keep this note.\n] } # Keep the field note.\n";
  const changed = replaceContexts(inline, section, { region: ["two"] });
  expect(changed).toContain("# Keep this note.\ncontexts =");
  expect(changed).toContain("# Keep the field note.");
  expect(readContexts(changed, section).values).toEqual({ region: ["two"] });
});


it("updates mapping values inside their array row and preserves value comments", () => {
  const inbound = "[[luckperms.inbound]]\nid = 'old' # Keep the identity note.\ngroups = [\n  'chef', # Keep the group note.\n]\n[luckperms.inbound.contexts]\nworld = ['one']\n";
  const row = parseLuckPerms(inbound).inbound[0];
  const updated = updateInboundBlock(inbound, { ...row, id: "new", groups: ["master_chef"] });
  expect(updated.startsWith("[[luckperms.inbound]]\nid = \"new\"")).toBe(true);
  expect(updated).toContain("# Keep the identity note.");
  expect(updated).toContain("# Keep the group note.");
  expect(parseLuckPerms(updated).inbound[0]).toMatchObject({ id: "new", groups: ["master_chef"], contexts: { world: ["one"] } });
  const outbound = "[[luckperms.outbound]]\nid = 'output'\nkind = 'permission'\nvalue = 'old' # Keep the permission note.\n";
  const output = parseLuckPerms(outbound).outbound[0];
  const changed = updateOutboundBlock(outbound, { ...output, value: "new" });
  expect(changed).toBe(outbound.replace("value = 'old'", 'value = "new"'));
  expect(parseLuckPerms(changed).outbound[0].value).toBe("new");
});
