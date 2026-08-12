package com.erju312.cam.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.processing.burner.BlazeBurnerBlock", remap = false)
public class BlazeBurnerBlockMixin {
    private static final ResourceLocation LAVA_BOTTLE = ResourceLocation.fromNamespaceAndPath("alexsmobs", "lava_bottle");
    private static final ResourceLocation FISH_OIL_BOTTLE = ResourceLocation.fromNamespaceAndPath("alexsmobs", "fish_oil");
    private static Method tryUpdateFuelMethod;
    private static Method spawnParticleBurstMethod;

    @Inject(method = "tryInsert", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cam$insertCustomFuelWithContainer(BlockState state, Level level, BlockPos pos, ItemStack stack,
        boolean creative, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack container = cam$getContainer(stack);
        if (container.isEmpty()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || !cam$tryUpdateFuel(blockEntity, stack, forceOverflow, simulate)) {
            cir.setReturnValue(InteractionResultHolder.pass(ItemStack.EMPTY));
            return;
        }

        if (!creative && !simulate && !level.isClientSide) {
            stack.shrink(1);
        }

        if (!simulate && level.isClientSide) {
            cam$spawnParticleBurst(blockEntity);
        }

        cir.setReturnValue(InteractionResultHolder.success(creative ? ItemStack.EMPTY : container));
    }

    private static ItemStack cam$getContainer(ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (LAVA_BOTTLE.equals(itemId) || FISH_OIL_BOTTLE.equals(itemId)) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        return ItemStack.EMPTY;
    }

    private static boolean cam$tryUpdateFuel(BlockEntity blockEntity, ItemStack stack, boolean forceOverflow, boolean simulate) {
        try {
            if (tryUpdateFuelMethod == null) {
                tryUpdateFuelMethod = blockEntity.getClass().getDeclaredMethod("tryUpdateFuel", ItemStack.class, boolean.class, boolean.class);
                tryUpdateFuelMethod.setAccessible(true);
            }
            return (boolean) tryUpdateFuelMethod.invoke(blockEntity, stack, forceOverflow, simulate);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            return false;
        }
    }

    private static void cam$spawnParticleBurst(BlockEntity blockEntity) {
        try {
            if (spawnParticleBurstMethod == null) {
                spawnParticleBurstMethod = blockEntity.getClass().getDeclaredMethod("spawnParticleBurst", boolean.class);
                spawnParticleBurstMethod.setAccessible(true);
            }
            spawnParticleBurstMethod.invoke(blockEntity, false);
        } catch (ReflectiveOperationException ignored) {
            // Fuel still works if Create changes the client-only particle helper.
        }
    }
}
