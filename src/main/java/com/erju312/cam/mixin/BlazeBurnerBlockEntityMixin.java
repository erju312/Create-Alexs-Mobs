package com.erju312.cam.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity", remap = false)
public class BlazeBurnerBlockEntityMixin {
    private static final ResourceLocation LAVA_BOTTLE = ResourceLocation.fromNamespaceAndPath("alexsmobs", "lava_bottle");
    private static final ResourceLocation FISH_OIL_BOTTLE = ResourceLocation.fromNamespaceAndPath("alexsmobs", "fish_oil");
    private static Field isCreativeField;
    private static Field activeFuelField;
    private static Field remainingBurnTimeField;

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true, remap = false)
    private void cam$tryCustomBlazeBurnerFuel(ItemStack stack, boolean forceOverflow, boolean simulate,
        CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        int burnTicks = cam$getBurnTicks(itemId);
        if (burnTicks <= 0 || cam$isCreative(this)) {
            return;
        }

        try {
            cam$cacheFields(this);
            Object activeFuel = activeFuelField.get(this);
            Object normalFuel = cam$getFuelType(activeFuel, "NORMAL");
            Object specialFuel = cam$getFuelType(activeFuel, "SPECIAL");
            int remainingBurnTime = remainingBurnTimeField.getInt(this);

            if (activeFuel == specialFuel) {
                cir.setReturnValue(false);
                return;
            }

            if (activeFuel == normalFuel) {
                if (remainingBurnTime <= 500) {
                    burnTicks += remainingBurnTime;
                } else if (forceOverflow) {
                    burnTicks = Math.min(remainingBurnTime + burnTicks, Math.max(10000, burnTicks));
                } else {
                    cir.setReturnValue(false);
                    return;
                }
            }

            if (!simulate) {
                activeFuelField.set(this, normalFuel);
                remainingBurnTimeField.setInt(this, burnTicks);
                cam$invokeNoArg(this, "playSound");
                cam$invokeNoArg(this, "updateBlockState");
            }

            cir.setReturnValue(true);
        } catch (ReflectiveOperationException exception) {
            // Let Create handle the item normally if its Blaze Burner internals change.
        }
    }

    private static int cam$getBurnTicks(ResourceLocation itemId) {
        if (LAVA_BOTTLE.equals(itemId)) {
            return 230 * 20;
        }
        if (FISH_OIL_BOTTLE.equals(itemId)) {
            return 5750;
        }
        return 0;
    }

    private static boolean cam$isCreative(Object burner) {
        try {
            cam$cacheFields(burner);
            return isCreativeField.getBoolean(burner);
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    private static void cam$cacheFields(Object burner) throws NoSuchFieldException {
        Class<?> type = burner.getClass();
        if (isCreativeField == null) {
            isCreativeField = cam$getField(type, "isCreative");
            activeFuelField = cam$getField(type, "activeFuel");
            remainingBurnTimeField = cam$getField(type, "remainingBurnTime");
        }
    }

    private static Field cam$getField(Class<?> type, String name) throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static Object cam$getFuelType(Object activeFuel, String name) {
        return Enum.valueOf(activeFuel.getClass().asSubclass(Enum.class), name);
    }

    private static void cam$invokeNoArg(Object target, String name) throws ReflectiveOperationException {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }
}
