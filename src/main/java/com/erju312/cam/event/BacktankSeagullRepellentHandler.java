package com.erju312.cam.event;

import com.erju312.cam.CAMMod;
import com.erju312.cam.init.ModSounds;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.foundation.particle.AirParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Mod.EventBusSubscriber(modid = CAMMod.MOD_ID)
public final class BacktankSeagullRepellentHandler {
    public static final String REPELLENT_TAG = "CAMSeagullRepellent";
    public static final String GEAR_BURST_COUNT_TAG = "CAMGearBurstCount";
    public static final String GEAR_BURST_TIME_TAG = "CAMGearBurstTime";
    private static final String COOLDOWN_TAG = "CAMSeagullRepellentCooldown";
    private static final String REPEL_TICKS_TAG = "CAMSeagullRepelTicks";
    private static final String REPEL_X_TAG = "CAMSeagullRepelX";
    private static final String REPEL_Z_TAG = "CAMSeagullRepelZ";
    private static final ResourceLocation SEAGULL_ID = ResourceLocation.fromNamespaceAndPath("alexsmobs", "seagull");
    private static final String SEAGULL_STEAL_GOAL = "com.github.alexthe666.alexsmobs.entity.ai.SeagullAIStealFromPlayers";
    private static final int COOLDOWN_TICKS = 3 * 20;
    private static final int REPEL_TICKS = 14;
    private static final float AIR_COST_RATIO = 0.03F;
    private static final double RANGE = 4.0D;
    private static Field targetField;
    private static Field stealCooldownField;
    private static Method setFlyingMethod;

    private BacktankSeagullRepellentHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(COOLDOWN_TAG);
        if (cooldown > 0) {
            data.putInt(COOLDOWN_TAG, cooldown - 1);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !(entity instanceof Mob seagull) || !isSeagull(seagull)) {
            return;
        }

