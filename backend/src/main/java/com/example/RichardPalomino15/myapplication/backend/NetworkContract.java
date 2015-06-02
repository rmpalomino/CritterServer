package com.example.RichardPalomino15.myapplication.backend;

/**
 * Created by RichardPalomino on 2/23/2015. Updated 3/7/2015.
 *
 * This class defines the constants used for response headers and other server-client communication.
 */
public class NetworkContract {

	//Response header and values for update. This lets the client know whether to load the sent
	//game data.
	public final static String UPDATE_HEADER = "SHOULD_UPDATE";
	public final static int NO_UPDATE = 0;
	public final static int UPDATE = 1;

	//Response header set during matchmaking, tells player whether they are host or guest.
	public final static String HOST_HEADER = "HOSTING";
	public final static int HOST = 0;
	public final static int GUEST = 1;

	//Response header for turn status
	public final static String TURN_STATUS_HEADER = "TURN_STATUS";

	//Request body parameters
	public final static String GAME_ID_PARAM = "GameID";
	public final static String GAME_STATE_PARAM = "State";

}
