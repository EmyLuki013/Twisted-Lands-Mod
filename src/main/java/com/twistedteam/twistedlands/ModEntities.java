package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.entity.CandyAntEntity;
import com.twistedteam.twistedlands.entity.LittleMintEntity;
import com.twistedteam.twistedlands.entity.RainbowAntEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class ModEntities {
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

    public static final EntityType<LittleMintEntity> LITTLE_MINT = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            TwistedLands.id("little_mint"),
            FabricEntityTypeBuilder.<LittleMintEntity>create(MobCategory.MISC, LittleMintEntity::new)
                    .dimensions(EntityDimensions.fixed(0.75f, 0.8f))
                    .trackRangeChunks(8)
                    .build());

    public static void initialize() {
        // No addon a formiga arco-íris nasce em todo o overworld, no subsolo (as condições ficam no canSpawn).
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, RAINBOW_ANT, 1, 1, 3);
    }
}
