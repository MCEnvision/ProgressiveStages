# Easy Builder troubleshooting

The localhost Easy Builder edits the authenticated server draft. It does not publish a change
until `Apply changes` passes validation and the review is confirmed. The browser can keep editing a
rejected draft while the last valid runtime remains active.

## ownership choices

The Setup and Access tabs expose four choices.

* Inherited removes both ownership overrides and follows the global team setting.
* Personal writes `scope = "team"` with `team_stage = false`, keeping profession stages with the
  triggering player.
* Team writes `scope = "team"` with `team_stage = true`.
* Server writes `scope = "server"` and removes `team_stage`.

The editor never writes the rejected `scope = "player"` value. Counter scope remains a separate
progression setting.

## LuckPerms mappings

The Access tab keeps the optional bridge dormant when LuckPerms is missing. Inbound rows accept
groups, Boolean permissions, `all` or `any` matching, and context values. Synchronized retention
removes only the bridge derived source after an authoritative loss. Permanent retention keeps that
source. Independent grants are unaffected. Outbound rows create only owned transient group or
permission contributions.

If validation reports a missing group, provider absence, invalid context, duplicate row id, or an
unsupported command path, fix the row in the draft and review again. The source text and last valid
runtime are retained when apply is rejected.

## direct interactions

Use Access, Direct interactions, Add interaction. For Selling Bin access, choose Use an item on a
block, set the held selector to `tag:c:armors` or `all:*`, and set the target block to
`id:selling_bin:selling_bin`. The server checks the live holders and the nonempty held stack before
the interaction continues.

For a selective restriction, enter `id:minecraft:bread` and the Selling Bin block selector, then
enable **Also restrict GUI insertion**. Set **Inventory rule priority** if another inventory rule
also matches. Saving creates an `item_on_block` rule and a matching `item_into_inventory` rule
in one draft change. Both require the containing stage, and both remain independently editable.
An independent empty hand click can still open the menu; other items follow their own rules.
Use **Right click a block** as a separate rule when the whole bin should require a stage.

The pairing option is available when adding a held item rule. For an existing rule, add its
inventory counterpart with the same item, choose **Container block**, enter the same block
selector, and select **Deny until stage is owned**. Conditional activation remains editable in
the Rules tab. Use the same activation on both rules when they should apply together.

## browser and draft recovery

If a field disappears after reload, reopen the stage from the current session. Do not copy the
generated source over the original package. Targeted edits preserve comments, unknown sections,
and unrelated files. A stale revision or failed reload leaves the editable draft and reports the
field or capability diagnostic. Use undo, redo, or reopen to restore the last valid source, then
run `Apply changes` again.

Apply checks the revision shown in the review against the server draft. If another tab or
collaborator changes the draft, the server returns `draft_conflict` without writing live configuration or
reloading stages. The editor closes that stale review and refreshes the current draft. Open
Review again, inspect the updated changes, and confirm them. Older cached tabs that omit the
reviewed revision are also rejected; reopen the editor to load the matching packaged assets.

If the tab reports `403 Forbidden`, close it and open `/pstages editor` again from a permission
level 3 operator. The token belongs to one loopback session and cannot be reused in an older tab.


## Access configuration validation

Use TOML booleans for `luckperms.enabled` and command `descendants`, for example `enabled = false`.
Quoted `"false"` and numeric values are rejected. Write groups and permissions as string arrays,
for example `groups = ["chef"]`; non-string entries are not silently discarded. Omission keeps
the documented default.

The server validation response retains the ordinary error summary and adds structured field
and stable row details for ownership and LuckPerms parsing failures. Package identity failures
name their draft relative `stage.toml` path. A rejected apply preserves the editable source and
installed definitions. Complete inline field presentation and capability feedback remain part
of the final editor verification.

## Editor operation capture

An operator with permission level 3 or the server console can capture one online player's editor
operations with `/stage debug editor on <player>`. Use `/stage debug editor status` for the
server selected output path, limits and writer state, and `/stage debug editor off` to stop.
This uses the same single capture as interactions, progression and permissions. A second capture
is rejected while one is active or draining, and stopping an inactive capture is harmless.

Authenticated draft operations record the action, requested and actual draft revisions, installed
definition and apply revisions, validation outcome, and before and after source digests. These
source digests cover the package file map, including comments and unknown fields, without emitting
file paths, source text, session tokens or error messages. They differ from the header's effective
configuration fingerprint. Current operation records identify the whole draft as their field;
more specific validation rows and complete capability observations remain acceptance work.

A record is reserved when an operation begins. A reload stops new observations immediately, while
an already accepted apply record can finish with its result and installed revision. The writer
waits at most 60 seconds for an accepted operation to complete and reports output failure if it
cannot. No new operation is accepted after stop. Source hashing and record serialization run on
the writer thread; the server supplies immutable before and after snapshots. Status bytes include
up to 4096 reserved bytes for each pending editor operation until its actual output size is known.
All reservations and the capture header share the 128 KiB limit. The usual 60 second, 200 sample,
20 sample per second and 256 queued record limits still apply. Exhaustion reports `sample_limit`
or `byte_limit`, with `rate_limit` and the existing lifecycle reasons where appropriate.

Use `invalid_field` with `validation_failed` to identify a rejected apply, `stale_revision` for a
revision conflict, and `applied` with matching apply and definition revisions for a successful
transaction. A draft edit uses `draft_changed`; `source_preserved` and equal source digests identify
an unchanged source observation. Provider state is `not_observed` unless the operation already
produced an authoritative capability response. Do not interpret that value as provider readiness.
Use the [capture privacy and lifecycle guide](interaction-locks.md) when collecting a support report.
