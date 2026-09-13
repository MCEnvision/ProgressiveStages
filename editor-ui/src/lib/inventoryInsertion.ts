import { conditionToml, encodeToml, inlineObjectValue, readBlockValue, upsertTomlEncoded } from "./toml";
import { comments, inlineRowSource, inlineValues, isInlineRow, newline, replaceValue, requireEditable, samePath, scanToml } from "./tomlSource";
import { updateInlineRow } from "./tomlRows";

export interface InventoryInsertionRuleDraft {
  id?: string;
  selector: string;
  targetKind: "block" | "menu" | "inventory";
  destination: string;
  effect: "lock" | "deny" | "allow" | "unlock" | "exclude";
  priority: number;
  lifetime?: string;
  duration?: string;
  condition?: string;
  resetCondition?: string;
}

export function serializeInventoryCondition(source: string, type: string, target: string, count: number): string {
  if (type === "none") return "";
  const sourceType = inlineObjectValue(source, "type");
  const sourceTarget = inlineObjectValue(source, "id") || inlineObjectValue(source, "value") || inlineObjectValue(source, "callback");
  const sourceCount = Number(inlineObjectValue(source, "count") || "1");
  if (source && sourceType === type && sourceTarget === target && sourceCount === count) return source;
  if (!source) return conditionToml(type, target, count);
  let updated = source;
  if (sourceType !== type) updated = updateInlineRow(updated, "type", encodeToml(type));
  if (sourceTarget !== target) {
    const key = ["id", "value", "callback"].find(key => readBlockValue(source, key)) || "id";
    updated = updateInlineRow(updated, key, encodeToml(target));
  }
  if (sourceCount !== count) updated = updateInlineRow(updated, "count", encodeToml(count));
  return updated;
}

export function readInventoryCondition(row: string, key: string): string {
  const inline = readBlockValue(row, key);
  if (inline) return inline;
  const source = scanToml(row);
  const path = [...(source.tables[0]?.path || []), key];
  const table = source.tables.find(table => !table.array && samePath(table.path, path));
  if (!table) return "";
  return `{ ${source.values.filter(entry => entry.table === table).map(entry => row.slice(entry.start, entry.valueEnd).trim()).join(", ")} }`;
}

function writeField(row: string, key: string, encoded: string): string {
  if (isInlineRow(row)) return updateInlineRow(row, key, encoded);
  const source = scanToml(row);
  requireEditable(source);
  const table = source.tables[0];
  const existing = source.values.find(entry => entry.table === table && samePath(entry.key, [key]));
  if (existing) return replaceValue(row, existing, encoded);
  const offset = table?.end || 0;
  return row.slice(0, offset) + (offset && row[offset - 1] !== "\n" ? newline(row) : "") + `${key} = ${encoded}${newline(row)}` + row.slice(offset);
}

function removeCondition(row: string, key: string): string {
  if (isInlineRow(row)) {
    const { text, root, prefix, suffix } = inlineRowSource(row);
    const fields = inlineValues(text, root);
    const index = fields.findIndex(entry => samePath(entry.key, [key]));
    if (index < 0) return row;
    const field = fields[index];
    const start = index ? fields[index - 1].valueEnd : field.start;
    const end = index === 0 && fields.length > 1 ? fields[1].start : field.valueEnd;
    const notes = comments(text.slice(field.valueStart, field.valueEnd));
    const updated = text.slice(0, start) + text.slice(end);
    return (notes.length ? notes.join(newline(row)) + newline(row) : "") + updated.slice(prefix.length, -suffix.length);
  }
  const source = scanToml(row);
  requireEditable(source);
  const root = source.tables[0];
  const field = source.values.find(entry => entry.table === root && samePath(entry.key, [key]));
  const path = [...(root?.path || []), key];
  const table = source.tables.find(entry => !entry.array && samePath(entry.path, path));
  const start = field?.start ?? table?.start;
  if (start == null) return row;
  const end = field?.end ?? source.tables.find(entry => entry.start > start && !samePath(entry.path.slice(0, path.length), path))?.start ?? row.length;
  const notes = comments(row.slice(start, end));
  return row.slice(0, start) + (notes.length ? notes.join(newline(row)) + newline(row) : "") + row.slice(end);
}

function writeCondition(row: string, key: string, condition: string, previous: string): string {
  if (!condition) return removeCondition(row, key);
  const source = scanToml(row);
  const path = [...(source.tables[0]?.path || []), key];
  const childTable = source.tables.some(entry => !entry.array && samePath(entry.path, path));
  if (!childTable) return writeField(row, key, condition);
  const { text, root } = inlineRowSource(condition);
  let result = row;
  for (const field of inlineValues(text, root)) {
    if (field.key.length !== 1) continue;
    const value = text.slice(field.valueStart, field.valueEnd);
    if (value !== readBlockValue(previous, field.key[0])) result = upsertTomlEncoded(result, [...path, ...field.key].join("."), value);
  }
  return result;
}

export function updateInventoryInsertionRule(row: string, draft: InventoryInsertionRuleDraft, previous: InventoryInsertionRuleDraft): string {
  let result = row;
  for (const [property, key] of [["id", "id"], ["selector", "held_item"], ["targetKind", "target_kind"], ["destination", "target"], ["effect", "effect"], ["priority", "priority"], ["lifetime", "lifetime"], ["duration", "duration"]] as const) {
    if (draft[property] !== previous[property]) result = writeField(result, key, encodeToml(draft[property] ?? ""));
  }
  if (draft.condition !== previous.condition) {
    const key = ["while", "when", "condition"].find(key => readInventoryCondition(row, key)) || "while";
    result = writeCondition(result, key, draft.condition || "", previous.condition || "");
  }
  if (draft.resetCondition !== previous.resetCondition) result = writeCondition(result, "reset_condition", draft.resetCondition || "", previous.resetCondition || "");
  return result;
}

export function serializeInventoryInsertionRule(draft: InventoryInsertionRuleDraft): string {
  const lines = [
    "[[interactions]]",
    ...(draft.id?.trim() ? [`id = ${encodeToml(draft.id.trim())}`] : []),
    'type = "item_into_inventory"',
    `held_item = ${encodeToml(draft.selector)}`,
    `target_kind = ${encodeToml(draft.targetKind)}`,
    `target = ${encodeToml(draft.destination)}`,
    `effect = ${encodeToml(draft.effect)}`,
    `priority = ${draft.priority}`
  ];
  if (draft.lifetime && draft.lifetime !== "permanent") lines.push(`lifetime = ${encodeToml(draft.lifetime)}`);
  if (draft.duration?.trim()) lines.push(`duration = ${encodeToml(draft.duration.trim())}`);
  if (draft.condition?.trim()) lines.push(`while = ${draft.condition}`);
  if (draft.resetCondition?.trim()) lines.push(`reset_condition = ${draft.resetCondition}`);
  return lines.join("\n");
}
