# Interaction locks

ProgressiveStages evaluates `item_on_block` and `block_right_click` rules on the server before a player interaction continues. The held item and clicked block are matched against their live registry holders, so tags use the actual tag membership of the item or block.

Use either modern selectors such as `tag:c:armors`, `id:selling_bin:selling_bin`, and `all:*`, or the legacy `#c:armors` tag form. An unprefixed namespaced identifier remains an exact identifier. The held stack must be nonempty for an `item_on_block` rule. Empty hand behavior remains available through ordinary block rules.

A missing required stage cancels the server interaction before the protected action continues. The client never decides whether an interaction is allowed. Creative bypass and unrelated interactions retain their existing behavior.

For a bounded support capture, an operator or console can run:

```
/stage debug interactions on <player>
/stage debug interactions status
/stage debug interactions off
```

The command requires permission level 3 and an online target. Only one capture can run at a time. The server chooses the output file and reports it through status. A capture lasts at most 60 seconds, records at most 200 decisions and 20 decisions per second, queues at most 256 records, and writes at most 128 KiB. Strings are limited to 256 characters and stage lists to 32 entries. Captures stop on reload, shutdown, restart, disconnect, timeout, or the first exhausted limit. The disabled path does not create records or perform file I/O.

Inspect only the path returned by status. Keep the smallest sanitized excerpt containing the capture id, selected hand, item and block identifiers, matched and missing stages, reason, cancellation flags, result, and mutation outcome. Captures never include player names, UUIDs, NBT, inventories, permission trees, credentials, addresses, raw commands, or arbitrary paths.

To reproduce a report, first enable the capture, perform one controlled click with the exact held item and target block, check status, then disable the capture. Compare the server decision with the resulting stack, menu, and sale state. A valid sale fixture must be tested separately from an unvalued item. If the candidate mod or dependency does not meet the pinned loader requirements, record that compatibility limit and do not call it a report reproduction.
