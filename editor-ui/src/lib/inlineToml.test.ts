import { describe, expect, it } from "vitest";
import fixtures from "./fixtures/toml-inline.json?raw";
import { readTomlValue, removeTomlValue, stringValue, upsertToml } from "./toml";
import { parseLuckPerms, parseOwnership, writeLuckPermsSettings, writeOwnership } from "./integrations";

interface Fixture { name: string; before: string; after: string; path: string; value: unknown; operation: string }

describe("inline TOML fields", () => {
  it.each(JSON.parse(fixtures) as Fixture[])("preserves source for $name", fixture => {
    const updated = fixture.operation === "remove" ? removeTomlValue(fixture.before, fixture.path)
      : upsertToml(fixture.before, fixture.path, fixture.value);
    expect(updated).toBe(fixture.after);
    if (fixture.operation === "remove") expect(readTomlValue(updated, fixture.path)).toBe("");
    else if (typeof fixture.value === "string") expect(stringValue(readTomlValue(updated, fixture.path))).toBe(fixture.value);
  });

  it("uses the ownership controls for every inline ownership transition", () => {
    const source = "stage = { id='chef', team_stage=false, extension={ note='keep' } } # Keep.\n";
    expect(parseOwnership(source)).toBe("personal");
    expect(writeOwnership(source, "personal")).toBe(source);
    const inherited = writeOwnership(source, "inherit");
    expect(inherited).toBe("stage = { id='chef', extension={ note='keep' } } # Keep.\n");
    const server = writeOwnership(source, "server");
    expect(parseOwnership(server)).toBe("server");
    expect(readTomlValue(server, "stage.team_stage")).toBe("");
    const team = writeOwnership(server, "team");
    expect(parseOwnership(team)).toBe("team");
    expect(stringValue(readTomlValue(team, "stage.scope"))).toBe("team");
    expect(writeOwnership(server, "inherit")).toBe(inherited);
    expect(team).toContain("extension={ note='keep' }");
  });

  it("reads and changes inline LuckPerms retention without replacing mappings", () => {
    const source = "stage={id='chef'}\nluckperms={enabled=false, inbound_mode='permanent', inbound=[{id='chef',groups=['chef'] }]} # Keep.\n";
    expect(parseLuckPerms(source)).toMatchObject({ present: true, enabled: false, inboundMode: "permanent" });
    expect(writeLuckPermsSettings(source, false, "permanent")).toBe(source);
    expect(writeLuckPermsSettings(source, true, "synchronized"))
      .toBe(source.replace("enabled=false", "enabled=true").replace("inbound_mode='permanent'", 'inbound_mode="synchronized"'));
    expect(parseLuckPerms("luckperms={}\n").present).toBe(true);
  });

  it.each(["'Chef'", "['Chef']", "false"])("does not overwrite a non table ancestor %s", value => {
    expect(() => upsertToml(`stage={display=${value}}\n`, "stage.display.name", "Chef")).toThrow(/parent value must be a TOML table/);
  });

  it.each(["{id='chef',}", "{id='chef' team_stage=false}", "{id='chef', team_stage=}"])("rejects ambiguous inline source %s", value => {
    expect(readTomlValue(`stage=${value}\n`, "stage.team_stage")).toBe("");
    expect(() => upsertToml(`stage=${value}\n`, "stage.team_stage", false)).toThrow();
  });
});
