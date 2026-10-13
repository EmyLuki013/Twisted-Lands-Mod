package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.entities.*;
import com.twistedteam.twistedlands.projectiles.ThrownAxeEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class TwistedLandsEntities {

    public static final EntityType<AppleCowEntity> APPLE_COW =
            Registry.register(BuiltInRegistries.ENTITY_TYPE,
                    new ResourceLocation(TwistedLands.MOD_ID, "apple_cow"),
                    EntityType.Builder.of(AppleCowEntity::new, MobCategory.CREATURE)
                            .sized(0.9f, 1.4f).build("apple_cow"));

    public static final EntityType<GoldenAppleCow> GOLDEN_APPLE_COW =
            Registry.register(BuiltInRegistries.ENTITY_TYPE,
                    new ResourceLocation(TwistedLands.MOD_ID, "golden_apple_cow"),
                    EntityType.Builder.of(GoldenAppleCow::new, MobCategory.CREATURE)
                            .sized(0.9f, 1.4f).build("golden_apple_cow"));

    public static final EntityType<EnchantedGoldenAppleCow> ENCHANTED_GOLDEN_APPLE_COW =
            Registry.register(BuiltInRegistries.ENTITY_TYPE,
                    new ResourceLocation(TwistedLands.MOD_ID, "enchanted_golden_apple_cow"),
                    EntityType.Builder.of(EnchantedGoldenAppleCow::new, MobCategory.CREATURE)
                            .sized(0.9f, 1.4f).build("enchanted_golden_apple_cow"));

    public static final EntityType<MobjiraEntity> MOBJIRA =
            Registry.register(BuiltInRegistries.ENTITY_TYPE,
                    new ResourceLocation(TwistedLands.MOD_ID, "mobjira"),
                    EntityType.Builder.of(MobjiraEntity::new, MobCategory.MONSTER)
                            .sized(33.0f, 50.0f).build("mobjira"));

    public static final EntityType<MantisEntity> MANTIS =
            Registry.register(BuiltInRegistries.ENTITY_TYPE,
                    new ResourceLocation(TwistedLands.MOD_ID, "mantis"),
                    EntityType.Builder.of(MantisEntity::new, MobCategory.MONSTER)
                            .sized(3.0f, 3.0f).build("mantis"));

    public static final EntityType<NastysaurusEntity> NASTYSAURUS =
            Registry.register(BuiltInRegistries.ENTITY_TYPE,
                    new ResourceLocation(TwistedLands.MOD_ID, "nastysaurus"),
                    EntityType.Builder.of(NastysaurusEntity::new, MobCategory.MONSTER)
                            .sized(7.0f, 5.5f).build("nastysaurus"));

    // Hitbox = collision_box do addon (1.4 x 0.9) vezes o scale de cada formiga.
    public static final EntityType<RainbowAntEntity> RAINBOW_ANT = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            TwistedLands.id("rainbow_ant"),
            FabricEntityTypeBuilder.<RainbowAntEntity>createMob()
                    .spawnGroup(MobCategory.MONSTER)
                    .entityFactory(RainbowAntEntity::new)
                    .dimensions(EntityDimensions.scalable(1.12f, 0.72f))
                    .defaultAttributes(RainbowAntEntity::createAttributes)
                    .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RainbowAntEntity::canSpawn)
                    .build());

    public static final EntityType<CandyAntEntity> CANDY_ANT = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            TwistedLands.id("candy_ant"),
            FabricEntityTypeBuilder.<CandyAntEntity>createMob()
                    .spawnGroup(MobCategory.CREATURE)
                    .entityFactory(CandyAntEntity::new)
                    .dimensions(EntityDimensions.scalable(0.84f, 0.54f))
                    .defaultAttributes(CandyAntEntity::createAttributes)
                    .build());

    // Sem collision_box no addon vale o padrão do Bedrock (1 x 1), vezes o scale 0.5.
    public static final EntityType<RatEntity> RAT = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            TwistedLands.id("rat"),
            FabricEntityTypeBuilder.<RatEntity>createMob()
                    .spawnGroup(MobCategory.CREATURE)
                    .entityFactory(RatEntity::new)
                    .dimensions(EntityDimensions.scalable(0.5f, 0.5f))
                    .defaultAttributes(RatEntity::createAttributes)
                    .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RatEntity::canSpawn)
                    .build());

    public static final EntityType<LittleMintEntity> LITTLE_MINT = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            TwistedLands.id("little_mint"),
            FabricEntityTypeBuilder.<LittleMintEntity>create(MobCategory.MISC, LittleMintEntity::new)
                    .dimensions(EntityDimensions.fixed(0.75f, 0.8f))
                    .trackRangeChunks(8)
                    .build());

    public static final EntityType<ThrownAxeEntity> THROWN_AXE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(TwistedLands.MOD_ID, "thrown_axe"),
            EntityType.Builder.<ThrownAxeEntity>of(ThrownAxeEntity::new, MobCategory.MISC)
                    .sized(1.5F, 3.5F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("thrown_axe")
    );

    public static void initialize() {
        // No addon a formiga arco-íris nasce em todo o overworld, no subsolo (as condições ficam no canSpawn).
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, RAINBOW_ANT, 1, 1, 3);
        // O rato é population "animal" no addon, nos biomas com a tag caves (lush, dripstone e deep dark).
        BiomeModifications.addSpawn(BiomeSelectors.tag(ConventionalBiomeTags.CAVES), MobCategory.CREATURE, RAT, 100, 1, 4);
    }
}
