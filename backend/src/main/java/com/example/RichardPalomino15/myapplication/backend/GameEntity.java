package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.Entity;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 *
 * This class holds all of the GameEntity contract information for the Google Datastore.
 */
public class GameEntity {

    public static final int NO_ORDERS = -1;
    public static final int HAVE_ORDER1 = 0;
    public static final int HAVE_ORDER2 = 1;
    public static final int ALL_ORDERS = 2;

    public static final String GAME_TYPE = "GameEntity";
    public static final String GAME_IDENTITY = "GID";
    public static final String TURN_STATUS = "S";
    public static final String PLAYER_1_ID = "P1ID";
    public static final String PLAYER_2_ID = "P2ID";
    public static final String TURN_NUMBER = "TN";
    public static final String CURRENT_ORDERS_PREFIX = "CO";
    public static final String GAME_END_STATE = "GO";

    public static Entity CreateGameEntity() {
        return new Entity(GAME_TYPE);
    }
}