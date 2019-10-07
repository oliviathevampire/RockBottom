package de.ellpeck.rockbottom;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.Registries;
import de.ellpeck.rockbottom.api.construction.resource.ResUseInfo;
import de.ellpeck.rockbottom.api.construction.smelting.FuelInput;
import de.ellpeck.rockbottom.api.effect.BasicEffect;
import de.ellpeck.rockbottom.api.item.*;
import de.ellpeck.rockbottom.api.tile.BasicTile;
import de.ellpeck.rockbottom.api.tile.PlatformTile;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.gen.biome.level.BiomeLevel;
import de.ellpeck.rockbottom.api.world.gen.biome.level.BiomeLevelBasic;
import de.ellpeck.rockbottom.construction.category.*;
import de.ellpeck.rockbottom.construction.criteria.CriteriaBreakTile;
import de.ellpeck.rockbottom.construction.criteria.CriteriaPickupItem;
import de.ellpeck.rockbottom.construction.criteria.CriteriaReachDepth;
import de.ellpeck.rockbottom.item.*;
import de.ellpeck.rockbottom.world.entity.*;
import de.ellpeck.rockbottom.world.entity.player.knowledge.RecipeInformation;
import de.ellpeck.rockbottom.world.gen.WorldGenBiomes;
import de.ellpeck.rockbottom.world.gen.WorldGenHeights;
import de.ellpeck.rockbottom.world.gen.biome.*;
import de.ellpeck.rockbottom.world.gen.feature.*;
import de.ellpeck.rockbottom.world.gen.ore.WorldGenBronze;
import de.ellpeck.rockbottom.world.gen.ore.WorldGenCoal;
import de.ellpeck.rockbottom.world.gen.ore.WorldGenCopper;
import de.ellpeck.rockbottom.world.tile.*;

public final class ContentRegistry {

