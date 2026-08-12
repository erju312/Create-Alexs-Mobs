package com.erju312.cam.mixin;

import com.erju312.cam.event.BacktankSeagullRepellentHandler;
import com.github.alexthe666.alexsmobs.entity.EntitySeagull;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.ai.SeagullAIStealFromPlayers", remap = false)
public class SeagullAIStealFromPlayersMixin {
    @Shadow(remap = false)
    @Final
    private EntitySeagull seagull;

    @Shadow(remap = false)
    private Player target;

    @Shadow(remap = false)
    private int fleeTime;

    @Inject(
        method = "m_8037_",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;m_41777_()Lnet/minecraft/world/item/ItemStack;",
            shift = At.Shift.BEFORE,
            remap = false
        ),
        cancellable = true,
        remap = false
    )
    private void cam$repelBeforeFoodIsTaken(CallbackInfo ci) {
        if (target == null || !BacktankSeagullRepellentHandler.tryInterceptSeagullTheft(target, seagull)) {
            return;
        }

        seagull.stealCooldown = 1500 + seagull.getRandom().nextInt(1500);
        seagull.aiItemFlag = false;
        target = null;
        fleeTime = 0;
        ci.cancel();
    }
}
