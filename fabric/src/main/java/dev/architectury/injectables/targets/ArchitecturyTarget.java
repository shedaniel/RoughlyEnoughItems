package dev.architectury.injectables.targets;

/**
 * REI now consumes the published Fabric Architectury artifact directly.
 * Keep exposing the Fabric target explicitly in dev until Architectury's
 * normal target transform is restored for this migration branch.
 */
public final class ArchitecturyTarget {
    private ArchitecturyTarget() {
    }

    public static String getCurrentTarget() {
        return "fabric";
    }
}
