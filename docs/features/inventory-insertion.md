# Inventory Insertion Rules

`item_into_inventory` prevents a player from putting a matching item into a
matching receiving inventory until that player owns the stage that contains the
rule. It is useful for profession systems, selling bins, machine inputs, and
class-specific storage without changing hoppers or other playerless automation.

## Quick Example

Put this in the stage package `rules.toml` file for a miner stage.

```toml
[[interactions]]
type = "item_into_inventory"
held_item = "tag:c:ores"
target_kind = "block"
target = "id:example:selling_bin"
effect = "lock"
priority = 100
```

Before the player owns the miner stage, ore cannot enter that selling bin from
the player's cursor, hotbar, inventory, shift-click action, or drag action.
After the stage is owned, the same action is allowed.

## Required Fields

| Field | Meaning |
| --- | --- |
| `type` | Must be `item_into_inventory`. |
| `held_item` | Selector for the item being inserted. |
| `target_kind` | `block`, `menu`, or `inventory`. |
| `target` | Selector for the receiving destination identity. |
| `effect` | `lock`, `deny`, `allow`, `unlock`, or `exclude`. |
| `priority` | Integer used when more than one matching rule exists. Higher values win. |

Both `held_item` and `target` accept `all:*`, `id:`, `mod:`, `tag:`, `name:`,
or `#namespace:tag`. A bare identifier is also an exact identifier. Use
`all:*` carefully because it matches every item or every destination of the
chosen kind.

## Destination Kinds

`block` means the receiving slot belongs to a block entity. For example, a
furnace uses `id:minecraft:furnace` and a chest uses `id:minecraft:chest`.

`menu` means the rule matches the registered menu type open for the player.
This is useful when a mod exposes a stable menu identifier but several block
variants share it.

`inventory` means the rule matches a stable server inventory-owner identity.
The built-in player inventory identity is `minecraft:player_inventory`.
ProgressiveStages and compatible mods can expose more owner identities without
depending on client labels, Java class names, coordinates, or the last player
to open a menu.

## Priority and Exceptions

The broad rule below blocks every item entering every block-backed inventory.
The second rule is narrower and has a higher priority, so raw iron is exempt
from the selling-bin part of the broad lock.

```toml
[[interactions]]
type = "item_into_inventory"
held_item = "all:*"
target_kind = "block"
target = "all:*"
effect = "lock"
priority = 100

[[interactions]]
type = "item_into_inventory"
held_item = "id:minecraft:raw_iron"
target_kind = "block"
target = "id:example:selling_bin"
effect = "exclude"
priority = 200
```

`lock` and `deny` deny a matching insertion until the owning stage is owned.
`allow` and `unlock` allow a matching insertion when the owning stage is owned.
`exclude` always permits that exact pairing. A matching deny beats a matching
allow when their priorities are equal.

## Easy Builder Workflow

1. Open `/pstages editor` as an operator.
2. Open the stage, then select **Rules** and **Add rule**.
3. Select **Interactions** and **Insert an item into an inventory**.
4. Choose the inserted-item selector mode and value.
5. Choose the destination kind, then choose its selector mode and value.
6. Choose the effect and priority, then save the draft.
7. Use **Review and apply** only after validation succeeds.

The server catalog filters item, block, menu, and registered inventory-owner
choices. The TOML source tab shows the exact canonical entry written by the
form. Editing or deleting the rule through either view keeps the same stage
draft until the operator applies it.

## Transaction and Automation Boundary

The server evaluates a real `ServerPlayer` before the receiving inventory
changes. It covers normal clicks, shift-click, drag placement, hotbar swaps,
and player inventory destinations. Double-click collection only removes items
into the carried stack and has no receiving inventory to gate. A denied
transaction is resynchronized from server state so client ghost stacks are not
retained.

Hoppers, pipes, capability transfers, and machines without an authenticated
initiating player are not subject to this rule. ProgressiveStages never guesses
which nearby player should own an automation action.

## Compatible Inventory Targets

Java integrations register a stable resolver and optional catalog descriptors
with `ProgressiveStagesAPI.registerInventoryTargetResolver`. A resolver
receives the authenticated server player, open menu, and receiving slot. It
returns a namespaced identity and tags only when it can identify the receiving
inventory unambiguously.
Duplicate resolver identifiers, duplicate target identities, and a descriptor
claimed by another resolver are rejected during registration.

Use a stable resource location such as `example:selling_bin`. Do not use a
display name, Java class name, block coordinate, container owner, last opener,
or data supplied by the client as the persisted target identity.

## Troubleshooting

If a rule appears not to match, confirm that it is in `rules.toml`, uses exactly
`type = "item_into_inventory"`, and has both source and destination fields.
Use the source view to check `target_kind` and `target`. Apply the draft, then
reload the server configuration before retesting an already open menu when your
server workflow requires it.

If a target does not appear in the inventory-owner picker, the server has not
registered a stable resolver for it. Use `block` or `menu` when those identities
fit, or ask the compatible mod to contribute an inventory target resolver.
