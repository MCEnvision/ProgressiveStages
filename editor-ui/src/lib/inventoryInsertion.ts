import { encodeToml } from "./toml";

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
