import { comments, contextAssignments, quoted, readContexts, replaceContexts, stringArray } from "./contexts";
import type { InteractionModel, InboundModel, OutboundModel, CommandPermissionModel } from "../types";
import {
  appendTomlBlock,
  encodeToml,
  escapeRegex,
  extractArrayBlocks,
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
  if (choice === "inherit") {
    return removeTomlValue(removeTomlValue(text, "stage.scope"), "stage.team_stage");
  }
  let updated = upsertToml(text, "stage.scope", choice === "server" ? "server" : "team");
  return choice === "server"
    ? removeTomlValue(updated, "stage.team_stage")
    : upsertToml(updated, "stage.team_stage", choice === "personal" ? false : true);
}


function mappingField(block: string, key: string): string {
  const body = block.slice(block.indexOf("\n") + 1);
  const child = /^[ \t]*\[[^\n]+\]/m.exec(body);
  const root = child ? body.slice(0, child.index) : body;
  const name = escapeRegex(key);
  const assignment = new RegExp(`^[ \\t]*(?:${name}|"${name}"|'${name}')[ \\t]*=[ \\t]*`, "m").exec(root);
  return assignment ? root.slice(assignment.index + assignment[0].length) : "";
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
  const sectionPresent = /^\s*\[luckperms\]\s*$/m.test(text);
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
  let updated = upsertToml(text, "luckperms.enabled", enabled);
  return upsertToml(updated, "luckperms.inbound_mode", inboundMode);
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
  const bodyStart = original.indexOf("\n") + 1;
  const body = original.slice(bodyStart);
  const child = /^[ \t]*\[[^\n]+\]/m.exec(body);
  const root = child ? body.slice(0, child.index) : body;
  const name = escapeRegex(key);
  const assignment = new RegExp(`^[ \\t]*(?:${name}|"${name}"|'${name}')[ \\t]*=[ \\t]*`, "m").exec(root);
  if (!assignment) return original.slice(0, bodyStart) + `${key} = ${encodeToml(value)}\n` + body;
  const start = bodyStart + assignment.index + assignment[0].length;
  let end: number;
  if (original[start] === "[") end = stringArray(original, start).end;
  else if (original[start] === '"' || original[start] === "'") end = quoted(original, start).end;
  else {
    const remaining = original.slice(start);
    const boundary = remaining.search(/[\r\n#]/);
    end = start + (boundary < 0 ? remaining.length : boundary);
  }
  const notes = comments(original.slice(start, end));
  const lineStart = bodyStart + assignment.index;
  return original.slice(0, lineStart) + (notes.length ? notes.join("\n") + "\n" : "")
    + original.slice(lineStart, start) + encodeToml(value) + original.slice(end);
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
  let updated = upsertToml(original, "id", row.id.trim());
  updated = upsertToml(updated, "path", row.path.trim());
  return upsertToml(updated, "descendants", row.descendants);
}

export function parseCommandPermissions(text: string): CommandPermissionModel[] {
  return extractArrayBlocks(text, "command_permissions").map(block => ({
    id: stringValue(readBlockValue(block.text, "id")),
    path: stringValue(readBlockValue(block.text, "path")),
    descendants: readBlockValue(block.text, "descendants").trim().toLowerCase() === "true",
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
  const blocks = extractArrayBlocks(text, "command_permissions");
  const lines = text.split(/\r?\n/);
  for (let index = blocks.length - 1; index >= 0; index--) {
    const block = blocks[index];
    const replacement = rows[index];
    lines.splice(block.start, block.end - block.start, ...(replacement ? replacement.split("\n") : []));
  }
  for (const replacement of rows.slice(blocks.length)) {
    if (lines.at(-1)?.trim()) lines.push("");
    lines.push(...replacement.split("\n"));
  }
  return lines.join("\n").replace(/\n{3,}/g, "\n\n").trimEnd() + "\n";
}

export function parseInteractions(text: string): InteractionModel[] {
  return extractArrayGroups(text, "interactions").map(block => ({
    type: stringValue(readBlockValue(block.text, "type")) || "item_on_block",
    heldItem: stringValue(readBlockValue(block.text, "held_item")),
    targetBlock: stringValue(readBlockValue(block.text, "target_block")),
    targetEntity: stringValue(readBlockValue(block.text, "target_entity")),
    targetKind: stringValue(readBlockValue(block.text, "target_kind")),
    target: stringValue(readBlockValue(block.text, "target")),
    effect: stringValue(readBlockValue(block.text, "effect")) || "lock",
    priority: Number(readBlockValue(block.text, "priority")) || 0,
    description: stringValue(readBlockValue(block.text, "description")),
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
  return serializeInteraction(row).split(/\r?\n/).reduce((result, line) => {
    const match = line.match(/^([A-Za-z0-9_.-]+)\s*=\s*(.*)$/);
    if (!match || match[1] === "type") return result;
    const raw = match[2].trim();
    const value = raw === "true" || raw === "false" ? raw === "true"
      : /^-?\d+$/.test(raw) ? Number(raw) : stringValue(raw);
    return upsertToml(result, match[1], value);
  }, upsertToml(original, "type", row.type));
}

export function appendRow(text: string, row: string): string {
  return appendTomlBlock(text, row);
}
