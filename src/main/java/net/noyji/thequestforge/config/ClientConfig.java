package net.noyji.thequestforge.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Boolean> CAMERA_ZOOM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CAMERA_PARALLAX;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("general");

        CAMERA_ZOOM = BUILDER.define("camera_zoom", true);
        CAMERA_PARALLAX = BUILDER.define("camera_parallax", true);

        SPEC = BUILDER.build();
    }
}
