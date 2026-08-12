package com.erju312.cam.client.event;

import com.erju312.cam.CAMMod;
import com.erju312.cam.event.BacktankSeagullRepellentHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CAMMod.MOD_ID, value = Dist.CLIENT)
public final class BacktankTooltipHandler {
    private BacktankTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!Screen.hasShiftDown() || !BacktankSeagullRepellentHandler.hasRepellentBacktank(event.getItemStack())) {
            return;
        }

        event.getToolTip().add(Component.translatable("tooltip.cam.seagull_backtank.title").withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.translatable("tooltip.cam.seagull_backtank.description").withStyle(ChatFormatting.GRAY));
    }
}
