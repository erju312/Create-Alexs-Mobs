package com.erju312.cam.init;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ModConfigs {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    private ModConfigs() {
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue crowCreateFiltersEnabled;

        public final ForgeConfigSpec.BooleanValue seagullRepellentEnabled;
        public final ForgeConfigSpec.DoubleValue seagullRepellentRange;
        public final ForgeConfigSpec.DoubleValue seagullRepellentCooldownSeconds;
        public final ForgeConfigSpec.DoubleValue seagullRepellentAirCostPercent;
        public final ForgeConfigSpec.IntValue seagullRepelDurationTicks;
        public final ForgeConfigSpec.DoubleValue seagullRepelTargetDistance;

        public final ForgeConfigSpec.BooleanValue gearBurstAnimationEnabled;
        public final ForgeConfigSpec.DoubleValue gearBurstSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue gearBurstReturnSeconds;

        public final ForgeConfigSpec.BooleanValue lavaBottleFuelEnabled;
        public final ForgeConfigSpec.DoubleValue lavaBottleBurnSeconds;
        public final ForgeConfigSpec.BooleanValue fishOilBottleFuelEnabled;
        public final ForgeConfigSpec.DoubleValue fishOilBottleBurnSeconds;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.push("crow");
            crowCreateFiltersEnabled = builder
                .comment("Allow crows to use Create filters displayed in item frames when depositing items.")
                .define("createFiltersEnabled", true);
            builder.pop();

            builder.push("seagullRepellentBacktank");
            seagullRepellentEnabled = builder
                .comment("Enable the seagull-repellent behavior on upgraded Create backtanks.")
                .define("enabled", true);
            seagullRepellentRange = builder
                .comment("Detection and repelling radius in blocks.")
                .defineInRange("range", 4.0D, 1.0D, 32.0D);
            seagullRepellentCooldownSeconds = builder
                .comment("Cooldown between air bursts in seconds.")
                .defineInRange("cooldownSeconds", 3.0D, 0.0D, 60.0D);
            seagullRepellentAirCostPercent = builder
                .comment("Percentage of the backtank's maximum air consumed by each burst.")
                .defineInRange("airCostPercent", 3.0D, 0.1D, 100.0D);
            seagullRepelDurationTicks = builder
                .comment("Number of ticks for which repelled seagulls keep receiving outward motion.")
                .defineInRange("repelDurationTicks", 14, 1, 200);
            seagullRepelTargetDistance = builder
                .comment("Distance in blocks used for the seagull's outward flight target.")
                .defineInRange("repelTargetDistance", 6.0D, 1.0D, 64.0D);
            builder.pop();

            builder.push("backtankGearAnimation");
            gearBurstAnimationEnabled = builder
                .comment("Make the two worn backtank gears accelerate after a repellent burst.")
                .define("enabled", true);
            gearBurstSpeedMultiplier = builder
                .comment("Initial gear speed multiplier immediately after a burst.")
                .defineInRange("speedMultiplier", 24.0D, 1.0D, 100.0D);
            gearBurstReturnSeconds = builder
                .comment("Time in seconds for the gears to return to Create's normal speed.")
                .defineInRange("returnSeconds", 0.4D, 0.05D, 5.0D);
            builder.pop();

            builder.push("blazeBurnerFuel");
            lavaBottleFuelEnabled = builder
                .comment("Allow Alex's Mobs lava bottles to fuel Blaze Burners.")
                .define("lavaBottleEnabled", true);
            lavaBottleBurnSeconds = builder
                .comment("Blaze Burner fuel duration of a lava bottle in seconds.")
                .defineInRange("lavaBottleBurnSeconds", 230.0D, 1.0D, 86400.0D);
            fishOilBottleFuelEnabled = builder
                .comment("Allow Alex's Mobs fish oil bottles to fuel Blaze Burners.")
                .define("fishOilBottleEnabled", true);
            fishOilBottleBurnSeconds = builder
                .comment("Blaze Burner fuel duration of a fish oil bottle in seconds.")
                .defineInRange("fishOilBottleBurnSeconds", 287.5D, 1.0D, 86400.0D);
            builder.pop();
        }
    }
}
