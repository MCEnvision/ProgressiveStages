# LuckPerms bridge troubleshooting

ProgressiveStages can read LuckPerms groups and Boolean permissions for a stage and can project a
stage's existing group or positive permission contributions back to LuckPerms. The integration is
optional and remains dormant when LuckPerms is absent. It uses the compile only API 5.4 surface and
is intended for the LuckPerms NeoForge 5.4.140 runtime on Minecraft 1.21.1.

## Configuration

Add the optional tables to the stage file. `inbound_mode` is `synchronized` by default. Use
`permanent` when losing the qualifying rank should leave the stage owned. Independent grants remain
independent in either mode.

```toml
[luckperms]
enabled = true
inbound_mode = "synchronized"

[[luckperms.inbound]]
id = "chef_rank"
groups = ["chef"]
permissions = ["professions.chef"]
match = "any"

[[luckperms.outbound]]
id = "home_permission"
kind = "permission"
value = "neoessentials.teleport.home.set"

[[command_permissions]]
id = "home_gate"
path = "sethome"
descendants = true
```

Inbound rows use `all` or `any` matching. Only a LuckPerms Boolean result of true qualifies a
permission. False and undefined results do not qualify. Context tables use an AND between keys and
an OR between values for each key. A reserved bridge marker is never accepted in configuration.

Outbound rows refer to an existing group or permission. ProgressiveStages owns only the transient
contribution it created, and removes it when the effective stage or provider context no longer
requires it. Existing administrative nodes and independent membership are preserved.

## Diagnosis

Use the permissions capture to inspect provider state, stage, row, desired result and reconciliation
reason.

```text
/stage debug permissions on <player>
/stage debug permissions status
/stage debug permissions off
```

The provider states are absent, starting, ready and failed. A missing provider, disabled stage, or
missing group leaves the source configuration intact and performs no stage mutation. Reload and
restart revalidate synchronized sources before they become effective. Permanent and independent
sources remain effective while the provider is unavailable.

If a stage is not granted, check the stage dependency and slot policy first. Permission
reconciliation never charges a cost, runs a reward, increments a trigger counter, refreshes an
expiry, or grants a prerequisite. A synchronized source is removed after an authoritative loss;
permanent and independent sources remain.

## Command gates

`command_permissions` matches the actual parsed literal path. Descendant rules cover the literal
subtree, while a non descendant rule covers the configured deepest literal and its argument values.
Aliases and namespaced literals are compared using their actual dispatcher binding. Native command
permission checks still run, so a stage cannot elevate a player. The gate is evaluated before the
command side effect and preserves the effective player through delegated execution.

## Recovery

Confirm the player is online, the provider reports ready, and the configured group exists. Check
the row's contexts and whether the permission result is true. If the provider was restarted, wait
for the bounded reconciliation queue to drain or use `/stage sync` after the provider is ready.
Capture output is bounded and redacts long values. Send only the relevant capture lines and the
before and after stage state. Never include a permission tree, credentials, private address or whole
inventory.
