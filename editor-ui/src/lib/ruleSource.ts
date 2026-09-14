import type { RuleModel } from "../types";
import { readInventoryCondition, serializeInventoryCondition, writeCondition } from "./inventoryInsertion";
import { encodeToml, extractArrayGroups, parseSimpleArray, readTomlValue, removeTomlValue, replaceArrayGroups, stringValue, upsertTomlEncoded } from "./toml";
import { requireEditable, scanToml } from "./tomlSource";

function ruleDocument(row: string) {
  const source = scanToml(row);
  requireEditable(source);
  const root = source.tables[0];
  if (!root?.array) throw new Error("Expected one rule table.");
  const header = row.slice(root.start, root.end);
  const text = row.slice(0, root.start) + header.replace("[[", "[").replace("]]", "]") + row.slice(root.end);
  return { text, path: root.path.join("."), restore: (updated: string) => {
    const edited = scanToml(updated).tables[0];
    return updated.slice(0, edited.start) + header + updated.slice(edited.end);
  } };
}

export function readRuleField(row: string, key: string): string {
  const document = ruleDocument(row);
  return readTomlValue(document.text, `${document.path}.${key}`);
}

function writeRuleField(row: string, key: string, encoded: string): string {
  const document = ruleDocument(row);
  return document.restore(upsertTomlEncoded(document.text, `${document.path}.${key}`, encoded));
}

function removeRuleField(row: string, key: string): string {
  const document = ruleDocument(row);
  return document.restore(removeTomlValue(document.text, `${document.path}.${key}`));
}

type RuleEdit = Pick<RuleModel, "stageState" | "category" | "action" | "effect" | "selector" | "priority" | "viewer" | "lifetime" | "duration" | "conditionType" | "conditionTarget" | "count" | "exception" | "exceptionPriority" | "resetConditionType" | "resetConditionTarget" | "resetCount">;

export function updateGenericRule(row: string, draft: RuleEdit, previous: RuleModel): string {
  let updated = row;
  if (draft.stageState !== previous.stageState) updated = writeRuleField(updated, "stage_state", encodeToml(draft.stageState));
  for (const key of ["action", "effect", "priority", "lifetime", "duration"] as const) {
    if (draft[key] !== previous[key]) updated = writeRuleField(updated, key, encodeToml(draft[key]));
  }
  if (draft.category !== previous.category || draft.selector !== previous.selector) {
    const key = `targets.${previous.category}`;
    const raw = readRuleField(row, key);
    const selectors = raw.trim().startsWith("[") ? parseSimpleArray(raw) : raw ? [stringValue(raw)] : [];
    if (draft.category !== previous.category && (extractArrayGroups(row, `${previous.table}.exceptions`).length
      || readRuleField(row, "targets")
      || scanToml(ruleDocument(row).text).values.filter(value => [...(value.table?.path || []), ...value.key][1] === "targets").length > 1)) {
      throw new Error("This rule has dependent targets or exceptions. Edit its category in Source to keep their meaning explicit.");
    }
    if (selectors.length) {
      if (draft.category !== previous.category) {
        if (selectors.length > 1) throw new Error("This rule has multiple targets. Edit its category in Source to keep their meaning explicit.");
        updated = removeRuleField(updated, key);
      }
      selectors[0] = draft.selector;
      updated = writeRuleField(updated, `targets.${draft.category}`, encodeToml(raw.trim().startsWith("[") ? selectors : selectors[0]));
    } else {
      updated = writeRuleField(updated, "category", encodeToml(draft.category));
      updated = writeRuleField(updated, "selector", encodeToml(draft.selector));
    }
  }
  if (draft.viewer !== previous.viewer) {
    for (const key of ["presentation.jei", "presentation.emi"]) {
      updated = draft.viewer === "inherit" ? removeRuleField(updated, key) : writeRuleField(updated, key, encodeToml(draft.viewer));
    }
  }
  for (const reset of [false, true]) {
    const type = reset ? draft.resetConditionType || "none" : draft.conditionType;
    const target = reset ? draft.resetConditionTarget || "" : draft.conditionTarget;
    const count = reset ? draft.resetCount || 1 : draft.count;
    if (type === (reset ? previous.resetConditionType || "none" : previous.conditionType)
      && target === (reset ? previous.resetConditionTarget || "" : previous.conditionTarget)
      && count === (reset ? previous.resetCount || 1 : previous.count)) continue;
    const key = reset ? "reset_condition" : ["while", "when", "condition"].find(key => readInventoryCondition(row, key)) || "while";
    const source = readInventoryCondition(row, key);
    updated = writeCondition(updated, key, serializeInventoryCondition(source, type, target, count), source);
  }
  if (draft.exception !== previous.exception || draft.exceptionPriority !== previous.exceptionPriority) {
    const table = `${previous.table}.exceptions`;
    const groups = extractArrayGroups(updated, table).map(group => group.text);
    if (!draft.exception) groups.shift();
    else if (groups.length) {
      const selectors = parseSimpleArray(readRuleField(groups[0], `targets.${previous.category}`));
      selectors[0] = draft.exception;
      groups[0] = writeRuleField(groups[0], `targets.${draft.category}`, encodeToml(selectors));
      groups[0] = writeRuleField(groups[0], "priority", encodeToml(draft.exceptionPriority));
    } else groups.push(`[[${table}]]\neffect = "exclude"\npriority = ${draft.exceptionPriority}\ntargets.${draft.category} = ${encodeToml([draft.exception])}\n`);
    updated = groups.length ? replaceArrayGroups(updated, table, groups) : replaceArrayGroups(updated, table, []);
  }
  return updated;
}

export function moveRuleTable(row: string, table: "rules" | "temporary_rules", previous: RuleModel): string {
  if (table === previous.table) return row;
  let updated = readRuleField(row, "stage_state") ? row : writeRuleField(row, "stage_state", encodeToml(
    previous.table === "temporary_rules" || !["lock", "exclude"].includes(previous.effect) ? "owned" : "missing"));
  for (const entry of scanToml(updated).tables.reverse()) {
    if (entry.path[0] !== previous.table) continue;
    const header = updated.slice(entry.start, entry.end).replace(previous.table, table);
    updated = updated.slice(0, entry.start) + header + updated.slice(entry.end);
  }
  return updated;
}
