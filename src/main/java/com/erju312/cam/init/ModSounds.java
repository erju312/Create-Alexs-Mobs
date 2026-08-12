package com.erju312.cam.init;

import com.erju312.cam.CAMMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CAMMod.MOD_ID);

    public static final RegistryObject<SoundEvent> AIR_BURST = SOUND_EVENTS.register(
        "air_burst",
        () -> SoundEvent.createVariableRangeEvent(CAMMod.id("air_burst"))
    );

    private ModSounds() {
    }

    public static void init(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}
