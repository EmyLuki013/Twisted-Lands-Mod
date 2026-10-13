package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

public class AppleCowEntity extends Cow {

    public AppleCowEntity(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 45;
    }

    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return TwistedLandsEntities.APPLE_COW.create(level);
    }
}
