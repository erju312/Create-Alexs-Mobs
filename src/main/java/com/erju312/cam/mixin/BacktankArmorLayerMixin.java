package com.erju312.cam.mixin;

import com.erju312.cam.event.BacktankSeagullRepellentHandler;
import com.erju312.cam.init.ModConfigs;
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

        if (!ModConfigs.COMMON.gearBurstAnimationEnabled.get()) {
            return renderTime;
        }

        ItemStack backtank = entity.getItemBySlot(EquipmentSlot.CHEST);
        CompoundTag tag = backtank.getTag();
        if (tag == null || !tag.contains(BacktankSeagullRepellentHandler.GEAR_BURST_TIME_TAG)) {
            return renderTime;
        }

        int burstCount = Math.max(0, tag.getInt(BacktankSeagullRepellentHandler.GEAR_BURST_COUNT_TAG));
        float durationTicks = (float) (ModConfigs.COMMON.gearBurstReturnSeconds.get() * 20.0D);
        float speedMultiplier = ModConfigs.COMMON.gearBurstSpeedMultiplier.get().floatValue();
        float extraDegreesPerTick = NORMAL_DEGREES_PER_TICK * (speedMultiplier - 1.0F);
        float extraDegreesPerBurst = extraDegreesPerTick * durationTicks * 0.5F;
        float elapsed = renderTime - tag.getLong(BacktankSeagullRepellentHandler.GEAR_BURST_TIME_TAG);
        float extraDegrees;
        if (elapsed >= durationTicks) {
            extraDegrees = burstCount * extraDegreesPerBurst;
        } else {
            int completedBursts = Math.max(0, burstCount - 1);
            float activeTicks = Math.max(0.0F, elapsed);
            float activeExtra = extraDegreesPerTick
                * (activeTicks - activeTicks * activeTicks / (2.0F * durationTicks));
            extraDegrees = completedBursts * extraDegreesPerBurst + activeExtra;
        }

        return renderTime + extraDegrees / NORMAL_DEGREES_PER_TICK;
    }
}
