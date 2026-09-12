# Selling Bin interaction repair

This record describes the generic interaction enforcement repair for ProgressiveStages 3.0.5 development. The production code has no Selling Bin dependency or special case.

The repaired path uses holder-aware `PrefixEntry` matching for item and block selectors. It accepts `tag:c:armors` and legacy `#c:armors`, exact `id:selling_bin:selling_bin`, and the `all:*` wildcard. The raw selector text remains unchanged in parsed definitions. A nonempty held stack is required for `item_on_block` rules. One immutable decision is shared by cancellation, feedback, and the optional server capture.

The candidate reviewed for this work is wd's Selling Bin `1.6-NEOFORGE-1.21.1`. The recorded SHA-256 is `025c96f5cf1ab531e75d11ef9ed655fe64878dd4228d73b26c419b688e3abf7d`. Its bundled `wdUtils`, Fancy Tab Sections, and Tiny Multiblock Library dependencies were recorded with the candidate manifest.

The pinned project loader is NeoForge 21.1.219. The candidate metadata requires newer NeoForge for its bundled libraries, so the exact candidate was started only in an isolated NeoForge 21.1.233 runtime. That server reached readiness and loaded the candidate. The pinned candidate runtime was rejected by dependency validation before gameplay. This is an external compatibility limitation, not evidence that the reporter's exact environment was reproduced.

Focused selector, parser, decision, and full build checks pass on the pinned project toolchain. A dedicated core startup smoke also remains independent of Selling Bin. The laptop presentation gate is not claimed here because the isolated client did not reach a responsive joined world and its application audio stream could not be verified over the available desktop connection. No release artifact is published by this work.
