# progression scopes and ownership

ProgressiveStages resolves a stage owner for the actual player before it checks dependencies,
slots, locks, triggers, rewards, or integration callbacks. This keeps profession stages private
when a pack uses FTB Teams for other progression.

## stage settings

```toml
[stage]
id = "profession:chef"
scope = "team"
team_stage = false
```

The `team_stage` key is optional. When omitted, the stage inherits `general.team_mode` and the
available team provider. `false` stores an actor-owned record. `true` requests the active FTB Teams
owner and falls back to the actor UUID when FTB Teams is disabled or unavailable. A stage with
`scope = "server"` is always stored in the server namespace and cannot declare `team_stage`.
The parser rejects `scope = "player"`; personal ownership is selected with `team_stage = false`.

Counters retain their own player, team, or server subject scope. Changing `team_stage` does not
rewrite counter history.

## effective views and membership

The server stores team records in `team_stages`, personal records in `personal_stages`, and source
labels in `stage_sources`. A player's effective view is the union of their personal records, the
resolved team record, and server records. Personal records never move when team membership changes.
Legacy team records remain in their original namespace and are not copied to every member.

An owner or provider change masks incompatible history while retaining the original record. Restoring
the compatible setting reveals that history again. Use an explicit administrative regrant when a
pack intentionally wants a new owner to receive a stage.

## Permission contributors

Source labels distinguish the contributing player, mapping row and retention mode within each
owner namespace. Removing one synchronized source preserves other contributors and independently
earned access. Adding the first derived source to an existing stage with no source labels preserves
that legacy stage as independent. A newly derived stage receives no independent grant.

When the current subject reconciles after an owner change, obsolete synchronized contributions are
removed from their original owner before new grants are evaluated. Deleting a definition also
withdraws that subject's synchronized contribution. Independent and permanent owner history stays
in place. This path does not migrate a contribution to another owner or grant an offline player.

Synchronized records loaded from disk remain stored but do not contribute effective access until
revalidated. Independent and permanent records remain effective. Explicit revocation and bulk
revocation still address pending stored entitlements. Inactive contributions cannot satisfy stage
dependencies or appear as effective source kinds.

The [LuckPerms guide](luckperms.md#inbound-source-ownership) describes current cleanup coverage and
the remaining migration and membership lifecycle limitations.

## diagnostics

Operators can inspect one resolved owner with:

```
/stage explain scope <player> <stage>
```

For a bounded redacted trace, use one capture at a time:

```
/stage debug progression on <player>
/stage debug progression status
/stage debug progression off
```

The capture is off by default and uses the same fixed limits as interaction diagnostics. Output is
written to the server selected `logs/progressivestages/progression/<capture-id>.log` path. Records
include scope presence, owner kind, pseudonymous owner label, effective before and after sets,
recipient count, cause, and mutation revision. Names, UUIDs, inventories, NBT, addresses, and raw
permission trees are not written.

If a reload fails, the last valid stage snapshot remains active. Stop an active capture before a
reload or shutdown; the lifecycle hooks stop it automatically and drain the bounded writer.

Capture status distinguishes stopped recording from completed output. Wait for `Writer: drained`
before reading the finished support file. `Writer: failed` leaves the capture incomplete; a later
write failure remains visible after manual stop. The [shared capture procedure](interaction-locks.md)
describes category isolation, fixed limits, server tick timing, and bounded JSON fields.
