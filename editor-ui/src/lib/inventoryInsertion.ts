import { encodeToml } from "./toml";

export interface InventoryInsertionRuleDraft {
  selector: string;
  targetKind: "block" | "menu" | "inventory";
  destination: string;
  effect: "lock" | "deny" | "allow" | "unlock" | "exclude";
  priority: number;
}

export function serializeInventoryInsertionRule(draft: InventoryInsertionRuleDraft): string {
  return [
    "[[interactions]]",
    'type = "item_into_inventory"',
    `held_item = ${encodeToml(draft.selector)}`,
    `target_kind = ${encodeToml(draft.targetKind)}`,
    `target = ${encodeToml(draft.destination)}`,
    `effect = ${encodeToml(draft.effect)}`,
    `priority = ${draft.priority}`
  ].join("\n");
}
