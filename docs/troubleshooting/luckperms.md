# LuckPerms bridge troubleshooting

ProgressiveStages can read LuckPerms groups and Boolean permissions for a stage and can project a
stage's existing group or positive permission contributions back to LuckPerms. The integration is
optional and remains dormant when LuckPerms is absent. It uses the compile only API 5.4 surface and
was developed against the selected LuckPerms NeoForge 5.4.140 candidate on Minecraft 1.21.1.
That exact candidate currently fails actual player login on NeoForge 21.1.248 even with
ProgressiveStages absent. See the [provider login evidence](../verification/luckperms-bridge.md#neoforge-211248-dependency-only-login-failure).
Successful server startup alone does not establish compatibility. The
[adapter source audit](../verification/luckperms-bridge.md#adapter-source-audit) also identifies
ProgressiveStages defects in persistence, context isolation, ownership and cleanup. Outbound
storage, reference handling and contextual queries now have isolated regression coverage. Real
provider context isolation and full lifecycle acceptance remain open, so the provider integration
is not ready for live use.

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
an OR between values for each key. Context keys and values are compared without case sensitivity,
while source casing is preserved. A reserved bridge marker, including differently cased aliases,
and duplicate keys differing only in case are rejected in configuration.

The query adapter uses current provider contexts for a loaded online user and static provider
contexts for a loaded offline user. It preserves multiple values per key and removes the bridge
marker from eligibility queries. An unloaded user or query failure is unavailable, not an
authoritative undefined permission. A false or unavailable permission query prevents positive
outbound output and withdraws an existing owned contribution. An authoritative undefined result
can receive a configured positive contribution. These query decisions have isolated API and core
server coverage; actual inherited exclusion and negative precedence still require provider proof.

Outbound rows refer to an existing group or permission. The required behavior is to own only
transient contributions, remove them when no longer required, and preserve administrative nodes
and independent membership. The adapter now writes only transient data and attaches an ownership token to
created nodes. Equal administrative nodes are not adopted or removed. Overlapping rows retain
separate references, row replacement removes the old contribution first, and failed operations
remain retryable. Logout and shutdown attempt exact owned cleanup. A cleanup warning means the
adapter and references remain retained, and another bind cannot silently replace them.

The reserved `progressivestages_bridge=active` context is published for the exact current player
object only after the intended node mutations are confirmed. Dirty state, reload, disconnect and
shutdown invalidate it before cleanup. An old reconciliation ticket cannot activate a replacement
session. Failed publication or cleanup leaves the projection unavailable and retains retry state.
Reload schedules a bounded online scan with at most sixteen subject reconciliations per tick;
it no longer queries every player directly inside the reload call. Context calculator cleanup is
part of shutdown completion. These controls still require real provider and native permission
acceptance alongside the core and isolated API checks.

User load, unload and node changes enqueue the affected UUID. Group changes, full synchronization
and provider configuration reload request a bounded rescan. Callbacks invalidate calculator state
without querying stages or loading users; provider cache notifications happen during server
reconciliation. Cache recalculation alone does not trigger another pass. Shutdown disables event
delivery before detaching listeners. Failed detachment keeps inactive handles for cleanup retry.

Actual provider behavior is still unverified. Do not treat restarting as proof of cleanup, and do
not remove historical persistent nodes merely because their names match a mapping. Any node left
by an older development fixture needs its exact ownership established before removal. The new
transient ledger does not retroactively claim or delete those historical nodes.

## Inbound source ownership

A contribution is stored under the stage's resolved owner and identifies its player, mapping row
and retention mode. Two players qualifying through the same row remain separate contributors.
Removing one synchronized contribution preserves other rows, other players and independent grants.
A legacy stage without source labels keeps its independent meaning when a new contribution is added.

For a registered stage and its current owner, deleting an inbound row or the LuckPerms table removes
that player's synchronized contribution on reconciliation. Retained permanent contributions survive
these changes. Older labels without player attribution are not guessed or deleted automatically.
Before checking new inbound eligibility, reconciliation removes the current player's synchronized
contributions under obsolete owners or deleted definitions. Other players' contributions and
independent or permanent history are preserved. The existing FTB membership detector invokes this
path before team synchronization. Offline contributors, startup provider revalidation, unattributed legacy
migration, exact membership event timing and expiry episode recovery still need complete lifecycle
verification. A successful online reconciliation does not prove offline cleanup.

## Saved sources awaiting revalidation

A saved synchronized source is inactive after data loading until it qualifies again. Its record is
retained, while independent and permanent grants remain effective. Pending sources do not satisfy
stage access or appear as effective source kinds. Revalidating an existing source does not create
an independent grant or restart its stored acquisition timestamp.

Explicit revoke and revoke-all still remove pending stored entitlements. Pending progression also
prevents default first-join starter grants from repeating. Do not mistake a pending
source for a lost save record. The current load and reactivation checks do not establish complete
offline loading, provider restart convergence, context invalidation or expiry episode behavior.

## Diagnosis

Use the permissions capture to inspect provider state, stage, row, desired result and reconciliation
reason.

```text
/stage debug permissions on <player>
/stage debug permissions status
/stage debug permissions off
```

The adapter interface defines absent, starting, ready and failed states. Its current reflective
implementation reports ready after obtaining the API; this is not proof of a working player
query, successful mutation, cleanup or login. Missing provider and group warnings preserve source
configuration. Reload, restart, provider loss and source retention still require the complete
runtime acceptance matrix.

The online update queue retains pending subjects when its 256 entry limit is reached and requests
a resumable scan. Dirty updates and scan entries share the limit of sixteen subjects per tick;
continued event traffic does not reset an active scan. Disconnect requests a followup pass, and
shutdown clears pending work. Core fixtures cover online event and reload processing. They do not
prove offline contributor recovery or actual provider event convergence.

If a stage is not granted, check the stage dependency and slot policy first. Permission
reconciliation must not charge costs, run rewards, increment trigger counters, refresh expiry
episodes or grant prerequisites. Current source tests cover missing prerequisites, purchase denial
without an XP charge and unchanged repeated grants; complete episode behavior remains unverified.
A synchronized source is removed after an authoritative loss; permanent and independent sources remain.

## Command gates

`command_permissions` matches resolved command nodes at Minecraft's execution tasks after redirects,
using the effective player at that point. Descendant rules cover the literal
subtree, while a non descendant rule covers the configured deepest literal and its argument values.
Aliases and namespaced literals are compared using their actual dispatcher binding. Native command
permission checks still run, so a stage cannot elevate a player. The gate is evaluated before the
command side effect and preserves the effective player through delegated execution.

An unresolved path produces an inactive rule warning. Reload after the command provider becomes
available. A similarly named command in another namespace is not automatically an alias. The
[core runtime regression](../verification/luckperms-bridge.md#command-execution-regression) covers
vanilla redirects and custom result callbacks; the full real provider and client matrix remains open.

## Recovery

Confirm the player is online, the provider reports ready, and the configured group exists. Check
the row's contexts and whether the permission result is true. If the provider was restarted, wait
for the bounded reconciliation queue to drain or use `/stage sync` after the provider is ready.
Capture output is bounded and redacts long values. Send only the relevant capture lines and the
before and after stage state. Never include a permission tree, credentials, private address or whole
inventory.

Capture status distinguishes stopped recording from completed output. Wait for `Writer: drained`
before reading the finished support file. `Writer: failed` leaves the capture incomplete; a later
write failure remains visible after manual stop. The [shared capture procedure](interaction-locks.md)
describes category isolation, fixed limits, server tick timing, and bounded JSON fields.
