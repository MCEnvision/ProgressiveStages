import { encodeToml, extractArrayGroups, replaceArrayGroups, upsertTomlEncoded } from "./toml";
import { arrayValues, comments, findValue, inlineRowSource, inlineValues, isInlineRow, newline, replaceValue, requireEditable, samePath, scanToml } from "./tomlSource";

export function extractRows(text: string, table: string): { text: string }[] {
  const source = scanToml(text);
  try {
    const value = findValue(source, table.split("."));
    if (value) return arrayValues(text, value).map(entry => {
      if (text[entry.valueStart] !== "{") throw new Error("Every row must be a TOML table.");
      inlineValues(text, entry);
      return { text: text.slice(entry.valueStart, entry.valueEnd) };
    });
  } catch { return []; }
  return extractArrayGroups(text, table);
}

export function updateInlineRow(row: string, key: string, encoded: string): string {
  const { text, root, prefix, suffix } = inlineRowSource(row);
  const fields = inlineValues(text, root);
  const existing = fields.find(entry => samePath(entry.key, [key]));
  let updated: string;
  if (existing) updated = replaceValue(text, existing, encoded);
  else {
    const dotted = fields.filter(entry => entry.key.length > 1 && entry.key[0] === key);
    if (dotted.length) {
      const kept = fields.filter(entry => !dotted.includes(entry)).map(entry => text.slice(entry.start, entry.valueEnd));
      kept.push(`${encodeToml(key)} = ${encoded}`);
      const notes = dotted.flatMap(entry => comments(text.slice(entry.valueStart, entry.valueEnd)));
      const replacement = (notes.length ? notes.join(newline(row)) + newline(row) : "") + `{ ${kept.join(", ")} }`;
      return (text.slice(0, root.valueStart) + replacement + text.slice(root.valueEnd)).slice(prefix.length, -suffix.length);
    }
    const last = fields.at(-1);
    const position = last?.valueEnd ?? root.valueStart + 1;
    updated = text.slice(0, position) + `${last ? ", " : " "}${encodeToml(key)} = ${encoded}`
      + (text[position] === "}" ? " " : "") + text.slice(position);
  }
  return updated.slice(prefix.length, -suffix.length);
}

function inlineRow(row: string, table: string): string {
  if (isInlineRow(row)) { inlineRowSource(row); return row; }
  const source = scanToml(row);
  requireEditable(source);
  const path = table.split(".");
  if (!source.tables[0]?.array || !samePath(source.tables[0].path, path)
    || source.tables.slice(1).some(entry => entry.array || !samePath(entry.path.slice(0, path.length), path))) {
    throw new Error("The added row must contain one matching table and its field tables.");
  }
  type Fields = Map<string, string | Fields>;
  const fields: Fields = new Map();
  for (const entry of source.values) {
    if (!entry.table) throw new Error("A row field is outside its table.");
    const keys = [...entry.table.path.slice(path.length), ...entry.key];
    let parent = fields;
    for (const key of keys.slice(0, -1)) {
      if (!parent.has(key)) parent.set(key, new Map());
      const child = parent.get(key);
      if (!(child instanceof Map)) throw new Error("A row field conflicts with its parent table.");
      parent = child;
    }
    if (parent.has(keys.at(-1)!)) throw new Error("A row field is declared more than once.");
    parent.set(keys.at(-1)!, row.slice(entry.valueStart, entry.valueEnd));
  }
  const encode = (fields: Fields): string => `{ ${[...fields].map(([key, value]) => `${encodeToml(key)} = ${typeof value === "string" ? value : encode(value)}`).join(", ")} }`;
  return encode(fields);
}

export function replaceRows(text: string, table: string, replacements: string[]): string {
  const source = scanToml(text);
  requireEditable(source);
  const path = table.split(".");
  const value = findValue(source, path);
  if (!value) {
    const inlineParent = path.slice(0, -1).some((_, index) => findValue(source, path.slice(0, index + 1)));
    if (inlineParent) return upsertTomlEncoded(text, table, `[${replacements.map(row => inlineRow(row, table)).join(", ")}]`);
    return replaceArrayGroups(text, table, replacements);
  }
  const entries = arrayValues(text, value);
  for (const entry of entries) {
    if (text[entry.valueStart] !== "{") throw new Error("Every row must be a TOML table before editing this collection.");
    inlineValues(text, entry);
  }
  const edits: { start: number; end: number; text: string }[] = [];
  const converted = replacements.map(row => inlineRow(row, table));
  const remainingNotes = converted.flatMap(row => comments(row));
  const retainedNotes = entries.flatMap(entry => comments(text.slice(entry.valueStart, entry.valueEnd))).filter(note => {
    const match = remainingNotes.indexOf(note);
    if (match < 0) return true;
    remainingNotes.splice(match, 1);
    return false;
  });
  if (retainedNotes.length) edits.push({ start: value.valueStart + 1, end: value.valueStart + 1, text: newline(text) + retainedNotes.join(newline(text)) + newline(text) });
  for (let index = 0; index < entries.length; index++) {
    const entry = entries[index];
    const original = text.slice(entry.valueStart, entry.valueEnd);
    if (index < replacements.length) {
      const replacement = converted[index];
      if (original !== replacement) edits.push({ start: entry.valueStart, end: entry.valueEnd, text: replacement });
    } else {
      edits.push({ start: entry.valueStart, end: entry.valueEnd, text: "" });
      if (entry.separator != null) edits.push({ start: entry.separator, end: entry.separator + 1, text: "" });
    }
  }
  const added = converted.slice(entries.length);
  if (added.length) {
    const last = entries.at(-1);
    const needsComma = last && last.separator == null;
    const adjacent = needsComma && last.valueEnd === value.valueEnd - 1;
    if (needsComma && !adjacent) edits.push({ start: last.valueEnd, end: last.valueEnd, text: "," });
    edits.push({ start: value.valueEnd - 1, end: value.valueEnd - 1, text: (adjacent ? ", " : "") + added.join(", ") });
  }
  let result = text;
  for (const edit of edits.sort((left, right) => right.start - left.start || right.end - left.end)) {
    result = result.slice(0, edit.start) + edit.text + result.slice(edit.end);
  }
  return result;
}
