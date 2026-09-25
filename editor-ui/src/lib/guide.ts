export type GuidePlaceholder = { token: string; label: string; example: string; help: string };

export const GUIDE_CODE_POINT_LIMIT = 2048;
export const GUIDE_BYTE_LIMIT = 8 * 1024;

export const GUIDE_PLACEHOLDERS: GuidePlaceholder[] = [
  { token: "{stage_name}", label: "Stage name", example: "Miner", help: "The name players see for this stage." },
  { token: "{stage_requirements}", label: "Stage requirements", example: "All of Beginner Mining", help: "The visible prerequisite stages and their rule." },
  { token: "{remaining_requirements}", label: "Remaining requirements", example: "Mine 10 stone", help: "Current unmet trigger or purchase requirements when available." },
  { token: "{stage_progress}", label: "Stage progress", example: "50%", help: "Current trigger progress, or Unavailable when no progress is known." }
];

export const GUIDE_PLACEHOLDER_VALUES = {
  stage_name: "Miner",
  stage_requirements: "All of Beginner Mining",
  remaining_requirements: "Mine 10 stone",
  stage_progress: "50%"
};

export function guideTextMetrics(value: string) {
  const codePoints = Array.from(value).length;
  const bytes = new TextEncoder().encode(value).length;
  return { codePoints, bytes, valid: codePoints <= GUIDE_CODE_POINT_LIMIT && bytes <= GUIDE_BYTE_LIMIT };
}

export function truncateGuideText(value: string) {
  let result = "";
  for (const character of value) {
    const next = result + character;
    const metrics = guideTextMetrics(next);
    if (metrics.codePoints > GUIDE_CODE_POINT_LIMIT || metrics.bytes > GUIDE_BYTE_LIMIT) break;
    result = next;
  }
  return result;
}

export function expandGuideTemplate(template: string, values: Record<string, string> = GUIDE_PLACEHOLDER_VALUES) {
  let output = "";
  const unknown = new Set<string>();
  for (let index = 0; index < template.length;) {
    const current = template[index];
    if (current === "{" && template[index + 1] === "{") { output += "{"; index += 2; continue; }
    if (current === "}" && template[index + 1] === "}") { output += "}"; index += 2; continue; }
    if (current === "{") {
      const end = template.indexOf("}", index + 1);
      if (end >= 0) {
        const token = template.slice(index + 1, end);
        if (Object.prototype.hasOwnProperty.call(values, token)) output += values[token] || "";
        else { output += template.slice(index, end + 1); if (token.trim()) unknown.add(token); }
        index = end + 1;
        continue;
      }
    }
    output += current;
    index++;
  }
  return { text: output, unknown: [...unknown] };
}
