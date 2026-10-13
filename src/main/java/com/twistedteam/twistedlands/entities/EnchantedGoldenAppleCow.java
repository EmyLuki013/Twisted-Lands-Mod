package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

public class EnchantedGoldenAppleCow extends Cow {

    public EnchantedGoldenAppleCow(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 60;
    }

    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return TwistedLandsEntities.ENCHANTED_GOLDEN_APPLE_COW.create(level);
    }
}
