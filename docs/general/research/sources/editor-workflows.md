# Editor workflow amendment evidence

This record supports SRC-022, SRC-023 and SRC-024. Repository observations are pinned to the approved source revision c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6. Source presence is not runtime verification. No product build, browser session or Minecraft runtime was launched for this amendment.

## Requested scope

The owner requests easier translation, complete editor help, nearby workflows and main config editing, and proposes an editable preview with hide/show controls, visible conditions and activation after acquisition. The main config request is explicit: "the main config should also be editable from the gui editor."

The bounded mandatory interpretation adds a local translation-help button and translation friendly presentation, complete existing main settings, and a preview using existing visibility and ownership rules. A direct Google link carrying selected text and a separate claim or additional activation mechanism remain optional pending the outstanding choices. No response has been treated as consent to external transmission or a new gameplay mechanism. Reusable presets and selective bulk changes are recommendations only. DEC-009 through DEC-011 and FUT-002 through FUT-005 record these boundaries; existing release and guide decisions remain intact.

## Existing configuration paths

CodeGraph first identified SettingsPage, EditorDraft, StageDefinition and EditorApplyService and their connected schema and draft boundaries. The current root index is older than the approved master. Bounded git show reads supplied the pinned implementation details and reflective spec coverage that the graph could not establish. Files included solely as test dependencies are fingerprinted for freshness, not claimed executed or fully reviewed.

SettingsPage filters bootstrap schemas for progressivestages.toml and groups by the first key segment. SettingControl uses readTomlValue, mutateFile and upsertToml, with Boolean, enum, list, number and string branches. It already edits the main file. A main settings rewrite is unnecessary; complete coverage, semantics and reliable application need proof.

BuiltinEditorSchemas.populate calls addSettings with StageConfig.SPEC.getSpec. addSettings recursively enumerates ValueSpec leaves and derives default, comment and restart metadata. settingType derives basic types from defaults, while the generated hints currently only identify generation. Range, enum/list constraints and correct process-specific restart behavior need explicit schema and runtime validation, not assumptions from an HTML input. Boolean help currently follows a separate SettingsPage path. EditorSchemaRegistry already exposes immutable sorted schemas and a coverage check.

EditorDraftValidator treats progressivestages.toml specially: it parses TOML syntax and continues without applying the actual ModConfigSpec validation. Stage validation uses live team mode. This is static evidence of a validation gap, not a reproduced production failure. EditorApplyService has existing review, conflict, backup and file replacement protections, followed by StageFileLoader.reload and sync. Those calls alone do not prove the main spec and cached effective settings changed. IF-007 therefore requires complete candidate validation, explicit lifecycle application, effective state readback, combined main/stage consistency and failure rollback.

## Visibility, activation and preview

EssentialsPanel already edits hidden, display.reveal, frame, category, color, background and coordinates. LayoutPage is a custom SVG authoring graph, not a faithful player-window preview. StageTreeScreen.rebuild excludes hidden stages before layout; revealed returns true for owned stages, tests prerequisites for dependencies, false for an unowned unlocked policy and true otherwise. Hidden and reveal are distinct. Some raw editor fallback strings differ from model defaults, so projection must use the actual accepted parser semantics and preserve omitted keys.

RulesPanel already has stage ownership missing, owned and always, an activation condition, condition target, lifetime and reset controls for supported rule forms. ConditionalRule declares OWNED, MISSING and ALWAYS plus LIVE and TRIGGERED. CompiledRuleEngine.stageStateMatches honors explicit stage_state, then applies effect specific defaults; its activation policy also accepts duration, cooldown, debounce, grace and minimum-state durations. These are existing controls to explain and reuse, not evidence that a new claim step is needed. Source-preserving compound conditions must not be replaced by the simple condition form.

The proposed preview reads one validated draft projection, uses explicit simulated owned stages, shares deterministic visibility/prerequisite/layout fixtures with the client and never calls grant, purchase, command, reward, apply or player persistence. Unknown live conditions remain unevaluated. Author view can expose hidden definitions to authorized editors; simulated Player preview must not expose hidden names or counts. Actual visual and synchronization claims still require the silent laptop client.

## Primary translation references

- [Chrome translation help](https://support.google.com/chrome/answer/173424?hl=en) documents toolbar and context-menu page translation and selected-text translation. These are browser actions; the page does not document a web-page API to open them. The local help button can explain them without pretending to invoke native UI.
- [Google website translation help](https://support.google.com/translate/answer/2534559?hl=en&co=GENIE.Platform%3DDesktop) describes URL-based website translation and eligibility restrictions for its website widget. An unrestricted free embedded widget is not established. Sending an authenticated local editor URL is not part of this plan.
- [Brave translation help](https://support.brave.app/hc/en-us/articles/8963107404813-How-do-I-use-Brave-Translate) documents Brave's built-in translation. It must not be described as Google's translator by default.
- [HTML translate attribute](https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Global_attributes/translate) documents translate=no as a hint for text and eligible attributes. It is not a privacy or access-control boundary. Preserve code, IDs and authored values and verify actual browser behavior rather than assuming every provider obeys hints.

The implementation inference is to prefer user operated browser translation over routing a local authenticated page through a public URL translator. It introduces no service credential or API billing. Translation can still process page text through the chosen browser provider, so the UI must not claim offline or private translation. Browser/version support and DOM mutation behavior remain execution checks. No editor text or URL was sent to a translator during research.
