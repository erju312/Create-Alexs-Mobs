package com.erju312.cam.mixin;

import com.erju312.cam.event.BacktankSeagullRepellentHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.equipment.armor.BacktankArmorLayer", remap = false)
public class BacktankArmorLayerMixin {
    private static final float NORMAL_DEGREES_PER_TICK = 2.0F;
    private static final float BURST_SPEED_MULTIPLIER = 24.0F;
    private static final float BURST_DURATION_TICKS = 8.0F;
    private static final float EXTRA_DEGREES_PER_TICK =
        NORMAL_DEGREES_PER_TICK * (BURST_SPEED_MULTIPLIER - 1.0F);
    private static final float EXTRA_DEGREES_PER_BURST =
        EXTRA_DEGREES_PER_TICK * BURST_DURATION_TICKS * 0.5F;
    private static final int BURST_COUNT_PERIOD = 45;
    private static final ThreadLocal<LivingEntity> CAM_RENDERED_ENTITY = new ThreadLocal<>();

    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void cam$captureRenderedEntity(PoseStack poseStack, MultiBufferSource buffer, int light,
        LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
        float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        CAM_RENDERED_ENTITY.set(entity);
    }

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private void cam$releaseRenderedEntity(PoseStack poseStack, MultiBufferSource buffer, int light,
        LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
        float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        CAM_RENDERED_ENTITY.remove();
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/createmod/catnip/animation/AnimationTickHolder;getRenderTime(Lnet/minecraft/world/level/LevelAccessor;)F"
        ),
        remap = false
    )
    private float cam$accelerateGearsAfterBurst(LevelAccessor level) {
        LivingEntity entity = CAM_RENDERED_ENTITY.get();
        Minecraft minecraft = Minecraft.getInstance();
        float renderTime = (entity == null ? minecraft.level.getGameTime() : entity.level().getGameTime())
            + minecraft.getFrameTime();
        if (entity == null) {
            return renderTime;
        }

        ItemStack backtank = entity.getItemBySlot(EquipmentSlot.CHEST);
        CompoundTag tag = backtank.getTag();
        if (tag == null || !tag.contains(BacktankSeagullRepellentHandler.GEAR_BURST_TIME_TAG)) {
            return renderTime;
        }

        int burstCount = Math.floorMod(
            tag.getInt(BacktankSeagullRepellentHandler.GEAR_BURST_COUNT_TAG),
            BURST_COUNT_PERIOD
        );
        float elapsed = renderTime - tag.getLong(BacktankSeagullRepellentHandler.GEAR_BURST_TIME_TAG);
        float extraDegrees;
        if (elapsed >= BURST_DURATION_TICKS) {
            extraDegrees = burstCount * EXTRA_DEGREES_PER_BURST;
        } else {
            int completedBursts = Math.floorMod(burstCount - 1, BURST_COUNT_PERIOD);
            float activeTicks = Math.max(0.0F, elapsed);
            float activeExtra = EXTRA_DEGREES_PER_TICK
                * (activeTicks - activeTicks * activeTicks / (2.0F * BURST_DURATION_TICKS));
            extraDegrees = completedBursts * EXTRA_DEGREES_PER_BURST + activeExtra;
        }

        return renderTime + extraDegrees / NORMAL_DEGREES_PER_TICK;
    }
}
