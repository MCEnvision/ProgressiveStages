import { parseOwnership } from "../lib/integrations";
import { useEditor } from "../store/EditorContext";
import type { StageCapabilities } from "../types";

export function ownershipDescription(ownership: ReturnType<typeof parseOwnership>, teamMode?: string,
                                     provider?: StageCapabilities["teamProvider"]): string {
  if (ownership === "server") return "Shared by the whole server, independently of FTB Teams.";
  if (ownership === "personal") return "Owned separately by each player, independently of team progression.";
  if (ownership === "inherit" && !teamMode) return "Inherits the server's global sharing setting. Validate to check current provider availability.";
  if (ownership === "inherit" && teamMode?.toLowerCase() !== "ftb_teams") return "Uses solo ownership under the current server's global sharing setting.";
  if (!provider) return "Requests team sharing. Validate to check current provider availability.";
  if (provider !== "READY") return `Uses solo fallback because FTB Teams is ${provider === "ABSENT" ? "unavailable" : "disabled"} on the current server.`;
  return "Shared with the player's current FTB team. Players without a team use solo fallback.";
}

export function OwnershipFeedback({ source }: { source: string }) {
  const { boot, validationResult } = useEditor();
  return <p className="muted" aria-live="polite">{ownershipDescription(parseOwnership(source),
    validationResult?.teamMode ?? boot?.teamMode, validationResult?.stageCapabilities?.teamProvider)}</p>;
}

export function ProviderFeedback() {
  const { validationResult } = useEditor();
  const state = validationResult?.stageCapabilities?.luckPerms;
  const labels: Record<StageCapabilities["luckPerms"], string> = {
    ABSENT: "LuckPerms is not installed. Mappings stay editable and dormant.",
    DISABLED: "LuckPerms integration is disabled. Mappings stay editable and dormant.",
    STARTING: "LuckPerms is starting. Validate again when it is ready.",
    READY: "LuckPerms is ready. Group availability is checked for this draft; validate again to refresh pending lookups.",
    FAILED: "LuckPerms is unavailable after a provider failure. Mappings stay editable and dormant."
  };
  return <p className="muted" aria-live="polite">{state ? labels[state]
    : "Validate this draft to check LuckPerms and referenced groups. Command gates work independently of LuckPerms."}</p>;
}
