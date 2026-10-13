package com.twistedteam.twistedlands.client.model;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.MobjiraEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class MobjiraModel extends DefaultedEntityGeoModel<MobjiraEntity> {

    public MobjiraModel() {
        super(TwistedLands.id("210_mobjira"));
    }
}
