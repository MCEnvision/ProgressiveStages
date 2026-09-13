import { encodeToml } from "./toml";
import { comments, quoted, scanToml, samePath, newline } from "./tomlSource";
export { comments, quoted } from "./tomlSource";

export interface ContextRow { key: string; values: string[] }

export function contextMap(rows: ContextRow[]): Record<string, string[]> {
  const result: Record<string, string[]> = Object.create(null);
  for (const row of rows) {
    if (Object.hasOwn(result, row.key)) throw new Error(`Context key "${row.key}" appears more than once. Combine its values in one row.`);
    result[row.key] = [...row.values];
  }
  return result;
}

export function contextAssignments(contexts: Record<string, string[]>): string[] {
  return Object.entries(contexts).map(([key, values]) => `${encodeToml(key)} = ${encodeToml(values)}`);
}


function skip(text: string, offset: number): number {
  let index = offset;
  while (index < text.length) {
    if (/\s/.test(text[index])) { index++; continue; }
    if (text[index] === "#") { while (index < text.length && text[index] !== "\n") index++; continue; }
    break;
  }
  return index;
}

export function stringArray(text: string, offset: number): { values: string[]; end: number } {
  let index = skip(text, offset);
  if (text[index++] !== "[") throw new Error("Context values must be an array of strings.");
  const values: string[] = [];
  index = skip(text, index);
  while (text[index] !== "]") {
    const token = quoted(text, index);
    values.push(token.value);
    index = skip(text, token.end);
    if (text[index] === "]") break;
    if (text[index++] !== ",") throw new Error("Separate context values with commas.");
    index = skip(text, index);
  }
  if (text[index] !== "]") throw new Error("The context array is not closed.");
  return { values, end: index + 1 };
}

interface Assignment { key: string; values: string[]; start: number; valueStart: number; end: number }
interface ContextSource { values: Record<string, string[]>; assignments: Assignment[]; start: number; end: number; inlineStart?: number; error?: string }

export function readContexts(block: string, section: string): ContextSource {
  const values: Record<string, string[]> = Object.create(null);
  const assignments: Assignment[] = [];
  const source = scanToml(block);
  const header = source.tables.find(table => !table.array && samePath(table.path, section.split(".")));
  if (!header) {
    const inline = source.values.find(value => value.table === source.tables[0] && samePath(value.key, ["contexts"]));
    if (!inline) return { values, assignments, start: -1, end: block.length, error: source.error };
    let index = inline.valueStart;
    const inlineStart = index;
    try {
      if (block[index++] !== "{") throw new Error("Contexts must be a table of string arrays.");
      index = skip(block, index);
      while (block[index] !== "}") {
        let key: string;
        if (block[index] === '"' || block[index] === "'") {
          const token = quoted(block, index); key = token.value; index = token.end;
        } else {
          const token = /^[A-Za-z0-9_-]+/.exec(block.slice(index));
          if (!token) throw new Error("A context key could not be read.");
          key = token[0]; index += key.length;
        }
        index = skip(block, index);
        if (block[index++] !== "=") throw new Error("Quote a context key containing dots or punctuation.");
        const parsed = stringArray(block, index);
        if (Object.hasOwn(values, key)) throw new Error(`Context key "${key}" appears more than once.`);
        values[key] = parsed.values;
        index = skip(block, parsed.end);
        if (block[index] === "}") break;
        if (block[index++] !== ",") throw new Error("Separate inline contexts with commas.");
        index = skip(block, index);
      }
      return { values, assignments, start: inlineStart, end: index + 1, inlineStart };
    } catch (failure) {
      return { values, assignments, start: inlineStart, end: index, inlineStart,
        error: failure instanceof Error ? failure.message : "Contexts could not be read." };
    }
  }
  let index = header.end;
  const start = index;
  try {
    while ((index = skip(block, index)) < block.length && block[index] !== "[") {
      const assignmentStart = index;
      let key: string;
      if (block[index] === '"' || block[index] === "'") {
        const token = quoted(block, index); key = token.value; index = token.end;
      } else {
        const token = /^[A-Za-z0-9_-]+/.exec(block.slice(index));
        if (!token) throw new Error("A context key could not be read.");
        key = token[0]; index += key.length;
      }
      while (/[ \t]/.test(block[index] || "x")) index++;
      if (block[index++] !== "=") throw new Error("Quote a context key containing dots or punctuation.");
      while (/[ \t]/.test(block[index] || "x")) index++;
      const valueStart = index;
      const parsed = stringArray(block, index);
      if (Object.hasOwn(values, key)) throw new Error(`Context key "${key}" appears more than once.`);
      values[key] = parsed.values;
      assignments.push({ key, values: parsed.values, start: assignmentStart, valueStart, end: parsed.end });
      index = parsed.end;
      while (/[ \t]/.test(block[index] || "x")) index++;
      if (index < block.length && !/[#\r\n]/.test(block[index])) throw new Error("Unexpected text after context values.");
    }
    return { values, assignments, start, end: index };
  } catch (failure) {
    return { values, assignments, start, end: index, error: failure instanceof Error ? failure.message : "Contexts could not be read." };
  }
}


export function replaceContexts(block: string, section: string, contexts: Record<string, string[]>): string {
  const source = readContexts(block, section);
  if (JSON.stringify(source.values) === JSON.stringify(contexts)) return block;
  if (source.error) throw new Error(`${source.error} Correct the existing context source before changing its values.`);
  if (source.inlineStart != null) {
    const notes = comments(block.slice(source.inlineStart, source.end));
    const lineStart = block.lastIndexOf("\n", source.inlineStart) + 1;
    const retained = notes.length ? notes.join(newline(block)) + newline(block) : "";
    return block.slice(0, lineStart) + retained + block.slice(lineStart, source.inlineStart)
      + `{ ${contextAssignments(contexts).join(", ")} }` + block.slice(source.end);
  }
  if (source.start < 0) {
    const lines = contextAssignments(contexts);
    return lines.length ? `${block}${block.endsWith("\n") ? "" : newline(block)}${newline(block)}[${section}]${newline(block)}${lines.join(newline(block))}` : block;
  }
  let result = block;
  const keys = new Set(source.assignments.map(assignment => assignment.key));
  const added = contextAssignments(Object.fromEntries(Object.entries(contexts).filter(([key]) => !keys.has(key))));
  if (added.length) result = result.slice(0, source.end) + `${source.end > 0 && result[source.end - 1] !== "\n" ? newline(block) : ""}${added.join(newline(block))}${newline(block)}` + result.slice(source.end);
  for (const assignment of [...source.assignments].reverse()) {
    if (Object.hasOwn(contexts, assignment.key) && JSON.stringify(contexts[assignment.key]) === JSON.stringify(assignment.values)) continue;
    const notes = comments(block.slice(assignment.valueStart, assignment.end));
    const retained = notes.length ? notes.join(newline(block)) + newline(block) : "";
    const replacement = Object.hasOwn(contexts, assignment.key)
      ? retained + block.slice(assignment.start, assignment.valueStart) + encodeToml(contexts[assignment.key]) : retained;
    result = result.slice(0, assignment.start) + replacement + result.slice(assignment.end);
  }
  return result;
}
