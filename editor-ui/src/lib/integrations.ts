import { newline, replaceValue, requireEditable, samePath, scanToml } from "./tomlSource";
import { contextAssignments, quoted, readContexts, replaceContexts, stringArray } from "./contexts";
import type { InteractionModel, InboundModel, OutboundModel, CommandPermissionModel } from "../types";
import {
  appendTomlBlock,
  encodeToml,
  extractArrayGroups,
  lineValues,
  parseSimpleArray,
  readBlockValue,
  readTomlValue,
  removeTomlValue,
  replaceArrayGroups,
  stringValue,
  upsertToml
} from "./toml";

export type OwnershipChoice = "inherit" | "personal" | "team" | "server";

export interface LuckPermsView {
  present: boolean;
  enabled: boolean;
  inboundMode: "synchronized" | "permanent";
  inbound: InboundModel[];
  outbound: OutboundModel[];
}

export function parseOwnership(text: string): OwnershipChoice {
  if (stringValue(readTomlValue(text, "stage.scope")) === "server") return "server";
  const teamStage = readTomlValue(text, "stage.team_stage").trim().toLowerCase();
  if (teamStage === "false") return "personal";
  if (teamStage === "true") return "team";
  return "inherit";
}

export function writeOwnership(text: string, choice: OwnershipChoice): string {
  const current = parseOwnership(text);
  if (current === choice) return text;
  if (choice === "inherit") {
    const updated = removeTomlValue(text, "stage.team_stage");
    return current === "server" ? removeTomlValue(updated, "stage.scope") : updated;
  }
  if (choice === "server") return removeTomlValue(upsertToml(text, "stage.scope", "server"), "stage.team_stage");
  const updated = current === "server" ? upsertToml(text, "stage.scope", "team") : text;
  return upsertToml(updated, "stage.team_stage", choice === "team");
}


function mappingField(block: string, key: string): string {
  return readBlockValue(block, key);
}

function mappingString(block: string, key: string): string {
  const raw = mappingField(block, key);
  if (!raw) return "";
  try { return quoted(raw, 0).value; }
  catch { return stringValue(raw.split(/\r?\n/, 1)[0]); }
}

function mappingArray(block: string, key: string): string[] {
  const raw = mappingField(block, key);
  if (!raw) return [];
  try { return stringArray(raw, 0).values; }
  catch { return parseSimpleArray(raw.split(/\r?\n/, 1)[0]); }
}

export function parseLuckPerms(text: string): LuckPermsView {
  const sectionRaw = readTomlValue(text, "luckperms.enabled");
  const source = scanToml(text);
  const sectionPresent = source.tables.some(table => !table.array && samePath(table.path, ["luckperms"]))
    || source.values.some(value => !value.table && value.key[0] === "luckperms");
  const inbound = extractArrayGroups(text, "luckperms.inbound").map((block): InboundModel => ({
    id: mappingString(block.text, "id"),
    groups: mappingArray(block.text, "groups"),
    permissions: mappingArray(block.text, "permissions"),
    match: mappingString(block.text, "match") === "any" ? "any" : "all",
    contexts: readContexts(block.text, "luckperms.inbound.contexts").values,
    contextSourceError: readContexts(block.text, "luckperms.inbound.contexts").error,
    sourceText: block.text
  }));
  const outbound = extractArrayGroups(text, "luckperms.outbound").map((block): OutboundModel => ({
    id: mappingString(block.text, "id"),
    kind: mappingString(block.text, "kind") === "group" ? "group" : "permission",
    value: mappingString(block.text, "value"),
    contexts: readContexts(block.text, "luckperms.outbound.contexts").values,
    contextSourceError: readContexts(block.text, "luckperms.outbound.contexts").error,
    sourceText: block.text
  }));
  return {
    present: sectionPresent || inbound.length > 0 || outbound.length > 0,
    enabled: sectionRaw === "" || sectionRaw.toLowerCase() === "true",
    inboundMode: stringValue(readTomlValue(text, "luckperms.inbound_mode")) === "permanent" ? "permanent" : "synchronized",
    inbound,
    outbound
  };
}

