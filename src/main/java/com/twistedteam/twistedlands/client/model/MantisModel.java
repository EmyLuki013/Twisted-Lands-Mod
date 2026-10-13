package com.twistedteam.twistedlands.client.model;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.MantisEntity;
import com.twistedteam.twistedlands.entities.MobjiraEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class MantisModel extends DefaultedEntityGeoModel<MantisEntity> {

    public MantisModel() {
        super(TwistedLands.id("mantis"));
    }
}
