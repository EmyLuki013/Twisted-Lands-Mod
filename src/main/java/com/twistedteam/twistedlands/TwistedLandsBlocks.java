package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.block.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

public class TwistedLandsBlocks {

    public static Block register(Block block, String id) {
        return register(block, id, new FabricItemSettings());
    }

    public static Block register(Block block, String id, FabricItemSettings itemSettings) {
        Registry.register(BuiltInRegistries.BLOCK, TwistedLands.id(id), block);
        Registry.register(BuiltInRegistries.ITEM, TwistedLands.id(id), new BlockItem(block, itemSettings));

        return block;
    }

    // Blocos da Backroom: som de lã (cloth no Bedrock), 1s pra quebrar e cor de mapa próxima do #B69834 do addon.
    private static BlockBehaviour.Properties backroom() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .strength(1.0f)
                .sound(SoundType.WOOL);
    }

    public static final Block AMETHYST_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "amethyst_ore"
    );

    public static final Block DEEPSLATE_AMETHYST_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "deepslate_amethyst_ore"
    );

    public static final Block AMETHYST_BLOCK = register(
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)),
            "amethyst_block"
    );

    public static final Block RUBY_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "ruby_ore"
    );

    public static final Block DEEPSLATE_RUBY_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "deepslate_ruby_ore"
    );

    public static final Block RUBY_BLOCK = register(
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)),
            "ruby_block"
    );

    public static final Block BACKROOMS_WALLPAPER = register(
            new Block(backroom()),
            "backrooms_wallpaper"
    );

    public static final Block WALLPAPER_PLANKS = register(
            new Block(backroom()),
            "wallpaper_planks"
    );

    public static final Block WALLPAPER_SMOOTH = register(
            new Block(backroom()),
            "wallpaper_smooth"
    );

    public static final Block WALLPAPER_SMOOTH_LAMP = register(
            new BackroomLampBlock(backroom().lightLevel(state -> 15)),
            "wallpaper_smooth_lamp"
    );

    public static final Block WALLPAPER = register(
            new WallpaperBlock(backroom().noOcclusion()),
            "wallpaper"
    );

    public static final Block WALLPAPER_CORNER = register(
            new WallpaperCornerBlock(backroom().noOcclusion()),
            "wallpaper_corner"
    );

    public static final Block WALLPAPER_MOSAIC = register(
            new Block(backroom()),
            "wallpaper_mosaic"
    );

    // As versões "dark" das lâmpadas são a lâmpada apagada: no addon não têm luz nem zumbido.
    public static final Block WALLPAPER_SMOOTH_DARK = register(
            new Block(backroom()),
            "wallpaper_smooth_dark"
    );

    public static final Block WALLPAPER_SMOOTH_DARK_LAMP = register(
            new Block(backroom()),
            "wallpaper_smooth_dark_lamp"
    );

    public static final Block WALLPAPER_TILED = register(
            new Block(backroom()),
            "wallpaper_tiled"
    );

    public static final Block WALLPAPER_TILED_DARK = register(
            new Block(backroom()),
            "wallpaper_tiled_dark"
    );

    public static final Block WALLPAPER_TILED_DARK_LAMP = register(
            new Block(backroom()),
            "wallpaper_tiled_dark_lamp"
    );

    public static final Block WALLPAPER_TILED_LAMP = register(
            new BackroomLampBlock(backroom().lightLevel(state -> 15)),
            "wallpaper_tiled_lamp"
    );

    public static final Block POOL_TILES = register(
            new Block(backroom()),
            "pool_tiles"
    );

    public static final Block UNCRYSTAL_WOOD_PLANKS = register(
            new Block(backroom()),
            "uncrystal_wood_planks"
    );

    // Gira conforme a face clicada, igual tronco (no addon era o trait placement_position/block_face).
    public static final Block UNCRYSTAL_WOOD_PILLAR = register(
            new RotatedPillarBlock(backroom()),
            "uncrystal_wood_pillar"
    );

    // Candy Lands. Tempo de quebra, resistência e som tirados do addon.
    public static final Block CANDY_CANE_LOG = register(
            new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(2.0f).sound(SoundType.WOOD)),
            "candy_cane_log"
    );

    public static final Block CANDY_CANE_WOOD = register(
            new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(2.0f).sound(SoundType.WOOD)),
            "candy_cane_wood"
    );

    public static final Block COOKIE_GRASS = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.6f).sound(SoundType.GRASS)),
            "cookie_grass"
    );

    public static final Block COOKIE_STONE = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.5f, 6.0f).sound(SoundType.STONE)),
            "cookie_stone"
    );

    // No addon é um bloco comum que não some longe do tronco, então não é LeavesBlock.
    public static final Block COTTON_LEAVES = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.35f, 0.2f).sound(SoundType.GRASS)
                    .noOcclusion().isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)),
            "cotton_leaves"
    );

    public static final Block CHOCOLATE_BAR_BLOCK = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f).sound(SoundType.STONE)),
            "chocolate_bar_block"
    );

    public static final Block SUGAR_BLOCK = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.5f).sound(SoundType.STONE)),
            "sugar_block"
    );

    public static final Block PIZZA = register(
            new PizzaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instabreak().sound(SoundType.WOOL)
                    .noOcclusion().pushReaction(PushReaction.DESTROY)),
            "pizza",
            new FabricItemSettings().maxCount(1)
    );

    public static final Block EXTREME_TORCH = register(
            new ExtremeTorchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak().sound(SoundType.WOOD)
                    .noCollission().noOcclusion().lightLevel(state -> 15).pushReaction(PushReaction.DESTROY)),
            "extreme_torch"
    );

    // Sal. O minério solta de 1 a 5 de XP; o bloco e o pó espantam formigas (ver SaltBlock).
    public static final Block SALT_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 1.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE), UniformInt.of(1, 5)),
            "salt_ore"
    );

    public static final Block SALT_BLOCK = register(
            new SaltBlock(40, 40, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.0f)
                    .lightLevel(state -> 3).sound(SoundType.SAND)),
            "salt_block"
    );

    // Sem item próprio: quem coloca é o item salt.
    public static final Block SALT_DUST = registerBlockOnly(
            new SaltDustBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.5f).lightLevel(state -> 1)
                    .noOcclusion().sound(SoundType.SAND).pushReaction(PushReaction.DESTROY)),
            "salt_dust"
    );

    // Pé de tomate. Quem planta é o item tomato.
    public static final Block TOMATO_PLANT_HEAD = registerBlockOnly(
            new TomatoPlantHeadBlock(tomatoPlant()),
            "tomato_plant_head"
    );

    public static final Block TOMATO_PLANT_BODY = registerBlockOnly(
            new TomatoPlantBodyBlock(tomatoPlant()),
            "tomato_plant_body"
    );

    // Dream World. Dureza e resistência são o seconds_to_destroy e o explosion_resistance do addon.
    public static final Block KYANITE = register(
            new CrystalBlock(crystal(MapColor.LAPIS, 1.5f, 6.0f, SoundType.AMETHYST)),
            "kyanite"
    );

    public static final Block CRYSTAL_GRASS = register(
            new CrystalBlock(crystal(MapColor.COLOR_LIGHT_GREEN, 0.6f, 0.6f, SoundType.AMETHYST)),
            "crystal_grass"
    );

    public static final Block DREAM_WORLD_GRASS_BLOCK = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(0.6f).sound(SoundType.GRASS)),
            "dream_world_grass_block"
    );

    public static final Block DREAM_STONE = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.LAPIS).strength(1.5f, 6.0f).sound(SoundType.STONE)),
            "dream_stone"
    );

    public static final Block CRYSTAL_WOOD_LOG = register(
            new CrystalBlock.Pillar(crystal(MapColor.SAND, 2.0f, 2.0f, SoundType.WOOD)),
            "crystal_wood_log"
    );

    public static final Block STRIPPED_CRYSTAL_WOOD_LOG = register(
            new CrystalBlock.Pillar(crystal(MapColor.SAND, 2.0f, 2.0f, SoundType.WOOD)),
            "stripped_crystal_wood_log"
    );

    public static final Block RED_CRYSTAL_WOOD_LOG = register(
            new CrystalBlock.Pillar(crystal(MapColor.COLOR_ORANGE, 2.0f, 2.0f, SoundType.WOOD)),
            "red_crystal_wood_log"
    );

    public static final Block STRIPPED_RED_CRYSTAL_WOOD_LOG = register(
            new CrystalBlock.Pillar(crystal(MapColor.COLOR_ORANGE, 2.0f, 2.0f, SoundType.WOOD)),
            "stripped_red_crystal_wood_log"
    );

    public static final Block CRYSTAL_WOOD_PLANKS = register(
            new CrystalBlock(crystal(MapColor.SAND, 2.0f, 3.0f, SoundType.WOOD)),
            "crystal_wood_planks"
    );

    public static final Block RED_CRYSTAL_WOOD_PLANKS = register(
            new CrystalBlock(crystal(MapColor.COLOR_ORANGE, 2.0f, 3.0f, SoundType.WOOD)),
            "red_crystal_wood_planks"
    );

    public static final Block CRYSTAL_MUSHROOM_STEM = register(
            new CrystalBlock(crystal(MapColor.SAND, 2.0f, 2.0f, SoundType.WOOD)),
            "crystal_mushroom_stem"
    );

    public static final Block CRYSTAL_LEAVES_BLUE = register(new Block(crystalLeaves(MapColor.COLOR_BLUE)), "crystal_leaves_blue");
    public static final Block CRYSTAL_LEAVES_GREEN = register(new Block(crystalLeaves(MapColor.EMERALD)), "crystal_leaves_green");
    public static final Block CRYSTAL_LEAVES_PINK = register(new Block(crystalLeaves(MapColor.CRIMSON_STEM)), "crystal_leaves_pink");
    public static final Block CRYSTAL_LEAVES_PURPLE = register(new Block(crystalLeaves(MapColor.COLOR_PURPLE)), "crystal_leaves_purple");
    public static final Block CRYSTAL_LEAVES_PURPLISH_RED = register(new Block(crystalLeaves(MapColor.COLOR_RED)), "crystal_leaves_purplish_red");

    public static final Block PURPLE_CRYSTAL_FLOWER = register(crystalFlower(), "purple_crystal_flower");
    public static final Block RED_CRYSTAL_FLOWER = register(crystalFlower(), "red_crystal_flower");

    public static final Block CRYSTAL_CRAFTING_TABLE = register(
            new CrystalCraftingTableBlock(crystal(MapColor.SAND, 2.5f, 2.5f, SoundType.WOOD)),
            "crystal_crafting_table"
    );

    // No addon não tinha estado aceso. A textura front_on já existia, então aqui acende como a fornalha vanilla.
    public static final Block CRYSTAL_FURNACE = register(
            new CrystalFurnaceBlock(crystal(MapColor.COLOR_BLUE, 3.5f, 3.5f, SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0)),
            "crystal_furnace"
    );

    public static final BlockEntityType<CrystalFurnaceBlock.Entity> CRYSTAL_FURNACE_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, TwistedLands.id("crystal_furnace"),
            FabricBlockEntityTypeBuilder.create(CrystalFurnaceBlock.Entity::new, CRYSTAL_FURNACE).build());

    public static final MenuType<CrystalFurnaceBlock.Menu> CRYSTAL_FURNACE_MENU = Registry.register(
            BuiltInRegistries.MENU, TwistedLands.id("crystal_furnace"),
            new MenuType<>(CrystalFurnaceBlock.Menu::new, FeatureFlags.VANILLA_SET));

    public static final RecipeType<CrystalFurnaceBlock.Smelting> CRYSTAL_SMELTING = RecipeType.register("twisted:crystal_smelting");

    public static final RecipeSerializer<CrystalFurnaceBlock.Smelting> CRYSTAL_SMELTING_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER, TwistedLands.id("crystal_smelting"),
            new SimpleCookingSerializer<>(CrystalFurnaceBlock.Smelting::new, 133));

    // Mais baixa que a Sacred Torch e sem os blocos de luz. Fogo na altura do addon (0.8).
    public static final Block CRYSTAL_TORCH = register(
            new ExtremeTorchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).instabreak().sound(SoundType.WOOD)
                    .noCollission().noOcclusion().lightLevel(state -> 15).pushReaction(PushReaction.DESTROY), 0, 10, 0.8),
            "crystal_torch"
    );

    public static final Block CRYSTAL_COAL_BLOCK = register(
            new CrystalBlock(crystal(MapColor.COLOR_ORANGE, 5.0f, 6.0f, SoundType.AMETHYST)),
            "crystal_coal_block"
    );

    public static final Block JADE_BLOCK = register(
            new CrystalBlock(crystal(MapColor.DIAMOND, 1.5f, 6.0f, SoundType.AMETHYST)),
            "jade_block"
    );

    public static final Block JADE_BRICKS = register(
            new CrystalBlock(crystal(MapColor.DIAMOND, 1.5f, 6.0f, SoundType.AMETHYST)),
            "jade_bricks"
    );

    public static final Block PINK_TOURMALINE_BLOCK = register(
            new CrystalBlock(crystal(MapColor.ICE, 1.5f, 6.0f, SoundType.AMETHYST)),
            "pink_tourmaline_block"
    );

    public static final Block PINK_TOURMALINE_BRICKS = register(
            new CrystalBlock(crystal(MapColor.ICE, 1.5f, 6.0f, SoundType.AMETHYST)),
            "pink_tourmaline_bricks"
    );

    public static final Block TIGERS_EYE_BLOCK = register(
            new CrystalBlock(crystal(MapColor.SAND, 1.5f, 6.0f, SoundType.AMETHYST)),
            "tigers_eye_block"
    );

    // Minérios do Dream World: qualquer picareta, de 1 a 5 de XP (ore_xp_reward).
    public static final Block CRYSTAL_COAL_ORE = register(
            new CrystalBlock(crystal(MapColor.COLOR_ORANGE, 3.0f, 3.0f, SoundType.AMETHYST).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)),
            "crystal_coal_ore"
    );

    public static final Block JADE_ORE = register(
            new CrystalBlock(crystal(MapColor.DIAMOND, 3.0f, 3.0f, SoundType.AMETHYST).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)),
            "jade_ore"
    );

    public static final Block PINK_TOURMALINE_ORE = register(
            new CrystalBlock(crystal(MapColor.ICE, 3.0f, 3.0f, SoundType.AMETHYST).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)),
            "pink_tourmaline_ore"
    );

    public static final Block TIGERS_EYE_DEBRIS = register(
            new CrystalBlock(crystal(MapColor.TERRACOTTA_GRAY, 3.0f, 3.0f, SoundType.AMETHYST).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)),
            "tigers_eye_debris"
    );

    // Blocos que só dropam eles mesmos. A tabela de loot é criada no LootDataManagerMixin, sem JSON.
    public static final List<Block> SELF_DROPS = List.of(KYANITE, DREAM_STONE, CRYSTAL_WOOD_LOG, STRIPPED_CRYSTAL_WOOD_LOG,
            RED_CRYSTAL_WOOD_LOG, STRIPPED_RED_CRYSTAL_WOOD_LOG, CRYSTAL_WOOD_PLANKS, RED_CRYSTAL_WOOD_PLANKS,
            CRYSTAL_MUSHROOM_STEM, PURPLE_CRYSTAL_FLOWER, RED_CRYSTAL_FLOWER, CRYSTAL_CRAFTING_TABLE, CRYSTAL_FURNACE,
            CRYSTAL_TORCH, CRYSTAL_COAL_BLOCK, JADE_BLOCK, JADE_BRICKS, PINK_TOURMALINE_BLOCK, PINK_TOURMALINE_BRICKS,
            TIGERS_EYE_BLOCK);

    // Minérios da Dream World e o item que dropam (com fortuna e toque de seda, como os do vanilla).
    public static final Map<Block, String> ORE_DROPS = Map.of(CRYSTAL_COAL_ORE, "crystal_coal", JADE_ORE, "raw_jade",
            PINK_TOURMALINE_ORE, "raw_pink_tourmaline");

    private static Block registerBlockOnly(Block block, String id) {
        return Registry.register(BuiltInRegistries.BLOCK, TwistedLands.id(id), block);
    }

    private static BlockBehaviour.Properties tomatoPlant() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().randomTicks()
                .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY);
    }

    // Translúcidos, ver CrystalBlock.
    private static BlockBehaviour.Properties crystal(MapColor color, float hardness, float resistance, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance).sound(sound).noOcclusion();
    }

    // Igual às cotton leaves: bloco comum, não some longe do tronco.
    private static BlockBehaviour.Properties crystalLeaves(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(0.35f, 0.2f).sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    // No addon a flor vai em bloco com a tag grass ou kyanite. A tag dirt do Java faz o papel da grass.
    private static Block crystalFlower() {
        return new BushBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()
                .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)) {
            private final VoxelShape shape = Block.box(4, 0, 4, 12, 12, 12);

            @Override
            protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
                return super.mayPlaceOn(state, level, pos) || state.is(KYANITE);
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return shape;
            }
        };
    }

    public static void register() {
        // Mesma inflamabilidade do addon (catch 5, destroy 20).
        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        flammable.add(CANDY_CANE_LOG, 5, 20);
        flammable.add(CANDY_CANE_WOOD, 5, 20);
        flammable.add(COTTON_LEAVES, 5, 20);
        for (Block block : List.of(CRYSTAL_WOOD_LOG, STRIPPED_CRYSTAL_WOOD_LOG, RED_CRYSTAL_WOOD_LOG, STRIPPED_RED_CRYSTAL_WOOD_LOG,
                CRYSTAL_WOOD_PLANKS, RED_CRYSTAL_WOOD_PLANKS, CRYSTAL_MUSHROOM_STEM, CRYSTAL_CRAFTING_TABLE, CRYSTAL_LEAVES_BLUE,
                CRYSTAL_LEAVES_GREEN, CRYSTAL_LEAVES_PINK, CRYSTAL_LEAVES_PURPLE, CRYSTAL_LEAVES_PURPLISH_RED)) {
            flammable.add(block, 5, 20);
        }

        // O log do addon descascava com qualquer machado e gastava o machado inteiro. Aqui é o descascar vanilla.
        StrippableBlockRegistry.register(CRYSTAL_WOOD_LOG, STRIPPED_CRYSTAL_WOOD_LOG);
        StrippableBlockRegistry.register(RED_CRYSTAL_WOOD_LOG, STRIPPED_RED_CRYSTAL_WOOD_LOG);
    }
}
