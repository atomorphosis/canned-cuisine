package atomorphosis.cannedcuisine.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<ProfileVisibility> PROFILE_VISIBILITY;
    public static final ModConfigSpec.EnumValue<TooltipActivation> TOOLTIP_ACTIVATION;

    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("ingredient_profiles");
        PROFILE_VISIBILITY = builder
                .comment("Controls which ingredient profiles may be shown in tooltips and the manual.")
                .defineEnum("profile_visibility", ProfileVisibility.DISCOVERED_ONLY);
        TOOLTIP_ACTIVATION = builder
                .comment("Controls whether eligible profile details require holding Shift.")
                .defineEnum("tooltip_activation", TooltipActivation.SHIFT);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public enum ProfileVisibility {
        DISCOVERED_ONLY,
        ALWAYS,
        OFF
    }

    public enum TooltipActivation {
        SHIFT,
        ALWAYS
    }
}
