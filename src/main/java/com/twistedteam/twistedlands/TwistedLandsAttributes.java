package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.entities.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.animal.Cow;

public class TwistedLandsAttributes {

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.APPLE_COW, Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.GOLDEN_APPLE_COW, Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.ENCHANTED_GOLDEN_APPLE_COW, Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.MANTIS, MantisEntity.createMantisAttributes());
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.MOBJIRA, MobjiraEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.NASTYSAURUS, NastysaurusEntity.createNastysaurosAttributes());
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.RAINBOW_ANT, RainbowAntEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(TwistedLandsEntities.CANDY_ANT, CandyAntEntity.createAttributes());
    }

}