        continueRepelMotion(seagull);
    }

    public static boolean hasRepellentBacktank(ItemStack stack) {
        return !stack.isEmpty() && isCreateBacktank(stack) && stack.hasTag() && stack.getTag().getBoolean(REPELLENT_TAG);
    }

    private static boolean hasRepellentBacktank(Player player) {
        return hasRepellentBacktank(player.getItemBySlot(EquipmentSlot.CHEST));
    }

    private static ItemStack getRepellentBacktank(Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        return hasRepellentBacktank(stack) ? stack : ItemStack.EMPTY;
    }

    private static boolean isCreateBacktank(ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && "create".equals(itemId.getNamespace()) && itemId.getPath().endsWith("_backtank");
    }

    private static boolean isSeagull(Mob mob) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        return SEAGULL_ID.equals(entityId);
    }

    private static int getCooldown(Player player) {
        return player.getPersistentData().getInt(COOLDOWN_TAG);
    }

    public static boolean tryInterceptSeagullTheft(Player player, Mob thief) {
        if (!hasRepellentBacktank(player)
            || getCooldown(player) > 0
            || !thief.isAlive()
            || player.distanceToSqr(thief) > RANGE * RANGE
            || !repelNearbySeagulls(player)) {
            return false;
        }

        return true;
    }

    private static boolean repelNearbySeagulls(Player player) {
        ItemStack backtank = getRepellentBacktank(player);
        if (backtank.isEmpty() || !hasEnoughAir(backtank)) {
            return false;
        }

        List<Mob> seagulls = player.level()
            .getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RANGE), seagull ->
                isSeagull(seagull)
                    && seagull.isAlive()
                    && player.distanceToSqr(seagull) <= RANGE * RANGE
            );

        if (seagulls.isEmpty()) {
            return false;
        }

        BacktankUtil.consumeAir(player, backtank, getAirCost(backtank));
        markGearBurst(player, backtank);
        seagulls.forEach(seagull -> repelSeagull(player, seagull));
        player.getPersistentData().putInt(COOLDOWN_TAG, COOLDOWN_TICKS);
        spawnAirBurst(player);
        player.level().playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            ModSounds.AIR_BURST.get(),
            SoundSource.PLAYERS,
            1.0F,
            0.95F + player.getRandom().nextFloat() * 0.1F
        );
        return true;
    }

    private static void markGearBurst(Player player, ItemStack backtank) {
        CompoundTag tag = backtank.getOrCreateTag();
        int burstCount = Math.floorMod(tag.getInt(GEAR_BURST_COUNT_TAG) + 1, 45);
        tag.putInt(GEAR_BURST_COUNT_TAG, burstCount);
        tag.putLong(GEAR_BURST_TIME_TAG, player.level().getGameTime());
    }

    private static boolean hasEnoughAir(Player player) {
        ItemStack backtank = getRepellentBacktank(player);
        return !backtank.isEmpty() && hasEnoughAir(backtank);
    }

    private static boolean hasEnoughAir(ItemStack backtank) {
        return BacktankUtil.getAir(backtank) >= getAirCost(backtank);
    }

    private static float getAirCost(ItemStack backtank) {
        return Math.max(1.0F, BacktankUtil.maxAir(backtank) * AIR_COST_RATIO);
    }

    private static boolean isStealingFrom(Mob seagull, Player player) {
        for (WrappedGoal wrappedGoal : seagull.goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (!wrappedGoal.isRunning() || !SEAGULL_STEAL_GOAL.equals(goal.getClass().getName())) {
                continue;
            }
            if (getGoalTarget(goal) == player) {
                return true;
            }
        }
        return false;
    }

    private static Player getGoalTarget(Goal goal) {
        try {
            if (targetField == null) {
                targetField = goal.getClass().getDeclaredField("target");
                targetField.setAccessible(true);
            }
            Object target = targetField.get(goal);
            return target instanceof Player player ? player : null;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static void repelSeagull(Player player, Mob seagull) {
        stopStealingGoal(seagull, player);
        seagull.setTarget(null);
        seagull.getNavigation().stop();
        setSeagullStealCooldown(seagull);

        Vec3 direction = seagull.position().subtract(player.position());
        direction = new Vec3(direction.x, 0.0D, direction.z);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        direction = direction.normalize();

        startRepelMotion(seagull, direction);
    }

    private static void startRepelMotion(Mob seagull, Vec3 direction) {
        CompoundTag data = seagull.getPersistentData();
        data.putInt(REPEL_TICKS_TAG, REPEL_TICKS);
        data.putDouble(REPEL_X_TAG, direction.x);
        data.putDouble(REPEL_Z_TAG, direction.z);
        applyRepelMotion(seagull, direction, true);
    }

    private static void continueRepelMotion(Mob seagull) {
        CompoundTag data = seagull.getPersistentData();
        int ticks = data.getInt(REPEL_TICKS_TAG);
        if (ticks <= 0) {
            return;
        }

        Vec3 direction = new Vec3(data.getDouble(REPEL_X_TAG), 0.0D, data.getDouble(REPEL_Z_TAG));
        if (direction.lengthSqr() < 1.0E-4D) {
            data.remove(REPEL_TICKS_TAG);
            data.remove(REPEL_X_TAG);
            data.remove(REPEL_Z_TAG);
            return;
        }

        direction = direction.normalize();
        applyRepelMotion(seagull, direction, false);
        data.putInt(REPEL_TICKS_TAG, ticks - 1);
    }

    private static void applyRepelMotion(Mob seagull, Vec3 direction, boolean initialBurst) {
        forceSeagullFlying(seagull);
        seagull.setOnGround(false);
        seagull.fallDistance = 0.0F;

        double horizontalSpeed = initialBurst ? 0.9D : 0.62D;
        double verticalSpeed = initialBurst ? 0.32D : 0.12D;
        Vec3 motion = direction.scale(horizontalSpeed).add(0.0D, verticalSpeed, 0.0D);
        seagull.setDeltaMovement(motion);
        seagull.move(MoverType.SELF, direction.scale(initialBurst ? 0.55D : 0.38D).add(0.0D, initialBurst ? 0.16D : 0.06D, 0.0D));
        seagull.hasImpulse = true;
        seagull.hurtMarked = true;

        Vec3 wanted = seagull.position().add(direction.scale(6.0D)).add(0.0D, 1.2D, 0.0D);
        seagull.getMoveControl().setWantedPosition(wanted.x, wanted.y, wanted.z, 1.8D);
    }

    private static void forceSeagullFlying(Mob seagull) {
        try {
            if (setFlyingMethod == null) {
                setFlyingMethod = seagull.getClass().getDeclaredMethod("setFlying", boolean.class);
                setFlyingMethod.setAccessible(true);
            }
            setFlyingMethod.invoke(seagull, true);
        } catch (ReflectiveOperationException ignored) {
            // Motion still pushes the seagull even if Alex's Mobs changes this helper.
        }
    }

    private static void spawnAirBurst(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AirParticleData air = new AirParticleData(0.96F, 0.18F);
        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.72D, 0.0D);
        int count = 28;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0D * i) / count;
            double x = Math.cos(angle);
            double z = Math.sin(angle);
            double y = (player.getRandom().nextDouble() - 0.5D) * 0.12D;
            double radius = 0.35D + player.getRandom().nextDouble() * 0.18D;
            serverLevel.sendParticles(
                air,
                center.x + x * radius,
                center.y + y,
                center.z + z * radius,
                0,
                x * 0.55D,
                y,
                z * 0.55D,
                1.0D
            );
        }
    }

    private static void stopStealingGoal(Mob seagull, Player player) {
        for (WrappedGoal wrappedGoal : seagull.goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (wrappedGoal.isRunning() && SEAGULL_STEAL_GOAL.equals(goal.getClass().getName()) && (getGoalTarget(goal) == null || getGoalTarget(goal) == player)) {
                wrappedGoal.stop();
            }
        }
    }

    private static void setSeagullStealCooldown(Mob seagull) {
        try {
            if (stealCooldownField == null) {
                stealCooldownField = seagull.getClass().getDeclaredField("stealCooldown");
                stealCooldownField.setAccessible(true);
            }
            stealCooldownField.setInt(seagull, Math.max(stealCooldownField.getInt(seagull), COOLDOWN_TICKS));
        } catch (ReflectiveOperationException ignored) {
            // Stopping the running steal goal is enough; this just prevents an immediate retarget.
        }
    }
}
