package com.erju312.cam.init;

import com.erju312.cam.CAMMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFluids {
    private static final ResourceLocation POTION_STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath("create", "fluid/potion_still");
    private static final ResourceLocation POTION_FLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath("create", "fluid/potion_flow");
    private static final ResourceLocation WATER_STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
    private static final ResourceLocation WATER_FLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");
    private static final int POISONOUS_ESSENCE_TINT = 0xFF74A685;
    private static final int KOMODO_SPIT_TINT = 0xFFD8DCA0;
    private static final int FISH_OIL_TINT = 0xFFFFD46A;

    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CAMMod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(ForgeRegistries.FLUIDS, CAMMod.MOD_ID);

    public static final FluidEntry POISONOUS_ESSENCE =
        register("poisonous_essence", POISONOUS_ESSENCE_TINT, POTION_STILL_TEXTURE, POTION_FLOWING_TEXTURE);
    public static final FluidEntry KOMODO_SPIT =
        register("komodo_spit", KOMODO_SPIT_TINT, WATER_STILL_TEXTURE, WATER_FLOWING_TEXTURE);
    public static final FluidEntry FISH_OIL =
        registerFishOil();

    private ModFluids() {
    }

    public static void init(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    private static FluidEntry register(String name, int tint, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        RegistryObject<FluidType> fluidType = FLUID_TYPES.register(
            name,
            () -> new ModFluidType(
                FluidType.Properties.create()
                    .descriptionId("fluid.cam." + name)
                    .density(1000)
                    .viscosity(1000),
                stillTexture,
                flowingTexture,
                tint
            )
        );

        ForgeFlowingFluid.Properties[] properties = new ForgeFlowingFluid.Properties[1];
        RegistryObject<FlowingFluid> source = FLUIDS.register(
            name,
            () -> new ForgeFlowingFluid.Source(properties[0])
        );
        RegistryObject<FlowingFluid> flowing = FLUIDS.register(
            "flowing_" + name,
            () -> new ForgeFlowingFluid.Flowing(properties[0])
        );
        properties[0] = new ForgeFlowingFluid.Properties(fluidType, source, flowing)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .tickRate(5);

        return new FluidEntry(fluidType, source, flowing);
    }

    private static FluidEntry registerFishOil() {
        RegistryObject<FluidType> fluidType = FLUID_TYPES.register(
            "fish_oil",
            () -> new ModFluidType(
                FluidType.Properties.create()
                    .descriptionId("fluid.cam.fish_oil")
                    .density(900)
                    .viscosity(1300),
                POTION_STILL_TEXTURE,
                POTION_FLOWING_TEXTURE,
                FISH_OIL_TINT
            )
        );

        ForgeFlowingFluid.Properties[] properties = new ForgeFlowingFluid.Properties[1];
        RegistryObject<FlowingFluid> source = FLUIDS.register(
            "fish_oil",
            () -> new ForgeFlowingFluid.Source(properties[0])
        );
        RegistryObject<FlowingFluid> flowing = FLUIDS.register(
            "flowing_fish_oil",
            () -> new ForgeFlowingFluid.Flowing(properties[0])
        );

        properties[0] = new ForgeFlowingFluid.Properties(fluidType, source, flowing)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .tickRate(5);

        return new FluidEntry(fluidType, source, flowing);
    }

    public static final class ModItems {
        public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CAMMod.MOD_ID);

        private ModItems() {
        }

        public static void init(IEventBus modBus) {
            ITEMS.register(modBus);
        }
    }

    public static final class ModBlocks {
        public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CAMMod.MOD_ID);

        private ModBlocks() {
        }

        public static void init(IEventBus modBus) {
            BLOCKS.register(modBus);
        }
    }

    public record FluidEntry(
        RegistryObject<FluidType> type,
        RegistryObject<FlowingFluid> source,
        RegistryObject<FlowingFluid> flowing
    ) {
    }
}
