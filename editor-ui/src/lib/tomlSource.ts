interface StringToken { value: string; end: number }
export function quoted(text: string, start: number): StringToken {
  const quote = text[start];
  if (quote !== '"' && quote !== "'") throw new Error("A TOML string value must be a quoted string.");
  const multiline = text.startsWith(quote.repeat(3), start);
  let index = start + (multiline ? 3 : 1);
  let value = "";
  if (multiline && text[index] === "\r" && text[index + 1] === "\n") index += 2;
  else if (multiline && text[index] === "\n") index++;
  while (index < text.length) {
    if (text[index] === quote) {
      if (!multiline) return { value, end: index + 1 };
      let count = 1;
      while (text[index + count] === quote) count++;
      if (count >= 3) {
        if (count > 5) throw new Error("Invalid TOML string delimiter.");
        return { value: value + quote.repeat(count - 3), end: index + count };
      }
      value += quote.repeat(count); index += count; continue;
    }
    const character = text[index++];
    if (!multiline && (character === "\n" || character === "\r")) throw new Error("A single line TOML string contains a newline.");
    if (multiline && character === "\r" && text[index] === "\n") { value += "\n"; index++; continue; }
    if (character !== "\\" || quote === "'") { value += character; continue; }
    const rest = text.slice(index);
    if (multiline && /^[ \t]*\r?\n/.test(rest)) {
      index += rest.match(/^[ \t\r\n]+/)![0].length;
      continue;
    }
    const escape = text[index++];
    const escapes: Record<string, string> = { b: "\b", t: "\t", n: "\n", f: "\f", r: "\r", '"': '"', "\\": "\\" };
    if (Object.hasOwn(escapes, escape)) { value += escapes[escape]; continue; }
    if (escape === "u" || escape === "U") {
      const length = escape === "u" ? 4 : 8;
      const digits = text.slice(index, index + length);
      const code = Number.parseInt(digits, 16);
      if (digits.length !== length || !/^[0-9a-f]+$/i.test(digits) || code > 0x10ffff || (code >= 0xd800 && code <= 0xdfff)) throw new Error("Invalid Unicode escape in TOML string.");
      value += String.fromCodePoint(code); index += length; continue;
    }
    throw new Error("Invalid escape in TOML string.");
  }
  throw new Error("A TOML string is not closed.");
}

export function comments(text: string): string[] {
  const result: string[] = [];
  for (let index = 0; index < text.length;) {
    if (text[index] === '"' || text[index] === "'") { index = quoted(text, index).end; continue; }
    if (text[index] === "#") {
      const end = text.indexOf("\n", index);
      result.push(text.slice(index, end < 0 ? text.length : end).replace(/\r$/, ""));
      index = end < 0 ? text.length : end;
    } else index++;
  }
  return result;
}

export interface TableSpan { path: string[]; array: boolean; start: number; end: number }
export interface ValueSpan { key: string[]; table?: TableSpan; start: number; valueStart: number; valueEnd: number; end: number }
export interface TomlSource { tables: TableSpan[]; values: ValueSpan[]; error?: string }

export function samePath(left: string[], right: string[]): boolean {
  return left.length === right.length && left.every((part, index) => part === right[index]);
}

export function newline(text: string): string { return text.includes("\r\n") ? "\r\n" : "\n"; }

function horizontal(text: string, start: number): number {
  let index = start;
  while (text[index] === " " || text[index] === "\t") index++;
  return index;
}

function keyPath(text: string, start: number): { path: string[]; end: number } {
  const path: string[] = [];
  let index = horizontal(text, start);
  while (index < text.length) {
    if (text[index] === '"' || text[index] === "'") {
      if (text.startsWith(text[index].repeat(3), index)) throw new Error("A TOML key cannot use a multiline string.");
      const token = quoted(text, index);
      path.push(token.value); index = token.end;
    } else {
      const token = /^[A-Za-z0-9_-]+/.exec(text.slice(index));
      if (!token) throw new Error("A TOML key could not be read.");
      path.push(token[0]); index += token[0].length;
    }
    index = horizontal(text, index);
    if (text[index] !== ".") return { path, end: index };
    index = horizontal(text, index + 1);
  }
  throw new Error("A TOML key is incomplete.");
}

function lineEnd(text: string, start: number): number {
  const end = text.indexOf("\n", start);
  return end < 0 ? text.length : end + 1;
}

function valueEnd(text: string, start: number): number {
  const closers: string[] = [];
  let index = start;
  while (index < text.length) {
    const character = text[index];
    if (character === '"' || character === "'") { index = quoted(text, index).end; continue; }
    if (character === "#") {
      if (!closers.length) break;
      index = lineEnd(text, index); continue;
    }
    if ((character === "\n" || character === "\r") && !closers.length) break;
    if (character === "[" || character === "{") closers.push(character === "[" ? "]" : "}");
    else if (character === "]" || character === "}") {
      if (closers.pop() !== character) throw new Error("A TOML value has an unmatched delimiter.");
    }
    index++;
  }
  if (closers.length) throw new Error("A TOML value is not closed.");
  return start + text.slice(start, index).trimEnd().length;
}

export function scanToml(text: string): TomlSource {
  const source: TomlSource = { tables: [], values: [] };
  let index = 0;
  let table: TableSpan | undefined;
  try {
    while (index < text.length) {
      const start = index;
      index = horizontal(text, index);
      if (text[index] === "#" || text[index] === "\r" || text[index] === "\n") { index = lineEnd(text, index); continue; }
      if (index === text.length) break;
      if (text[index] === "[") {
        const array = text[index + 1] === "[";
        const key = keyPath(text, index + (array ? 2 : 1));
        const close = array ? "]]" : "]";
        if (!text.startsWith(close, key.end)) throw new Error("A TOML table header is not closed.");
        const end = horizontal(text, key.end + close.length);
        if (end < text.length && !/[#\r\n]/.test(text[end])) throw new Error("Unexpected text after a TOML table header.");
        table = { path: key.path, array, start, end: lineEnd(text, end) };
        source.tables.push(table); index = table.end;
      } else {
        const key = keyPath(text, index);
        if (text[key.end] !== "=") throw new Error("A TOML assignment is missing its equals sign.");
        const valueStart = horizontal(text, key.end + 1);
        const end = valueEnd(text, valueStart);
        source.values.push({ key: key.path, table, start, valueStart, valueEnd: end, end: lineEnd(text, end) });
        index = lineEnd(text, end);
      }
    }
  } catch (failure) {
    source.error = failure instanceof Error ? failure.message : "The TOML source could not be read.";
  }
  return source;
}

export function requireEditable(source: TomlSource): void {
  if (source.error) throw new Error(`${source.error} Correct the source before editing its fields.`);
}

export function findValue(source: TomlSource, path: string[]): ValueSpan | undefined {
  return source.values.find(value => !value.table?.array && samePath([...(value.table?.path || []), ...value.key], path));
}

export function replaceValue(text: string, span: ValueSpan, encoded: string): string {
  const notes = comments(text.slice(span.valueStart, span.valueEnd));
  const retained = notes.length ? notes.join(newline(text)) + newline(text) : "";
  return text.slice(0, span.start) + retained + text.slice(span.start, span.valueStart) + encoded + text.slice(span.valueEnd);
}
