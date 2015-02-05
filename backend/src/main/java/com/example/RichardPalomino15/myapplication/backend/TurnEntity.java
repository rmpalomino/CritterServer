package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.Entity;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 */
public class TurnEntity {

    public static final String TURN_TYPE = "TurnEntity";
    public static final String GAME_IDENTITY_PROPERTY = "GameID";
    public static final String TURN_NUMBER_PROPERTY = "TurnNumber";
    public static final String CURRENT_ORDERS_PROPERTY = "CurrentOrders";
    public static final String RESULTING_STATE_PROPERTY = "ResultingState";

    public static Entity CreateGameEntity() {
        return new Entity(TURN_TYPE);
    }
}
