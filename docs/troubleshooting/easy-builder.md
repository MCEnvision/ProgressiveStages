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

## browser and draft recovery

If a field disappears after reload, reopen the stage from the current session. Do not copy the
generated source over the original package. Targeted edits preserve comments, unknown sections,
and unrelated files. A stale revision or failed reload leaves the editable draft and reports the
field or capability diagnostic. Use undo, redo, or reopen to restore the last valid source, then
run `Apply changes` again.

If the tab reports `403 Forbidden`, close it and open `/pstages editor` again from a permission
level 3 operator. The token belongs to one loopback session and cannot be reused in an older tab.
