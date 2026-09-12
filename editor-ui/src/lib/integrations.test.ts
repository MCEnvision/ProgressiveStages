import { describe, expect, it } from "vitest";
import { parseCommandPermissions, parseInteractions, parseLuckPerms, parseOwnership, replaceCommandPermissions, replaceInbound, serializeInbound, serializeInteraction, writeLuckPermsSettings, writeOwnership } from "./integrations";

describe("guided integration configuration", () => {
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
