package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.Entity;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 *
 * This class holds all of the GameEntity contract information for the Google Datastore.
 *
 */
public class GameEntity {

	/*
	Note that the current version of the server uses the player numbers 0 or 1 to show it only
	has one set of orders, and whose they are.
	 */
    public static final int NO_ORDERS = -1;
    public static final int ALL_ORDERS = 2;

    /*
    Possible game status, used for matchmaking queries and to tell if the game is over.
     */
    
    public static final int NEW_GAME = 1;
	public static final int IN_PROGRESS = 2;
	public static final int GAME_OVER = 3;

	/*
	Note that player identifiers are not currently used. Once we implement a user system, they will
	be used to store the players in the current game.
	 */
    public static final String ENTITY_TYPE = "GameEntity";
    public static final String GAME_IDENTITY = "GID";
    public static final String TURN_STATUS = "S";
    public static final String PLAYER_1_ID = "P1ID";
    public static final String PLAYER_2_ID = "P2ID";
    public static final String TURN_NUMBER = "TN";
    public static final String CURRENT_ORDERS_PREFIX = "CO";
    public static final String GAME_STATUS = "GS";

    public static Entity CreateGameEntity() {
        Entity e = new Entity(ENTITY_TYPE);
		e.setUnindexedProperty(GameEntity.TURN_STATUS, GameEntity.NO_ORDERS);
		e.setUnindexedProperty(GameEntity.TURN_NUMBER, 0);
		e.setProperty(GAME_STATUS, NEW_GAME);
		return e;
    }
	public static Entity CreateNumberedGame(int id) {
		Entity e = new Entity(ENTITY_TYPE, id);
		e.setUnindexedProperty(GameEntity.TURN_STATUS, GameEntity.NO_ORDERS);
		e.setUnindexedProperty(GameEntity.TURN_NUMBER, 0);
		e.setProperty(GAME_STATUS, NEW_GAME);
		return e;
	}
}