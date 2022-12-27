package me.shimizuizumi.aoainfo;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AoaInfo.MODID)
public class AoaInfo {
    public static final String MODID = "aoainfo", VERSION = "1.0.0";

    public AoaInfo() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(new AoAInfoEventHandler());
        MinecraftForge.EVENT_BUS.register(new AoAInfoHudRenderer());

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AoAInfoConfig.SPEC, "extra-aoa-info.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }
}
