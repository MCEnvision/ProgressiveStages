import { useMemo, useState } from "react";
import { Badge, Button, EmptyState, Field, Section, Toggle } from "../../components/ui";
import { appendRow, parseCommandPermissions, parseInteractions, parseLuckPerms, parseOwnership, replaceCommandPermissions, replaceInbound, replaceInteractions, replaceOutbound, serializeCommandPermission, serializeInbound, serializeInteraction, serializeOutbound, updateCommandPermissionBlock, updateInboundBlock, updateInteractionBlock, updateOutboundBlock, writeLuckPermsSettings, writeOwnership } from "../../lib/integrations";
import { lineValues } from "../../lib/toml";
import { useEditor } from "../../store/EditorContext";
import type { CommandPermissionModel, InboundModel, InteractionModel, OutboundModel, StagePackage } from "../../types";

function contextText(contexts: Record<string, string[]>): string {
  return Object.entries(contexts).map(([key, values]) => `${key}=${values.join(",")}`).join("\n");
}

function parseContextText(value: string): Record<string, string[]> {
  const result: Record<string, string[]> = {};
  for (const line of value.split(/\r?\n/)) {
    const split = line.indexOf("=");
    if (split <= 0) continue;
    const key = line.slice(0, split).trim();
    const values = lineValues(line.slice(split + 1));
    if (key && values.length) result[key] = values;
  }
  return result;
}

function InboundForm({ row, onSave, onCancel }: { row?: InboundModel; onSave: (row: InboundModel) => Promise<void>; onCancel: () => void }) {
  const [id, setId] = useState(row?.id || "chef_rank");
  const [groups, setGroups] = useState(row?.groups.join(", ") || "");
  const [permissions, setPermissions] = useState(row?.permissions.join(", ") || "");
  const [match, setMatch] = useState<"all" | "any">(row?.match || "all");
  const [contexts, setContexts] = useState(contextText(row?.contexts || {}));
  return <form className="dialog-form" onSubmit={event => { event.preventDefault(); void onSave({ id, groups: lineValues(groups), permissions: lineValues(permissions), match, contexts: parseContextText(contexts) }); }}>
    <div className="form-grid">
      <Field label="Mapping id" help="Stable id used in diagnostics and source attribution."><input value={id} onChange={event => setId(event.target.value)} required/></Field>
      <Field label="Match policy"><select value={match} onChange={event => setMatch(event.target.value as "all" | "any")}><option value="all">Require every condition</option><option value="any">Require any condition</option></select></Field>
      <Field label="LuckPerms groups" help="Comma separated inherited or direct group names." wide><input value={groups} onChange={event => setGroups(event.target.value)} placeholder="chef, master_chef"/></Field>
      <Field label="Boolean permissions" help="Comma separated permission nodes. Only true qualifies." wide><input value={permissions} onChange={event => setPermissions(event.target.value)} placeholder="professions.chef"/></Field>
      <Field label="Contexts" help="One key per line, with comma separated values. Keys combine with AND." wide><textarea rows={4} value={contexts} onChange={event => setContexts(event.target.value)} placeholder="server=survival\nworld=overworld"/></Field>
    </div>
    <footer className="dialog-actions"><Button type="button" tone="quiet" onClick={onCancel}>Cancel</Button><Button type="submit" tone="primary" disabled={!groups.trim() && !permissions.trim()}>Save inbound mapping</Button></footer>
  </form>;
}

function OutboundForm({ row, onSave, onCancel }: { row?: OutboundModel; onSave: (row: OutboundModel) => Promise<void>; onCancel: () => void }) {
  const [id, setId] = useState(row?.id || "home_permission");
  const [kind, setKind] = useState<"group" | "permission">(row?.kind || "permission");
  const [value, setValue] = useState(row?.value || "");
  const [contexts, setContexts] = useState(contextText(row?.contexts || {}));
  return <form className="dialog-form" onSubmit={event => { event.preventDefault(); void onSave({ id, kind, value, contexts: parseContextText(contexts) }); }}>
    <div className="form-grid">
      <Field label="Mapping id"><input value={id} onChange={event => setId(event.target.value)} required/></Field>
      <Field label="Output kind"><select value={kind} onChange={event => setKind(event.target.value as "group" | "permission")}><option value="permission">Permission node</option><option value="group">Group membership</option></select></Field>
      <Field label={kind === "group" ? "Group name" : "Permission node"} help="ProgressiveStages owns only its transient contribution." wide><input value={value} onChange={event => setValue(event.target.value)} placeholder={kind === "group" ? "chef" : "neoessentials.teleport.home.set"} required/></Field>
      <Field label="Contexts" help="Optional context values for the outbound node." wide><textarea rows={4} value={contexts} onChange={event => setContexts(event.target.value)} placeholder="server=survival"/></Field>
    </div>
    <footer className="dialog-actions"><Button type="button" tone="quiet" onClick={onCancel}>Cancel</Button><Button type="submit" tone="primary">Save outbound mapping</Button></footer>
  </form>;
}

