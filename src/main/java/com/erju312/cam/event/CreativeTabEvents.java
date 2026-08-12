package com.erju312.cam.event;

import com.erju312.cam.CAMMod;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CAMMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CreativeTabEvents {
    private static final ResourceKey<CreativeModeTab> CREATE_BASE_TAB =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("create", "base"));
    private static final ResourceLocation COPPER_BACKTANK = ResourceLocation.fromNamespaceAndPath("create", "copper_backtank");
    private static final ResourceLocation NETHERITE_BACKTANK = ResourceLocation.fromNamespaceAndPath("create", "netherite_backtank");

    private CreativeTabEvents() {
    }

    @SubscribeEvent
    public static void addRepellentBacktanks(BuildCreativeModeTabContentsEvent event) {
        if (!CREATE_BASE_TAB.equals(event.getTabKey())) {
            return;
        }

        acceptRepellentBacktank(event, COPPER_BACKTANK);
        acceptRepellentBacktank(event, NETHERITE_BACKTANK);
    }

    private static void acceptRepellentBacktank(BuildCreativeModeTabContentsEvent event, ResourceLocation itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            return;
        }

        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putBoolean(BacktankSeagullRepellentHandler.REPELLENT_TAG, true);
        stack.getOrCreateTag().putFloat("Air", BacktankUtil.maxAir(stack));
        event.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
