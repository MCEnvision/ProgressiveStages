# LuckPerms bridge verification

This record describes the Phase 002 implementation boundary. The selected compile only API is
`net.luckperms:api:5.4`. The selected runtime candidate is LuckPerms NeoForge 5.4.140 for Minecraft
1.21.1. NeoEssentials 1.0.4 build 61 is the real command side effect candidate for
`neoessentials.teleport.home.set`.

The artifact checks are bound to the following bytes. The API SHA 256 is
`086e3971ea63c0b5ad567881b2dfd955acbbffa24fe85ceac8abf54b114ce986`. The LuckPerms runtime SHA 256 is
`6b8097a7e1a27d870d3d472d00079fa958271db16b9433369dce6c7f62530b19`. The NeoEssentials runtime SHA 256 is
`60557f5942985fc538b10d0bd93c8f0065f964e1ee1f38649e3161713e74a506`.

Automated coverage parses valid and invalid inbound, outbound and command tables, rejects duplicate
row IDs and reserved contexts, preserves source configuration, and validates the optional boundary
when the provider is absent. Stage sources are attributed by owner, stage and inbound row. The
bridge supports synchronized and permanent retention, overlapping rows, true versus false or
undefined Boolean results, context matching, outbound reference counting, provider loss and a
bounded reconciliation queue.

Command gates run from NeoForge `CommandEvent` after Brigadier has resolved the command and before
execution side effects. Literal descendants, argument values, aliases, namespaced literals and
delegated effective actors are evaluated without changing native permission predicates.

The real runtime acceptance matrix requires a dependency only startup, bridge disabled startup,
provider ready login, inherited group and Boolean permission queries, explicit negative values, and
the NeoEssentials home storage before and after a non operator command attempt. A denied attempt
must leave the home record unchanged. An allowed attempt must create one expected home. Provider
loss, reload, restart, rank removal, source overlap and independent stage preservation are checked
before cleanup.

Headless server evidence is retained with exact artifact hashes, fixture configuration, sanitized
capture excerpts, command and storage oracles, process shutdown confirmation and disposable path
cleanup. A laptop input or rendered feedback claim remains open until the matched silent laptop
client is joined to the disposable dedicated server and its application audio stream is verified
muted.

On 2026-09-11, the exact LuckPerms and NeoEssentials candidates loaded with Minecraft 1.21.1 and
NeoForge 21.1.219 in the disposable dedicated server. LuckPerms reported successful enablement and
the server reached `Done`. A second run with the LuckPerms jar absent reached `Done` with the bridge
dormant. The laptop login, provider-managed rank changes and real command side-effect matrix remain
unverified because the required laptop session was not available on the headless execution host.