function CommandForm({ row, onSave, onCancel }: { row?: CommandPermissionModel; onSave: (row: CommandPermissionModel) => Promise<void>; onCancel: () => void }) {
  const [id, setId] = useState(row?.id || "home_gate");
  const [path, setPath] = useState(row?.path || "sethome");
  const [descendants, setDescendants] = useState(row?.descendants ?? true);
  return <form className="dialog-form" onSubmit={event => { event.preventDefault(); void onSave({ id, path, descendants }); }}>
    <div className="form-grid"><Field label="Rule id"><input value={id} onChange={event => setId(event.target.value)} required/></Field><Field label="Command literal path" help="Use literals separated by spaces, without a leading slash."><input value={path} onChange={event => setPath(event.target.value)} placeholder="sethome" required/></Field><Toggle label="Gate descendants" help="Also gate child literals and their argument values." checked={descendants} onChange={setDescendants}/></div>
    <footer className="dialog-actions"><Button type="button" tone="quiet" onClick={onCancel}>Cancel</Button><Button type="submit" tone="primary">Save command gate</Button></footer>
  </form>;
}

function InteractionForm({ row, onSave, onCancel }: { row?: InteractionModel; onSave: (row: InteractionModel) => Promise<void>; onCancel: () => void }) {
  const [type, setType] = useState<InteractionModel["type"]>(row?.type || "item_on_block");
  const [heldItem, setHeldItem] = useState(row?.heldItem || "");
  const [targetBlock, setTargetBlock] = useState(row?.targetBlock || "");
  const [targetEntity, setTargetEntity] = useState(row?.targetEntity || "");
  const [targetKind, setTargetKind] = useState(row?.targetKind || "block");
  const [target, setTarget] = useState(row?.target || "");
  const [effect, setEffect] = useState(row?.effect || "lock");
  const [priority, setPriority] = useState(row?.priority || 0);
  const [description, setDescription] = useState(row?.description || "");
  const save = (event: React.FormEvent) => { event.preventDefault(); void onSave({ type, heldItem, targetBlock, targetEntity, targetKind, target, effect, priority, description }); };
  return <form className="dialog-form" onSubmit={save}>
    <div className="form-grid">
      <Field label="Interaction type"><select value={type} onChange={event => setType(event.target.value)}><option value="item_on_block">Use an item on a block</option><option value="block_right_click">Right click a block</option><option value="item_on_entity">Use an item on an entity</option><option value="item_into_inventory">Insert an item into an inventory</option></select></Field>
      <Field label="Held item selector" help="Use id, tag, mod, name, or all selectors."><input value={heldItem} onChange={event => setHeldItem(event.target.value)} placeholder="tag:c:armors"/></Field>
      {type === "item_on_block" || type === "block_right_click" ? <Field label="Target block selector" help="The live registry holder is matched on the server." wide><input value={targetBlock} onChange={event => setTargetBlock(event.target.value)} placeholder="id:selling_bin:selling_bin" required/></Field> : null}
      {type === "item_on_entity" ? <Field label="Target entity selector" wide><input value={targetEntity} onChange={event => setTargetEntity(event.target.value)} placeholder="id:minecraft:villager" required/></Field> : null}
      {type === "item_into_inventory" ? <><Field label="Destination type"><select value={targetKind} onChange={event => setTargetKind(event.target.value)}><option value="block">Container block</option><option value="menu">Open menu</option><option value="inventory">Inventory owner</option></select></Field><Field label="Destination selector"><input value={target} onChange={event => setTarget(event.target.value)} placeholder="id:minecraft:chest" required/></Field><Field label="Result"><select value={effect} onChange={event => setEffect(event.target.value)}><option value="lock">Deny until stage is owned</option><option value="allow">Allow after stage is owned</option><option value="exclude">Always allow this match</option></select></Field><Field label="Priority"><input type="number" value={priority} onChange={event => setPriority(Number(event.target.value))}/></Field></> : null}
      <Field label="Description" wide><input value={description} onChange={event => setDescription(event.target.value)} placeholder="Optional operator note"/></Field>
    </div>
    {type === "item_on_block" ? <div className="rule-primer"><strong>Quick Selling Bin selectors</strong><p><Button type="button" tone="quiet" onClick={() => { setHeldItem("tag:c:armors"); setTargetBlock("id:selling_bin:selling_bin"); }}>Armor tag</Button> <Button type="button" tone="quiet" onClick={() => { setHeldItem("all:*"); setTargetBlock("id:selling_bin:selling_bin"); }}>All items</Button></p></div> : null}
    <footer className="dialog-actions"><Button type="button" tone="quiet" onClick={onCancel}>Cancel</Button><Button type="submit" tone="primary">Save interaction</Button></footer>
  </form>;
}