export function writeLuckPermsSettings(text: string, enabled: boolean, inboundMode: LuckPermsView["inboundMode"]): string {
  const previous = parseLuckPerms(text);
  const updated = previous.enabled === enabled ? text : upsertToml(text, "luckperms.enabled", enabled);
  return previous.inboundMode === inboundMode ? updated : upsertToml(updated, "luckperms.inbound_mode", inboundMode);
}

function listValue(value: string): string[] {
  return lineValues(value).map(item => item.trim()).filter(Boolean);
}

export function serializeInbound(row: InboundModel): string {
  const lines = [
    "[[luckperms.inbound]]",
    `id = ${encodeToml(row.id.trim())}`,
    `groups = ${encodeToml(listValue(row.groups.join(", ")))}`,
    `permissions = ${encodeToml(listValue(row.permissions.join(", ")))}`,
    `match = ${encodeToml(row.match)}`
  ];
  return lines.join("\n") + contextsBlockFromMap("luckperms.inbound.contexts", row.contexts);
}

export function serializeOutbound(row: OutboundModel): string {
  const lines = [
    "[[luckperms.outbound]]",
    `id = ${encodeToml(row.id.trim())}`,
    `kind = ${encodeToml(row.kind)}`,
    `value = ${encodeToml(row.value.trim())}`
  ];
  return lines.join("\n") + contextsBlockFromMap("luckperms.outbound.contexts", row.contexts);
}

function contextsBlockFromMap(section: string, contexts: Record<string, string[]>): string {
  const entries = contextAssignments(contexts);
  return entries.length ? `\n[${section}]\n${entries.join("\n")}` : "";
}

export function replaceInbound(text: string, rows: string[]): string {
  return replaceArrayGroups(text, "luckperms.inbound", rows);
}

export function replaceOutbound(text: string, rows: string[]): string {
  return replaceArrayGroups(text, "luckperms.outbound", rows);
}


function updateMappingValue(original: string, key: string, value: unknown): string {
  const source = scanToml(original);
  requireEditable(source);
  const root = source.tables[0];
  const assignment = source.values.find(entry => entry.table === root && samePath(entry.key, [key]));
  if (assignment) return replaceValue(original, assignment, encodeToml(value));
  const start = root?.end || 0;
  return original.slice(0, start) + (start && original[start - 1] !== "\n" ? newline(original) : "")
    + `${key} = ${encodeToml(value)}${newline(original)}` + original.slice(start);
}

export function updateInboundBlock(original: string, row: InboundModel): string {
  const previous = parseLuckPerms(original).inbound[0];
  let updated = original;
  for (const key of ["id", "groups", "permissions", "match"] as const) {
    if (!previous || JSON.stringify(previous[key]) !== JSON.stringify(row[key])) updated = updateMappingValue(updated, key, row[key]);
  }
  return replaceContexts(updated, "luckperms.inbound.contexts", row.contexts);
}

export function updateOutboundBlock(original: string, row: OutboundModel): string {
  const previous = parseLuckPerms(original).outbound[0];
  let updated = original;
  for (const key of ["id", "kind", "value"] as const) {
    if (!previous || previous[key] !== row[key]) updated = updateMappingValue(updated, key, row[key]);
  }
  return replaceContexts(updated, "luckperms.outbound.contexts", row.contexts);
}

export function updateCommandPermissionBlock(original: string, row: CommandPermissionModel): string {
  const previous = parseCommandPermissions(original)[0];
  let updated = original;
  for (const key of ["id", "path", "descendants"] as const) {
    if (!previous || previous[key] !== row[key]) updated = updateMappingValue(updated, key, row[key]);
  }
  return updated;
}

