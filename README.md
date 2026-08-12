# Create: Alex's Mobs

Create: Alex's Mobs is a small Forge addon that connects Alex's Mobs items, fluids, and mob behavior with Create automation.

The mod focuses on datapack-style recipes and compatibility polish: Create machines can process Alex's Mobs items, several mob fluids become automatable, and a few awkward JEI/generated-recipe cases are cleaned up.

## Requirements

- Minecraft 1.20.1
- Forge 47.4.10 or newer
- Create 6.0.8 or newer
- Alex's Mobs 1.22.9 or newer
- JEI is optional, but recommended for recipe viewing

Install this mod on both the client and the server.

## Added Fluids

- Poisonous Essence, using Create's potion fluid visuals.
- Komodo Dragon Spit, using a tinted water-style fluid.
- Fish Oil, using Create's potion fluid visuals.

## Create Recipes

### Automatic Brewing

These are Create mixing recipes that are shown in Create's Automatic Brewing JEI category.

- 1000 mB regular Poison potion + Rattlesnake Rattle + heat -> 1000 mB Poisonous Essence.
- 1000 mB Poisonous Essence + Cave Centipede Leg + heat -> 1000 mB regular Poison Resistance potion.
- 1000 mB regular Poison Resistance potion + 250 mB Komodo Dragon Spit + heat -> 1000 mB long Poison Resistance potion.
- 1000 mB Lava + Bone Serpent Tooth + heat -> 1000 mB regular Lava Vision potion.

### Emptying

- Poison Bottle -> Glass Bottle + 250 mB Poisonous Essence.
- Komodo Spit item -> 250 mB Komodo Dragon Spit.
- Komodo Spit Bottle -> Glass Bottle + 1000 mB Komodo Dragon Spit.
- Fish Oil bottle -> Glass Bottle + 250 mB Fish Oil.

### Filling

- Glass Bottle + 250 mB Poisonous Essence -> Poison Bottle.
- Glass Bottle + 1000 mB Komodo Dragon Spit -> Komodo Spit Bottle.
- Glass Bottle + 250 mB Fish Oil -> Fish Oil bottle.

### Other Processing

- Blobfish compacting -> 75 mB Fish Oil.
- Fish Bones crushing or milling -> 2 Bone Meal, 25% chance for 2 extra Bone Meal, and 25% chance for White Dye.
- Animal Dictionary is available through Mechanical Crafting.

## Behavior Tweaks

- Alex's Mobs Banana works as a Create Potato Cannon projectile with 3 damage.
- Alex's Mobs Poison Bottle, Lava Bottle, Fish Oil, Komodo Spit, and Komodo Spit Bottle stand upright on Create belts.
- Lava Bottle and Fish Oil bottle can fuel Blaze Burners and return Glass Bottles.
- Create's automatic mixer recipe generation is prevented from advertising impossible recipes for the Animal Dictionary, Komodo Spit Bottle, and Fish Oil bottle.
- Crows can use Create filters in item frames above containers when deciding where to deposit items.

## Seagull Repellent Backtanks

Craft a Create Copper Backtank or Netherite Backtank with a Precision Mechanism to add seagull repellent behavior.

When a seagull successfully starts stealing food from the player, the tagged backtank consumes about 3% of its max air, plays an air burst sound, cancels that theft, and pushes nearby seagulls away. The effect has a 3 second cooldown and appears in the Create creative tab.

Hold Shift over the crafted backtank to see the extra tooltip.

## Building From Source

The project expects Create, Alex's Mobs, and JEI jars to be available locally for compilation. They are not redistributed in this repository.

Use one of these options:

- Put the dependency jars in a local `libs/` folder.
- Create a `local.properties` file with `local_mods_dir=C:/path/to/your/minecraft/mods`.
- Set the `CAM_LOCAL_MODS_DIR` environment variable to your local mods folder.

Then run:

```powershell
.\gradlew.bat build
```

The built jar will be in `build/libs/`.

## Credits

This is an unofficial compatibility addon. Create and Alex's Mobs belong to their respective authors. This project references compatible runtime assets where possible and does not redistribute Create or Alex's Mobs jars.

## License

Apache-2.0
