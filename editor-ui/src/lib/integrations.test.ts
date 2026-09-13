import { describe, expect, it } from "vitest";
import { appendRow, parseCommandPermissions, parseInteractions, parseLuckPerms, parseOwnership, replaceCommandPermissions, replaceInbound, serializeContainerInsertionPair, serializeInbound, serializeInteraction, updateInteractionBlock, updateCommandPermissionBlock, writeLuckPermsSettings, writeOwnership } from "./integrations";

describe("guided integration configuration", () => {
  it("authors selective insertion together without adding a menu access or wildcard rule", () => {
    const original = '# Keep the existing conditional rule.\n[[interactions]]\ntype = "item_into_inventory"\nheld_item = "id:minecraft:carrot"\ntarget_kind = "block"\ntarget = "id:minecraft:chest"\n[interactions.while]\ntype = "dimension"\nid = "minecraft:the_end"\n';
    const pair = serializeContainerInsertionPair({ type: "item_on_block", heldItem: "id:minecraft:bread", targetBlock: "id:selling_bin:selling_bin", targetEntity: "", targetKind: "", target: "", effect: "lock", priority: 100, description: "Chef profession" });
    const source = appendRow(original, pair);
    expect(source.startsWith(original)).toBe(true);
    const rows = parseInteractions(source);
    expect(rows).toHaveLength(3);
    expect(rows[1]).toMatchObject({ type: "item_on_block", heldItem: "id:minecraft:bread", targetBlock: "id:selling_bin:selling_bin" });
    expect(rows[2]).toMatchObject({ type: "item_into_inventory", heldItem: "id:minecraft:bread", targetKind: "block", target: "id:selling_bin:selling_bin", effect: "lock", priority: 100 });
    expect(pair).not.toContain("block_right_click");
    expect(pair).not.toContain("all:*");
    const conditional = `${rows[2].sourceText}\n[interactions.while]\ntype = "dimension"\nid = "minecraft:the_end"\n`;
    const edited = updateInteractionBlock(conditional, { ...rows[2], priority: 150 });
    expect(edited).toContain('priority = 150');
    expect(edited).toContain('[interactions.while]\ntype = "dimension"\nid = "minecraft:the_end"');
  });

  it("maps ownership choices without writing the rejected player scope", () => {
    const source = "# keep this note\n[stage]\nid = \"pack:chef\"\n";
    const personal = writeOwnership(source, "personal");
    expect(personal).toContain("scope = \"team\"");
    expect(personal).toContain("team_stage = false");
    expect(personal).not.toContain('scope = "player"');
    expect(parseOwnership(personal)).toBe("personal");
    expect(parseOwnership(writeOwnership(personal, "inherit"))).toBe("inherit");
    expect(writeOwnership(source, "server")).toContain('scope = "server"');
  });

  it("round trips luckperms rows and nested contexts while retaining unrelated source", () => {
    const source = [
      "# keep this comment",
      "[luckperms]",
      "enabled = true",
      "inbound_mode = \"permanent\"",
      "",
      "[[luckperms.inbound]]",
      "id = \"chef\"",
      "groups = [\"chef\"]",
      "permissions = [\"professions.chef\"]",
      "match = \"any\"",
      "",
      "[luckperms.inbound.contexts]",
      "server = [\"survival\", \"hard\"]",
      "",
      "[metadata]",
      "owner = \"pack author\"",
      ""
    ].join("\n");
    const view = parseLuckPerms(source);
    expect(view.inbound[0]).toMatchObject({ id: "chef", groups: ["chef"], permissions: ["professions.chef"], match: "any", contexts: { server: ["survival", "hard"] } });
    expect(view.inboundMode).toBe("permanent");
    const updated = replaceInbound(source, [serializeInbound({ id: "chef", groups: ["chef", "master_chef"], permissions: [], match: "all", contexts: { server: ["survival"] } })]);
    expect(updated).toContain("# keep this comment");
    expect(updated).toContain('[metadata]');
    expect(updated).toContain('groups = ["chef", "master_chef"]');
    expect(parseLuckPerms(writeLuckPermsSettings(updated, false, "synchronized")).enabled).toBe(false);
    expect(serializeInbound({ id: "safe", groups: ["chef"], permissions: [], match: "all", contexts: { "bad\nvalue": ["x"] } })).not.toContain("bad\nvalue");
  });

  it("preserves command rows and exact interaction selectors through replacement", () => {
    const source = [
      "[[command_permissions]]",
      "id = \"home\"",
      "path = \"sethome\"",
      "descendants = true",
      "",
      "[[interactions]]",
      "type = \"item_on_block\"",
      "held_item = \"tag:c:armors\"",
      "target_block = \"id:selling_bin:selling_bin\"",
      ""
    ].join("\n");
    expect(parseCommandPermissions(source)[0]).toMatchObject({ id: "home", path: "sethome", descendants: true });
    expect(parseInteractions(source)[0]).toMatchObject({ type: "item_on_block", heldItem: "tag:c:armors", targetBlock: "id:selling_bin:selling_bin" });
    const commands = replaceCommandPermissions(source, ["[[command_permissions]]\nid = \"home\"\npath = \"sethome\"\ndescendants = false"]);
    expect(parseCommandPermissions(commands)[0].descendants).toBe(false);
    expect(commands).toContain('target_block = "id:selling_bin:selling_bin"');
    expect(serializeInteraction({ type: "item_on_block", heldItem: "all:*", targetBlock: "id:selling_bin:selling_bin", targetEntity: "", targetKind: "", target: "", effect: "lock", priority: 0, description: "all items" })).toContain('held_item = "all:*"');
  });
});


