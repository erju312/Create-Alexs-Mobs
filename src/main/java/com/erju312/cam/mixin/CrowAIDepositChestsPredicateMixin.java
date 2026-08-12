package com.erju312.cam.mixin;

import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityCrow$AIDepositChests$1", remap = false)
public class CrowAIDepositChestsPredicateMixin {
    @Shadow(remap = false)
    @Final
    private EntityCrow val$this$0;

    @Inject(method = "apply(Lnet/minecraft/world/entity/decoration/ItemFrame;)Z", at = @At("RETURN"), cancellable = true, remap = false)
    private void cam$allowCreateFiltersOnFrames(ItemFrame frame, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }

        ItemStack filter = frame.getItem();
        ItemStack held = val$this$0.getMainHandItem();
        if (filter.isEmpty() || held.isEmpty()) {
            return;
        }

        FilterItemStack filterStack = FilterItemStack.of(filter);
        if (!filterStack.isFilterItem() || !filterStack.test(frame.level(), held)) {
            return;
        }

        BlockPos containerPos = frame.getPos().relative(frame.getDirection().getOpposite());
        BlockEntity blockEntity = frame.level().getBlockEntity(containerPos);
        if (blockEntity == null || !blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, frame.getDirection().getOpposite()).isPresent()) {
            return;
        }

        cir.setReturnValue(true);
    }
}
