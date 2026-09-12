import type { InteractionModel, InboundModel, OutboundModel, CommandPermissionModel } from "../types";
import {
  appendTomlBlock,
  encodeToml,
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

function sectionValues(block: string, section: string): Record<string, string[]> {
  const result: Record<string, string[]> = {};
  const lines = block.split(/\r?\n/);
  let active = "";
  for (const line of lines) {
    const header = line.match(/^\s*\[([^\]]+)\]/);
    if (header) {
      active = header[1];
      continue;
    }
    if (active !== section) continue;
    const assignment = line.match(/^\s*([A-Za-z0-9_.-]+)\s*=\s*(.+?)\s*(?:#.*)?$/);
    if (!assignment) continue;
    if (/^[A-Za-z0-9_.:-]{1,256}$/.test(assignment[1])) result[assignment[1]] = parseSimpleArray(assignment[2]);
  }
  return result;
}

export function parseLuckPerms(text: string): LuckPermsView {
  const sectionRaw = readTomlValue(text, "luckperms.enabled");
  const sectionPresent = /^\s*\[luckperms\]\s*$/m.test(text);
  const inbound = extractArrayGroups(text, "luckperms.inbound").map((block): InboundModel => ({
    id: stringValue(readBlockValue(block.text, "id")),
    groups: parseSimpleArray(readBlockValue(block.text, "groups")),
    permissions: parseSimpleArray(readBlockValue(block.text, "permissions")),
    match: stringValue(readBlockValue(block.text, "match")) === "any" ? "any" : "all",
    contexts: sectionValues(block.text, "luckperms.inbound.contexts"),
    sourceText: block.text
  }));
  const outbound = extractArrayGroups(text, "luckperms.outbound").map((block): OutboundModel => ({
    id: stringValue(readBlockValue(block.text, "id")),
    kind: stringValue(readBlockValue(block.text, "kind")) === "group" ? "group" : "permission",
    value: stringValue(readBlockValue(block.text, "value")),
    contexts: sectionValues(block.text, "luckperms.outbound.contexts"),
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
  const entries = Object.entries(contexts).filter(([key, values]) => /^[A-Za-z0-9_.:-]{1,256}$/.test(key.trim()) && values.length);
  return entries.length ? `\n[${section}]\n${entries.map(([key, values]) => `${key.trim()} = ${encodeToml(values.map(value => value.trim()).filter(Boolean))}`).join("\n")}` : "";
}

export function replaceInbound(text: string, rows: string[]): string {
  return replaceArrayGroups(text, "luckperms.inbound", rows);
}

export function replaceOutbound(text: string, rows: string[]): string {
  return replaceArrayGroups(text, "luckperms.outbound", rows);
}

function replaceContextAssignments(block: string, section: string, contexts: Record<string, string[]>): string {
  const oldKeys = new Set(Object.keys(sectionValues(block, section)));
  const lines = block.split(/\r?\n/);
  let start = -1;
  let end = lines.length;
  for (let index = 0; index < lines.length; index++) {
    const header = lines[index].match(/^\s*\[([^\]]+)\]/);
    if (!header) continue;
    if (start >= 0) { end = index; break; }
    if (header[1] === section) start = index;
  }
  const assignments = Object.entries(contexts).filter(([key, values]) => /^[A-Za-z0-9_.:-]{1,256}$/.test(key.trim()) && values.length)
    .map(([key, values]) => `${key.trim()} = ${encodeToml(values.map(value => value.trim()).filter(Boolean))}`);
  if (start < 0) return assignments.length ? `${block.trimEnd()}\n\n[${section}]\n${assignments.join("\n")}` : block;
  const retained = lines.slice(start + 1, end).filter(line => {
    const match = line.match(/^\s*([A-Za-z0-9_.-]+)\s*=/);
    return !match || !oldKeys.has(match[1]);
  });
  const prefix = lines.slice(0, start + 1);
  const suffix = lines.slice(end);
  return [...prefix, ...retained, ...assignments, ...suffix].join("\n").replace(/\n{3,}/g, "\n\n");
}

export function updateInboundBlock(original: string, row: InboundModel): string {
  let updated = upsertToml(original, "id", row.id.trim());
  updated = upsertToml(updated, "groups", row.groups);
  updated = upsertToml(updated, "permissions", row.permissions);
  updated = upsertToml(updated, "match", row.match);
  return replaceContextAssignments(updated, "luckperms.inbound.contexts", row.contexts);
}

export function updateOutboundBlock(original: string, row: OutboundModel): string {
  let updated = upsertToml(original, "id", row.id.trim());
  updated = upsertToml(updated, "kind", row.kind);
  updated = upsertToml(updated, "value", row.value.trim());
  return replaceContextAssignments(updated, "luckperms.outbound.contexts", row.contexts);
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