describe("command and interaction row editing", () => {
  it.each(["", "descendants = true # Include children.\n", "descendants = false # Only the exact literal.\n"])("preserves the command descendant selection and omission in %s", field => {
    const source = "[[command_permissions]]\nid = 'home' # Keep the ID note.\npath = 'sethome' # Keep the path note.\n" + field;
    const row = parseCommandPermissions(source)[0];
    expect(row).toMatchObject({ id: "home", path: "sethome", descendants: !field.startsWith("descendants = false") });
    expect(updateCommandPermissionBlock(source, row)).toBe(source);
    const changed = updateCommandPermissionBlock(source, { ...row, path: "home" });
    expect(changed).toBe(source.replace("path = 'sethome'", 'path = "home"'));
    expect(parseCommandPermissions(changed)[0].path).toBe("home");
  });

  it("edits the selected command row without absorbing an extension or adjacent row", () => {
    const source = "[stage]\nid = 'chef'\n[[command_permissions]]\nid = 'first'\npath = 'sethome'\n[command_permissions.future]\npath = 'preserve'\n[[command_permissions]]\nid = 'second'\npath = 'home'\ndescendants = false\n";
    const rows = parseCommandPermissions(source);
    expect(rows).toHaveLength(2);
    const changed = replaceCommandPermissions(source, [
      updateCommandPermissionBlock(rows[0].sourceText!, { ...rows[0], id: "renamed", descendants: false }), rows[1].sourceText!
    ]);
    expect(parseCommandPermissions(changed).map(row => [row.id, row.path, row.descendants]))
      .toEqual([["renamed", "sethome", false], ["second", "home", false]]);
    expect(changed).toContain("[command_permissions.future]\npath = 'preserve'");
    expect(changed).toContain("[stage]\nid = 'chef'");
  });

  it.each(["item_on_block", "block_right_click", "item_on_entity", "item_into_inventory"])("edits a %s row without rewriting its activation", type => {
    const target = type === "item_on_entity" ? "target_entity = 'id:minecraft:cow'"
      : type === "item_into_inventory" ? "target_kind = 'block'\ntarget = 'id:minecraft:chest'\npriority = 1_280 # Keep the priority note."
      : "target_block = 'id:selling_bin:selling_bin'";
    const source = `[[interactions]]
type = '${type}' # Keep the type note.
held_item = 'id:minecraft:bread' # Keep the item note.
${target}
[interactions.while]
type = 'dimension'
id = 'minecraft:the_end'
`;
    const row = parseInteractions(source)[0];
    expect(row.type).toBe(type);
    if (type === "item_into_inventory") expect(row.priority).toBe(1280);
    expect(updateInteractionBlock(source, row)).toBe(source);
    const changed = updateInteractionBlock(source, { ...row, heldItem: "id:minecraft:carrot" });
    expect(changed).toBe(source.replace("held_item = 'id:minecraft:bread'", 'held_item = "id:minecraft:carrot"'));
    expect(parseInteractions(changed)[0].heldItem).toBe("id:minecraft:carrot");
  });

  it("changes priority and clears an optional description inside the selected interaction", () => {
    const source = "[[interactions]]\ntype = 'item_into_inventory'\nheld_item = 'id:minecraft:bread'\ntarget_kind = 'block'\ntarget = 'id:minecraft:chest'\npriority = 100 # Priority note.\ndescription = 'old' # Description note.\n";
    const row = parseInteractions(source)[0];
    const changed = updateInteractionBlock(source, { ...row, priority: 150, description: "" });
    expect(changed).toBe(source.replace("priority = 100", "priority = 150").replace("description = 'old'", 'description = ""'));
    expect(parseInteractions(changed)[0]).toMatchObject({ priority: 150, description: "" });
  });
});
