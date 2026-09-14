import { describe, expect, it } from "vitest";
import { extractRows, replaceRows } from "./tomlRows";
import { parseCommandPermissions, parseInteractions, parseLuckPerms, replaceCommandPermissions, replaceInbound, replaceInteractions, replaceOutbound, serializeInbound, serializeInteraction, updateCommandPermissionBlock, updateInboundBlock, updateInteractionBlock, updateOutboundBlock } from "./integrations";

const interaction = "{type='item_on_block', held_item='id:minecraft:bread', target_block='id:selling_bin:selling_bin', while={type='dimension',id='minecraft:the_end'}, extension={note='Keep , } ['}}";

describe("inline access rows", () => {
  it("edits one interaction field while retaining nested conditions and extension values", () => {
    const source = `# Keep.\r\ninteractions = [\r\n  ${interaction}, # Bread.\r\n]\r\n[extension]\r\nvalue='keep'\r\n`;
    const [row] = parseInteractions(source);
    expect(row.heldItem).toBe("id:minecraft:bread");
    expect(replaceInteractions(source, [row.sourceText!])).toBe(source);
    const updated = updateInteractionBlock(row.sourceText!, { ...row, heldItem: "id:minecraft:carrot" });
    expect(replaceInteractions(source, [updated])).toBe(source.replace("held_item='id:minecraft:bread'", 'held_item="id:minecraft:carrot"'));
  });

  it("changes command paths without adding the omitted descendant default", () => {
    const source = "stage={id='chef'}\ncommand_permissions=[{id='home', path='home', extra={value='keep'}}]\n";
    const [row] = parseCommandPermissions(source);
    expect(row.descendants).toBe(true);
    const updated = updateCommandPermissionBlock(row.sourceText!, { ...row, path: "sethome" });
    expect(replaceCommandPermissions(source, [updated])).toBe(source.replace("path='home'", 'path="sethome"'));
  });

  it("edits nested inbound rows and contexts without filling omitted fields", () => {
    const source = 'luckperms={inbound_mode="permanent", inbound=[{id="chef", groups=["chef"], contexts={"server.name"=["first,second"]}, extension={note="keep"}}]}\n';
    const [row] = parseLuckPerms(source).inbound;
    expect(row).toMatchObject({ id: "chef", groups: ["chef"], permissions: [], match: "all", contexts: { "server.name": ["first,second"] } });
    expect(updateInboundBlock(row.sourceText!, row)).toBe(row.sourceText);
    const updated = updateInboundBlock(row.sourceText!, { ...row, contexts: { "server.name": ["third"] } });
    expect(replaceInbound(source, [updated])).toBe(source.replace('{"server.name"=["first,second"]}', '{ "server.name" = ["third"] }'));
  });

  it("adds contexts to an outbound row and keeps its permission kind omitted", () => {
    const source = "[luckperms]\noutbound=[{id='home', value='home.use'}]\n";
    const [row] = parseLuckPerms(source).outbound;
    const updated = updateOutboundBlock(row.sourceText!, { ...row, contexts: { world: ["minecraft:overworld"] } });
    const result = replaceOutbound(source, [updated]);
    expect(result).toContain('"contexts" = { "world" = ["minecraft:overworld"] }');
    expect(result).not.toContain("kind");
    expect(parseLuckPerms(result).outbound[0].contexts).toEqual({ world: ["minecraft:overworld"] });
  });

  it("reads and replaces dotted inline contexts without leaving duplicate declarations", () => {
    const source = 'luckperms.inbound=[{id="chef", contexts."server.name"=["local"], groups=["chef"], contexts.world=["overworld"]}]\n';
    const [row] = parseLuckPerms(source).inbound;
    expect(row.contexts).toEqual({ "server.name": ["local"], world: ["overworld"] });
    const updated = updateInboundBlock(row.sourceText!, { ...row, contexts: { world: ["nether"] } });
    const result = replaceInbound(source, [updated]);
    expect(parseLuckPerms(result).inbound[0].contexts).toEqual({ world: ["nether"] });
    expect(result).not.toContain("contexts.");
    expect(result).toContain('groups=["chef"]');
  });

  it.each(["[]", "[ ]", "[\n# Keep.\n]", `[${interaction}]`, `[${interaction}, # Keep.\n]`])("adds a row to %s without declaring a conflicting array table", array => {
    const source = `interactions=${array}\n`;
    const existing = parseInteractions(source);
    const extra = { ...parseInteractions(`interactions=[${interaction}]`)[0], heldItem: "id:minecraft:carrot" };
    const result = replaceInteractions(source, [...existing.map(row => row.sourceText!), serializeInteraction(extra)]);
    expect(result).not.toContain("[[interactions]]");
    expect(parseInteractions(result)).toHaveLength(existing.length + 1);
    expect(parseInteractions(result).at(-1)?.heldItem).toBe("id:minecraft:carrot");
    if (source.includes("# Keep.")) expect(result).toContain("# Keep.");
  });

  it("adds a mapping array inside an existing inline LuckPerms table", () => {
    const source = "luckperms={enabled=true, extension={note='keep'}}\n";
    const result = replaceInbound(source, [serializeInbound({ id: "chef", groups: ["chef"], permissions: [], match: "all", contexts: { "server.name": ["local"] } })]);
    expect(parseLuckPerms(result).inbound[0]).toMatchObject({ id: "chef", contexts: { "server.name": ["local"] } });
    expect(result).toContain("extension={note='keep'}");
    expect(result).not.toContain("[[luckperms.inbound]]");
  });

  it.each([0, 1, 2])("removes row %i and retains all source comments", index => {
    const source = "command_permissions=[\n{id='a',path='a'}, # First.\n{id='b',path='b',notes=[\"b\", # Inside.\n]}, # Second.\n{id='c',path='c'}, # Third.\n]\n";
    const rows = parseCommandPermissions(source);
    const result = replaceCommandPermissions(source, rows.filter((_, current) => current !== index).map(row => row.sourceText!));
    expect(parseCommandPermissions(result).map(row => row.id)).toEqual(rows.filter((_, current) => current !== index).map(row => row.id));
    for (const note of ["# First.", "# Inside.", "# Second.", "# Third."]) expect(result.split(note)).toHaveLength(2);
    const empty = replaceCommandPermissions(result, []);
    expect(parseCommandPermissions(empty)).toHaveLength(0);
    for (const note of ["# First.", "# Inside.", "# Second.", "# Third."]) expect(empty.split(note)).toHaveLength(2);
  });

  it("retains comments from a changed array value outside the inline row", () => {
    const source = 'luckperms.inbound=[{id="chef",groups=[\n"chef", # Keep.\n]}]\n';
    const [row] = parseLuckPerms(source).inbound;
    const updated = updateInboundBlock(row.sourceText!, { ...row, groups: ["master_chef"] });
    expect(updated).toContain("# Keep.\n{");
    const result = replaceInbound(source, [updated]);
    expect(result.match(/# Keep\./g)).toHaveLength(1);
    expect(parseLuckPerms(result).inbound[0].groups).toEqual(["master_chef"]);
  });

  it("rejects a mixed array instead of overwriting entries hidden by its guided view", () => {
    const source = "command_permissions=[{id='home', path='home'}, false]\n";
    expect(extractRows(source, "command_permissions")).toEqual([]);
    expect(() => replaceRows(source, "command_permissions", [])).toThrow(/Every row must be a TOML table/);
  });
});
