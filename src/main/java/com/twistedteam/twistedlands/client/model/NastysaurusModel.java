package com.twistedteam.twistedlands.client.model;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.MantisEntity;
import com.twistedteam.twistedlands.entities.MobjiraEntity;
import com.twistedteam.twistedlands.entities.NastysaurusEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class NastysaurusModel extends DefaultedEntityGeoModel<NastysaurusEntity> {

    public NastysaurusModel() {
        super(TwistedLands.id("nastysaurus"));
    }
}
