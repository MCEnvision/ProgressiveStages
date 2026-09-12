package com.enviouse.progressivestages.common.stage;

/** Source represented in an actor's effective stage view. */
public enum StageSourceKind {
    INDEPENDENT,
    LUCKPERMS_SYNCHRONIZED,
    LUCKPERMS_PERMANENT,
    TEMPORARY;

    /** Convert the persisted source label to the stable public source kind. */
    public static StageSourceKind fromLabel(String label) {
        if (label == null) return INDEPENDENT;
        return switch (label.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "luckperms_synchronized", "luckperms:synchronized" -> LUCKPERMS_SYNCHRONIZED;
            case "luckperms_permanent", "luckperms:permanent" -> LUCKPERMS_PERMANENT;
            case "temporary" -> TEMPORARY;
            default -> INDEPENDENT;
        };
    }
}
