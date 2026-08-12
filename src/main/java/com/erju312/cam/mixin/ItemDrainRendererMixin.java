package com.erju312.cam.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.fluids.drain.ItemDrainRenderer", remap = false)
public class ItemDrainRendererMixin {
    private static final ResourceLocation KOMODO_SPIT = ResourceLocation.fromNamespaceAndPath("alexsmobs", "komodo_spit");
    private static Method getHeldItemStackMethod;
    private static Field processingTicksField;

    private boolean cam$renderingKomodoSpitFluid;

    @Inject(
        method = "renderItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;m_115143_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"
        ),
        remap = false
    )
    private void cam$adjustItemWhileDraining(@Coerce Object drain, float partialTicks,
        PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        int processingTicks = cam$getProcessingTicks(drain);
        ResourceLocation heldItemId = cam$getHeldItemId(drain);
        if (!KOMODO_SPIT.equals(heldItemId) || processingTicks <= 0 || processingTicks >= 20) {
            return;
        }

        float scale = cam$getKomodoSpitDrainScale(processingTicks, partialTicks, 0.05F);
        if (scale >= 1.0F) {
            return;
        }

        poseStack.translate(0.0F, 0.5F * (1.0F - scale), 0.0F);
        poseStack.scale(scale, scale, scale);
    }

    @Inject(method = "renderFluid", at = @At("HEAD"), remap = false)
    private void cam$captureKomodoSpitFluidLine(@Coerce Object drain, float partialTicks,
        PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        cam$renderingKomodoSpitFluid = cam$isHeldKomodoSpit(drain);
    }

    @Inject(method = "renderFluid", at = @At("RETURN"), remap = false)
    private void cam$releaseKomodoSpitFluidLine(@Coerce Object drain, float partialTicks,
        PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        cam$renderingKomodoSpitFluid = false;
    }

    @ModifyArg(
        method = "renderFluid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/createmod/catnip/render/FluidRenderHelper;renderFluidBox(Ljava/lang/Object;FFFFFFLnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IZZ)V",
            ordinal = 1
        ),
        index = 5,
        remap = false
    )
    private float cam$shortenKomodoSpitFluidLineTop(float maxY) {
        return cam$renderingKomodoSpitFluid ? maxY * 0.93F : maxY;
    }

    private static boolean cam$isHeldKomodoSpit(Object drain) {
        return KOMODO_SPIT.equals(cam$getHeldItemId(drain));
    }

    private static ResourceLocation cam$getHeldItemId(Object drain) {
        ItemStack stack = cam$getHeldItemStack(drain);
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    private static float cam$getKomodoSpitDrainScale(int processingTicks, float partialTicks, float minScale) {
        if (processingTicks <= 0 || processingTicks >= 20) {
            return 1.0F;
        }

        float remainingTicks = processingTicks - partialTicks;
        float progress = 1.0F - Mth.clamp((remainingTicks - 5.0F) / 15.0F, 0.0F, 1.0F);
        if (progress < 0.01F) {
            return 1.0F;
        }

        float shrinkProgress = Mth.clamp((progress - 0.01F) / 0.99F, 0.0F, 1.0F);
        return Mth.lerp(shrinkProgress, 1.0F, minScale);
    }

    private static ItemStack cam$getHeldItemStack(Object drain) {
        try {
            if (getHeldItemStackMethod == null) {
                getHeldItemStackMethod = drain.getClass().getMethod("getHeldItemStack");
            }
            return (ItemStack) getHeldItemStackMethod.invoke(drain);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            return ItemStack.EMPTY;
        }
    }

    private static int cam$getProcessingTicks(Object drain) {
        try {
            if (processingTicksField == null) {
                processingTicksField = drain.getClass().getDeclaredField("processingTicks");
                processingTicksField.setAccessible(true);
            }
            return processingTicksField.getInt(drain);
        } catch (ReflectiveOperationException exception) {
            return 0;
        }
    }
}
