package com.erju312.cam.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity", remap = false)
public class MechanicalMixerBlockEntityMixin {
    private static final Set<ResourceLocation> BLOCKED_AUTOMATIC_MIXER_OUTPUTS = Set.of(
        ResourceLocation.fromNamespaceAndPath("alexsmobs", "animal_dictionary"),
        ResourceLocation.fromNamespaceAndPath("alexsmobs", "komodo_spit_bottle"),
        ResourceLocation.fromNamespaceAndPath("alexsmobs", "fish_oil")
    );

    @Inject(method = "matchStaticFilters", at = @At("HEAD"), cancellable = true, remap = false)
    private <C extends Container> void cam$skipBlockedShapelessOutputsInMixer(Recipe<C> recipe,
        CallbackInfoReturnable<Boolean> cir) {
        if (!(recipe instanceof CraftingRecipe) || recipe instanceof IShapedRecipe<?>) {
            return;
        }

        ItemStack result = recipe.getResultItem(RegistryAccess.EMPTY);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(result.getItem());
        if (BLOCKED_AUTOMATIC_MIXER_OUTPUTS.contains(itemId)) {
            cir.setReturnValue(false);
        }
    }
}