export function IntegrationsPanel({ stage }: { stage: StagePackage }) {
  const { boot, mutateFile, openDialog, closeDialog } = useEditor();
  const stageText = boot?.draft.files[stage.stagePath] || "";
  const rulesText = boot?.draft.files[stage.rulesPath] || "";
  const luckperms = useMemo(() => parseLuckPerms(stageText), [stageText]);
  const inbound = luckperms.inbound;
  const outbound = luckperms.outbound;
  const commands = useMemo(() => parseCommandPermissions(stageText), [stageText]);
  const interactions = useMemo(() => parseInteractions(rulesText), [rulesText]);
  const saveStage = (content: string, message: string) => mutateFile(stage.stagePath, content, message);
  const editInbound = (row?: InboundModel, index = -1) => openDialog({ title: row ? "Edit inbound LuckPerms mapping" : "Add inbound LuckPerms mapping", description: "Map a qualifying group or Boolean permission to this stage.", content: <InboundForm row={row} onCancel={closeDialog} onSave={async next => { const rows = inbound.map(entry => entry.sourceText || serializeInbound(entry)); if (index < 0) rows.push(serializeInbound(next)); else rows[index] = updateInboundBlock(rows[index], next); await saveStage(replaceInbound(stageText, rows), "Inbound LuckPerms mapping saved"); closeDialog(); }} />, width: "wide" });
  const editOutbound = (row?: OutboundModel, index = -1) => openDialog({ title: row ? "Edit outbound LuckPerms mapping" : "Add outbound LuckPerms mapping", description: "Project this stage to one transient group or permission contribution.", content: <OutboundForm row={row} onCancel={closeDialog} onSave={async next => { const rows = outbound.map(entry => entry.sourceText || serializeOutbound(entry)); if (index < 0) rows.push(serializeOutbound(next)); else rows[index] = updateOutboundBlock(rows[index], next); await saveStage(replaceOutbound(stageText, rows), "Outbound LuckPerms mapping saved"); closeDialog(); }} />, width: "wide" });
  const editCommand = (row?: CommandPermissionModel, index = -1) => openDialog({ title: row ? "Edit command gate" : "Add command gate", description: "Gate the actual parsed command literal while preserving native permissions.", content: <CommandForm row={row} onCancel={closeDialog} onSave={async next => { const rows = commands.map(entry => entry.sourceText || serializeCommandPermission(entry)); if (index < 0) rows.push(serializeCommandPermission(next)); else rows[index] = updateCommandPermissionBlock(rows[index], next); await saveStage(replaceCommandPermissions(stageText, rows), "Command permission gate saved"); closeDialog(); }} /> });
  const editInteraction = (row?: InteractionModel, index = -1) => openDialog({ title: row ? "Edit interaction" : "Add interaction", description: "Build a server checked item and target interaction.", content: <InteractionForm row={row} onCancel={closeDialog} onSave={async next => { const rows = interactions.map(entry => entry.sourceText || serializeInteraction(entry)); if (index < 0) await mutateFile(stage.rulesPath, appendRow(rulesText, serializeInteraction(next)), "Interaction saved"); else { rows[index] = updateInteractionBlock(rows[index], next); await mutateFile(stage.rulesPath, replaceInteractions(rulesText, rows), "Interaction saved"); } closeDialog(); }} />, width: "wide" });
  const remove = async (kind: "inbound" | "outbound" | "command" | "interaction", index: number) => {
    if (kind === "inbound") await saveStage(replaceInbound(stageText, inbound.filter((_, current) => current !== index).map(entry => entry.sourceText || serializeInbound(entry))), "Inbound LuckPerms mapping removed");
    if (kind === "outbound") await saveStage(replaceOutbound(stageText, outbound.filter((_, current) => current !== index).map(entry => entry.sourceText || serializeOutbound(entry))), "Outbound LuckPerms mapping removed");
    if (kind === "command") await saveStage(replaceCommandPermissions(stageText, commands.filter((_, current) => current !== index).map(entry => entry.sourceText || serializeCommandPermission(entry))), "Command permission gate removed");
    if (kind === "interaction") await mutateFile(stage.rulesPath, replaceInteractions(rulesText, interactions.filter((_, current) => current !== index).map(entry => entry.sourceText || serializeInteraction(entry))), "Interaction removed");
  };
  return <div className="stage-panel-stack">
    <Section title="Ownership and scope" description="Select personal, team, server, or inherited ownership. Counter scope stays independent." className="identity-section">
      <div className="form-grid"><Field label="Stage ownership" help="Personal uses team_stage = false. Inherited leaves both overrides absent."><select value={parseOwnership(stageText)} onChange={event => void saveStage(writeOwnership(stageText, event.target.value as Parameters<typeof writeOwnership>[1]), "Stage ownership saved")}><option value="inherit">Inherit the global team setting</option><option value="personal">Personal stage for each player</option><option value="team">Shared by the team</option><option value="server">Shared by the whole server</option></select></Field><div className="policy-summary"><strong>{parseOwnership(stageText) === "personal" ? "Profession style stage" : parseOwnership(stageText) === "server" ? "Server progression" : "Team progression"}</strong><p>Ownership is resolved before locks, triggers, rewards, and integrations.</p></div></div>
    </Section>
    <Section title="LuckPerms bridge" description="Use optional provider mappings with synchronized or permanent inbound retention.">
      <div className="form-grid"><Toggle label="Enable LuckPerms mappings" help="The provider remains optional and dormant when absent." checked={luckperms.enabled} onChange={value => void saveStage(writeLuckPermsSettings(stageText, value, luckperms.inboundMode), "LuckPerms bridge setting saved")}/><Field label="Inbound retention"><select value={luckperms.inboundMode} onChange={event => void saveStage(writeLuckPermsSettings(stageText, luckperms.enabled, event.target.value as "synchronized" | "permanent"), "LuckPerms retention saved")}><option value="synchronized">Remove access when rank is lost</option><option value="permanent">Keep access after rank is lost</option></select></Field></div>
      <div className="configured-list">{inbound.map((row, index) => <article key={row.id}><div><Badge tone="gold">Inbound</Badge><strong>{row.id}</strong><code>{[...row.groups, ...row.permissions].join(", ")}</code><small>{row.match} match. {Object.keys(row.contexts).length} context key{Object.keys(row.contexts).length === 1 ? "" : "s"}.</small></div><div className="rule-card-actions"><Button tone="quiet" onClick={() => editInbound(row, index)}>Edit</Button><Button tone="danger" onClick={() => void remove("inbound", index)}>Remove</Button></div></article>)}</div>
      <Button tone="primary" icon="plus" onClick={() => editInbound()}>Add inbound mapping</Button>
    </Section>
    <Section title="LuckPerms outputs" description="Grant a transient group or permission while this stage is effective."><div className="configured-list">{outbound.map((row, index) => <article key={row.id}><div><Badge tone="success">{row.kind}</Badge><strong>{row.id}</strong><code>{row.value}</code><small>{Object.keys(row.contexts).length} context key{Object.keys(row.contexts).length === 1 ? "" : "s"}.</small></div><div className="rule-card-actions"><Button tone="quiet" onClick={() => editOutbound(row, index)}>Edit</Button><Button tone="danger" onClick={() => void remove("outbound", index)}>Remove</Button></div></article>)}</div><Button tone="primary" icon="plus" onClick={() => editOutbound()}>Add outbound mapping</Button></Section>
    <Section title="Command gates" description="Gate actual commands and descendants while native permission checks still apply."><div className="configured-list">{commands.map((row, index) => <article key={row.id}><div><Badge tone="gold">{row.descendants ? "Tree" : "Literal"}</Badge><strong>{row.id}</strong><code>/{row.path}</code><small>{row.descendants ? "Includes child literals and arguments." : "Matches this literal and arguments."}</small></div><div className="rule-card-actions"><Button tone="quiet" onClick={() => editCommand(row, index)}>Edit</Button><Button tone="danger" onClick={() => void remove("command", index)}>Remove</Button></div></article>)}</div><Button tone="primary" icon="plus" onClick={() => editCommand()}>Add command gate</Button></Section>
    <Section title="Direct interactions" description="Build item_on_block, block_right_click, item_on_entity, and item_into_inventory definitions, including both Selling Bin selectors."><div className="configured-list">{interactions.map((row, index) => <article key={`${row.type}:${index}`}><div><Badge tone="gold">{row.type}</Badge><strong>{row.heldItem || "Any held item"}</strong><code>{row.targetBlock || row.targetEntity || row.target}</code><small>{row.description || "Server checked before the interaction continues."}</small></div><div className="rule-card-actions"><Button tone="quiet" onClick={() => editInteraction(row, index)}>Edit</Button><Button tone="danger" onClick={() => void remove("interaction", index)}>Remove</Button></div></article>)}</div>{interactions.length === 0 ? <EmptyState icon="rules" title="No direct interactions" description="Add a Selling Bin selector or another item and target interaction."/> : null}<Button tone="primary" icon="plus" onClick={() => editInteraction()}>Add interaction</Button></Section>
  </div>;
}
