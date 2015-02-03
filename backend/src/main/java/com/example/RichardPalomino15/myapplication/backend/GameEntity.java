package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.Entity;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 */
public class GameEntity {
    Entity mEntity;

    public static final String GAME_TYPE = "GameEntity";
    public static final String GAME_IDENTITY_PROPERTY = "GameID";
    public static final String PLAYER_1_TURN_PROPERTY = "P1Turn";
    public static final String PLAYER_2_TURN_PROPERTY = "P2Turn";
    public static final String STATUS_PROPERTY = "Status";

}
