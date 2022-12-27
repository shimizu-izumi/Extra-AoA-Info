package me.shimizuizumi.aoainfo;

import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.common.ForgeConfigSpec;

public class AoAInfoConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Boolean> DISPLAY_ADVANCED_TOOLTIPS;
    public static ForgeConfigSpec.ConfigValue<Boolean> DISPLAY_AMMO_HUD;

    static {
        BUILDER.push(I18n.get("gui.aoainfoconfig.displayAdvancedTooltips"));
        DISPLAY_ADVANCED_TOOLTIPS = BUILDER.comment("Set this to false to disable additional tooltip info")
                .define("displayAdvancedTooltips ", true);

        BUILDER.push(I18n.get("gui.aoainfoconfig.displayAmmoHud"));
        DISPLAY_AMMO_HUD = BUILDER.comment("Set this to false to disable the ammo HUD that shows when holding a weapon that uses ammo")
                .comment("Currently not working")
                .define("displayAmmoHud  ", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
