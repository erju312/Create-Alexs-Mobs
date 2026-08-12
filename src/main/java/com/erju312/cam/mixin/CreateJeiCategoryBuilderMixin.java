package com.erju312.cam.mixin;

import com.erju312.cam.CAMMod;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "com.simibubi.create.compat.jei.CreateJEI$CategoryBuilder", remap = false)
public class CreateJeiCategoryBuilderMixin {
    private static final Set<ResourceLocation> AUTOMATIC_BREWING_RECIPE_IDS = Set.of(
        CAMMod.id("automatic_brewing/poisonous_essence"),
        CAMMod.id("automatic_brewing/poison_resistance"),
        CAMMod.id("automatic_brewing/lava_vision"),
        CAMMod.id("automatic_brewing/long_poison_resistance")
    );
    private static final Set<ResourceLocation> BLOCKED_AUTOMATIC_MIXER_OUTPUTS = Set.of(
        ResourceLocation.fromNamespaceAndPath("alexsmobs", "animal_dictionary"),
        ResourceLocation.fromNamespaceAndPath("alexsmobs", "komodo_spit_bottle"),
        ResourceLocation.fromNamespaceAndPath("alexsmobs", "fish_oil")
    );

    @Shadow
    @Final
    private List<Consumer<List<Recipe<?>>>> recipeListConsumers;

    @Inject(method = "build", at = @At("HEAD"), remap = false)
    private void cam$routeDatapackBrewingRecipesToJei(String name,
        CreateRecipeCategory.Factory<?> factory,
        CallbackInfoReturnable<CreateRecipeCategory<?>> cir) {
        if ("automatic_brewing".equals(name)) {
            recipeListConsumers.add(recipes ->
                CreateJEI.consumeTypedRecipes(recipe -> {
                    if (cam$isAutomaticBrewingRecipe(recipe)) {
                        recipes.add(recipe);
                    }
                }, AllRecipeTypes.MIXING.getType())
            );
        }

        if ("mixing".equals(name)) {
            recipeListConsumers.add(recipes -> recipes.removeIf(CreateJeiCategoryBuilderMixin::cam$isAutomaticBrewingRecipe));
        }

        if ("mixing".equals(name) || "automatic_shapeless".equals(name)) {
            recipeListConsumers.add(recipes -> recipes.removeIf(CreateJeiCategoryBuilderMixin::cam$hasBlockedAutomaticMixerOutput));
        }
    }

    private static boolean cam$isAutomaticBrewingRecipe(Recipe<?> recipe) {
        return AUTOMATIC_BREWING_RECIPE_IDS.contains(recipe.getId());
    }

    private static boolean cam$hasBlockedAutomaticMixerOutput(Recipe<?> recipe) {
        if (BLOCKED_AUTOMATIC_MIXER_OUTPUTS.contains(recipe.getId())) {
            return true;
        }

        ItemStack result = recipe.getResultItem(RegistryAccess.EMPTY);
        return BLOCKED_AUTOMATIC_MIXER_OUTPUTS.contains(ForgeRegistries.ITEMS.getKey(result.getItem()));
    }
}