    public static void init() {
        new AirTile().register();
        new SoilTile().register();
        new GrassTile().register();
        new BasicTile(ResourceName.intern("stone")).setChiselable().register();
        new BasicTile(ResourceName.intern("compressed_stone")).register();
        new GrassTuftTile().register();
        new LogTile().register();
        new LeavesTile().register();
        new FlowerTile().register();
        new PebblesTile().register();
        new FallingTile(ResourceName.intern("sand")).register();
        new BasicTile(ResourceName.intern("sandstone")).setChiselable().register();
        new FallingTile(ResourceName.intern("red_sand")).register();
        new BasicTile(ResourceName.intern("red_sandstone")).setChiselable().register();
        new FallingTile(ResourceName.intern("white_sand")).register();
        new BasicTile(ResourceName.intern("white_sandstone")).setChiselable().register();
        new FallingTile(ResourceName.intern("gravel")).register();
        new OreMaterialTile(ResourceName.intern("coal")).register();
        new TorchTile(ResourceName.intern("torch")).register();
        new SnowTile().register();
        new LadderTile().register();
        new ChestTile().register();
        new SignTile().register();
        new SaplingTile().register();
        new WaterTile().register();
        new WoodBoardsTile().setChiselable().register();
        new WoodDoorTile(ResourceName.intern("wood_door")).register();
        new WoodDoorTile(ResourceName.intern("wood_door_old")).register();
        new RemainsGooTile().register();
        new GrassTorchTile().register();
        new CopperTile().register();
        new BronzeTile().register();
        new SpinningWheelTile().register();
        new SimpleFurnaceTile().register();
        new ConstructionTableTile().register();
        new SmithingTableTile().register();
        new BookshelfTile().register();
        new CaveMushroomTile().register();
        new StardropTile().register();
        new LampTile(ResourceName.intern("lamp_iron")).register();
        new MortarTile().register();
        new TilledSoilTile().register();
        new CornTile().register();
        new CottonTile().register();
        new GlassTile().register();
        new PlatformTile().register();
        new BasicTile(ResourceName.intern("netherrack")).register();
        new BedTile(ResourceName.intern("red_bed")).register();

        new ItemTool(ResourceName.intern("brittle_pickaxe"), 1.5F, 50, ToolProperty.PICKAXE, 1).register();
        new ItemTool(ResourceName.intern("brittle_axe"), 1.25F, 50, ToolProperty.AXE, 1).register();
        new ItemTool(ResourceName.intern("brittle_shovel"), 1.25F, 50, ToolProperty.SHOVEL, 1).register();
        new ItemSword(ResourceName.intern("brittle_sword"), 50, 4, 10, 1.5D, 0.25D).register();
        new ItemBasicTool(ResourceName.intern("wrench"), 100).register();
        new ItemBasicTool(ResourceName.intern("saw"), 100).register();
        new ItemBasicTool(ResourceName.intern("hammer"), 100).register();
        new ItemBasicTool(ResourceName.intern("mallet"), 100).register();
        new ItemChisel(ResourceName.intern("chisel"), 1, 100, ToolProperty.CHISEL, 1).register();
        new ItemFirework().register();
        new ItemStartNote().register();
        new ItemBasic(ResourceName.intern("plant_fiber")).register();
        new ItemBasic(ResourceName.intern("yarn")).register();
        new ItemTwig().register();
        new ItemBasic(ResourceName.intern("stick")).register();
        new ItemTool(ResourceName.intern("stone_pickaxe"), 2.5F, 120, ToolProperty.PICKAXE, 5).register();
        new ItemTool(ResourceName.intern("stone_axe"), 1.5F, 120, ToolProperty.AXE, 5).register();
        new ItemTool(ResourceName.intern("stone_shovel"), 1.5F, 120, ToolProperty.SHOVEL, 5).register();
        new ItemSword(ResourceName.intern("stone_sword"), 120, 8, 20, 2D, 0.5D).register();
        new ItemCopperCanister().register();
        new ItemBronzeCanister().register();
        new ItemTool(ResourceName.intern("super_pickaxe"), Float.MAX_VALUE, Short.MAX_VALUE, ToolProperty.PICKAXE, Integer.MAX_VALUE).addToolProperty(ToolProperty.AXE, Integer.MAX_VALUE).addToolProperty(ToolProperty.SHOVEL, Integer.MAX_VALUE).register();
        new ItemBasic(ResourceName.intern("copper_ingot")).register();
        new ItemTool(ResourceName.intern("copper_pickaxe"), 4F, 350, ToolProperty.PICKAXE, 10).register();
        new ItemTool(ResourceName.intern("copper_axe"), 2F, 350, ToolProperty.AXE, 10).register();
        new ItemTool(ResourceName.intern("copper_shovel"), 2F, 350, ToolProperty.SHOVEL, 10).register();
        new ItemSword(ResourceName.intern("copper_sword"), 350, 12, 30, 2D, 0.35D).register();

        new ItemBasic(ResourceName.intern("bronze_ingot")).register();
        new ItemTool(ResourceName.intern("bronze_pickaxe"), 4F, 350, ToolProperty.PICKAXE, 10).register();
        new ItemTool(ResourceName.intern("bronze_axe"), 2F, 350, ToolProperty.AXE, 10).register();
        new ItemTool(ResourceName.intern("bronze_shovel"), 2F, 350, ToolProperty.SHOVEL, 10).register();
        new ItemSword(ResourceName.intern("bronze_sword"), 350, 12, 30, 2D, 0.35D).register();

        new ItemRecipeNote().register();
        new ItemBowl().register();
        new ItemTool(ResourceName.intern("pestle"), 1F, 64, ToolProperty.PESTLE, 1).register();
        new ItemMush().register();
        new ItemBoomerang(ResourceName.intern("wood_boomerang"), 50, 4, 0.25, 8).register();
        new ItemTool(ResourceName.intern("simple_hoe"), 1F, 50, ToolProperty.HOE, 1).register();

        BiomeLevel sky = new BiomeLevelBasic(ResourceName.intern("sky"), 15, Integer.MAX_VALUE, false, 0).register();
        BiomeLevel surface = new BiomeLevelBasic(ResourceName.intern("surface"), -10, 15, true, 1000).register();
        BiomeLevel underground = new BiomeLevelBasic(ResourceName.intern("underground"), -40, -10, false, 500).register();
        BiomeLevel deepUnderground = new BiomeLevelBasic(ResourceName.intern("deep_underground"), -90, -40, false, 500).register();
        BiomeLevel hell = new BiomeLevelBasic(ResourceName.intern("hell"), -110, -90, false, 500).register();

        new BiomeSky(ResourceName.intern("sky"), 1000, sky).register();
        new BiomeGrassland(ResourceName.intern("grassland"), 1000, surface).register();
        new BiomeDesert(ResourceName.intern("desert"), 800, surface).register();
        new BiomeRedDesert(ResourceName.intern("red_desert"), 800, surface).register();
        new BiomeWhiteDesert(ResourceName.intern("white_desert"), 800, surface).register();
        new BiomeUnderground(ResourceName.intern("underground"), 1000, underground).register();
        new BiomeDeepUnderground(ResourceName.intern("deep_underground"), 1000, deepUnderground).register();
        new BiomeHell(ResourceName.intern("hell"), 1000, hell).register();
        new BiomeColdGrassland(ResourceName.intern("cold_grassland")).register();

        Registries.ENTITY_REGISTRY.register(ResourceName.intern("item"), EntityItem.class);
        Registries.ENTITY_REGISTRY.register(ResourceName.intern("falling"), EntityFalling.class);
        Registries.ENTITY_REGISTRY.register(ResourceName.intern("firework"), EntityFirework.class);
        Registries.ENTITY_REGISTRY.register(ResourceName.intern("slime"), EntitySlime.class);
        Registries.ENTITY_REGISTRY.register(ResourceName.intern("boomerang"), EntityBoomerang.class);

        Registries.WORLD_GENERATORS.register(WorldGenBiomes.ID, WorldGenBiomes.class);
        Registries.WORLD_GENERATORS.register(WorldGenHeights.ID, WorldGenHeights.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("grass"), WorldGenGrass.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("trees"), WorldGenTrees.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("flowers"), WorldGenFlowers.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("pebbles"), WorldGenPebbles.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("coal"), WorldGenCoal.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("start_hut"), WorldGenStartHut.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("copper"), WorldGenCopper.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("bronze"), WorldGenBronze.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("caves"), WorldGenCaves.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("cave_mushrooms"), WorldGenCaveMushrooms.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("stardrops"), WorldGenStardrops.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("lakes"), WorldGenLakes.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("corn"), WorldGenCorn.class);
        Registries.WORLD_GENERATORS.register(ResourceName.intern("cotton"), WorldGenCotton.class);

        Registries.INFORMATION_REGISTRY.register(RecipeInformation.REG_NAME, RecipeInformation.class);

        new CriteriaBreakTile().register();
        new CriteriaPickupItem().register();
        new CriteriaReachDepth().register();

        new BasicEffect(ResourceName.intern("speed"), false, false, 36000, 10).register();
        new BasicEffect(ResourceName.intern("jump_height"), false, false, 36000, 20).register();
        new BasicEffect(ResourceName.intern("range"), false, false, 36000, 10).register();
        new BasicEffect(ResourceName.intern("pickup_range"), false, false, 36000, 10).register();

        new FuelInput(new ResUseInfo(GameContent.RES_COAL), 1000).register();
        new FuelInput(new ResUseInfo(GameContent.RES_WOOD_RAW), 300).register();
        new FuelInput(new ResUseInfo(GameContent.RES_WOOD_PROCESSED), 100).register();
        new FuelInput(new ResUseInfo(GameContent.RES_PLANT_FIBER), 20).register();
        new FuelInput(new ResUseInfo(GameContent.RES_STICK), 20).register();

        EntitySlime.SPAWN_BEHAVIOR.register();

        CategoryManualConstruction.INSTANCE.register();
        new CategoryConstructionTable().register();
        new CategorySmithingTable().register();
        new CategoryMortar().register();
        new CategorySmelting().register();
    }
}