function mappingScalar(block: string, key: string): string {
  return mappingField(block, key).split(/[\r\n#]/, 1)[0].trim();
}

export function parseCommandPermissions(text: string): CommandPermissionModel[] {
  return extractArrayGroups(text, "command_permissions").map(block => ({
    id: mappingString(block.text, "id"),
    path: mappingString(block.text, "path"),
    descendants: mappingScalar(block.text, "descendants") !== "false",
    sourceText: block.text
  }));
}

export function serializeCommandPermission(row: CommandPermissionModel): string {
  return [
    "[[command_permissions]]",
    `id = ${encodeToml(row.id.trim())}`,
    `path = ${encodeToml(row.path.trim())}`,
    `descendants = ${row.descendants}`
  ].join("\n");
}

export function replaceCommandPermissions(text: string, rows: string[]): string {
  return replaceArrayGroups(text, "command_permissions", rows);
}

export function parseInteractions(text: string): InteractionModel[] {
  return extractArrayGroups(text, "interactions").map(block => ({
    type: mappingString(block.text, "type") || "item_on_block",
    heldItem: mappingString(block.text, "held_item"),
    targetBlock: mappingString(block.text, "target_block"),
    targetEntity: mappingString(block.text, "target_entity"),
    targetKind: mappingString(block.text, "target_kind"),
    target: mappingString(block.text, "target"),
    effect: mappingString(block.text, "effect") || "lock",
    priority: Number(mappingScalar(block.text, "priority").replaceAll("_", "")) || 0,
    description: mappingString(block.text, "description"),
    sourceText: block.text
  }));
}

export function serializeInteraction(row: InteractionModel): string {
  const lines = ["[[interactions]]", `type = ${encodeToml(row.type)}`];
  if (row.heldItem.trim()) lines.push(`held_item = ${encodeToml(row.heldItem.trim())}`);
  if (row.type === "item_on_entity") lines.push(`target_entity = ${encodeToml(row.targetEntity.trim())}`);
  else if (row.type === "item_into_inventory") {
    lines.push(`target_kind = ${encodeToml(row.targetKind || "block")}`, `target = ${encodeToml(row.target.trim())}`,
      `effect = ${encodeToml(row.effect || "lock")}`, `priority = ${row.priority}`);
  } else lines.push(`target_block = ${encodeToml(row.targetBlock.trim())}`);
  if (row.description.trim()) lines.push(`description = ${encodeToml(row.description.trim())}`);
  return lines.join("\n");
}

export function serializeContainerInsertionPair(row: InteractionModel): string {
  if (row.type !== "item_on_block" || !row.heldItem.trim() || !row.targetBlock.trim()) {
    throw new Error("Choose an item selector and a block destination for the insertion pair.");
  }
  const inventory: InteractionModel = {
    ...row,
    type: "item_into_inventory",
    targetKind: "block",
    target: row.targetBlock,
    targetBlock: "",
    targetEntity: "",
    effect: "lock"
  };
  return `${serializeInteraction(row)}\n\n${serializeInteraction(inventory)}`;
}

export function replaceInteractions(text: string, rows: string[]): string {
  return replaceArrayGroups(text, "interactions", rows);
}

export function updateInteractionBlock(original: string, row: InteractionModel): string {
  const previous = parseInteractions(original)[0];
  let updated = original;
  const fields: [keyof InteractionModel, string][] = [
    ["type", "type"], ["heldItem", "held_item"], ["description", "description"]
  ];
  if (row.type === "item_into_inventory") {
    fields.push(["targetKind", "target_kind"], ["target", "target"], ["effect", "effect"], ["priority", "priority"]);
  } else fields.push(row.type === "item_on_entity" ? ["targetEntity", "target_entity"] : ["targetBlock", "target_block"]);
  for (const [property, key] of fields) {
    if (!previous || previous[property] !== row[property]) updated = updateMappingValue(updated, key, row[property]);
  }
  return updated;
}

export function appendRow(text: string, row: string): string {
  return appendTomlBlock(text, row);
}
