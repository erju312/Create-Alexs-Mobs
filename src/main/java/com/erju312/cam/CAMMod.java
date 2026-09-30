package com.erju312.cam;

import com.erju312.cam.init.ModConfigs;
import com.erju312.cam.init.ModFluids;
import com.erju312.cam.init.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CAMMod.MOD_ID)
public class CAMMod {
    public static final String MOD_ID = "cam";

    public CAMMod() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfigs.COMMON_SPEC);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModFluids.init(modBus);
        ModFluids.ModItems.init(modBus);
        ModFluids.ModBlocks.init(modBus);
        ModSounds.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
