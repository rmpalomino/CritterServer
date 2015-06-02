package com.example.RichardPalomino15.myapplication.backend;

import com.google.api.client.util.Charsets;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Richard Palomino 15 on 2/3/2015. Updated 3/7/2015.
 *
 * Handles initial game requests, creating a new game if necessary or sending the latest completed
 * turn otherwise.
 */
public class MatchServlet extends HttpServlet {

	private static boolean local_debug = false;
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		//Reader busy work
        BufferedReader requestReader = req.getReader();
        StringBuilder requestBuilder = new StringBuilder();

        while ( requestReader.ready() )
            requestBuilder.append(requestReader.readLine());

		//Request string is in the form: URL_ESCAPED(GameID=XX&State=SS&uniqueID=YY) due to Unity restrictions
        String[] tokens = requestBuilder.toString().split("&");
        String[] gameTokens = tokens[0].split("=");
		String[] stateTokens = tokens[1].split("=");
		String[] uniqueIdTokens = tokens[2].split("=");

        long gameNum = Long.parseLong(gameTokens[1]);

		//Undo URL escaping
		String decoded = URLDecoder.decode(stateTokens[1], Charsets.UTF_8.name());

		if (local_debug)
        	System.out.printf("Made contact, game %d was requested.\n", gameNum);

		String uniqueID = uniqueIdTokens[1];

		//Trying to locate an existing game
        if (gameNum >= 0) {

			//Request the corresponding GameEntity for GameID XX

            Query.Filter gameFilter = new Query.FilterPredicate(GameEntity.GAME_IDENTITY, Query.FilterOperator.EQUAL, gameNum);
            Query gameQuery = new Query(GameEntity.ENTITY_TYPE).setFilter(gameFilter);
            PreparedQuery preparedGameQuery = datastore.prepare(gameQuery);

			//Limit to one entity, throws PreparedQuery.TooManyResultsException
			Entity gameEntity = null;
			try {
				gameEntity = preparedGameQuery.asSingleEntity();
			} catch(PreparedQuery.TooManyResultsException e) {
				e.printStackTrace();
			}

			//No matching game for this GameID
            if (gameEntity == null) {
				//Create new GameEntity
                Entity newGame = GameEntity.CreateGameEntity();
				//Assign game id, ensure that no other properties are indexable
                newGame.setProperty(GameEntity.GAME_IDENTITY, gameNum);
				newGame.setUnindexedProperty(GameEntity.CURRENT_ORDERS_PREFIX + "0", new Text(decoded));
                datastore.put(newGame);

				//Tell Unity client that their state of the game is correct
//                System.out.printf("Created new game, num is %d\n", gameNum);
				resp.addHeader(NetworkContract.UPDATE_HEADER, Integer.toString(NetworkContract.NO_UPDATE));
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }

			//Game already exists in the Datastore
            else {
				//Busy work to get PrintWriter
                PrintWriter writer = resp.getWriter();

				//Send latest full turn to the client, tell them to update their game
                long currentTurn = ((Long) gameEntity.getProperty(GameEntity.TURN_NUMBER));

				resp.addIntHeader(NetworkContract.UPDATE_HEADER, NetworkContract.UPDATE);

				//Get the current turn's state to send to Unity client
                String currentTurnText = null;
				if (gameEntity.hasProperty(GameEntity.CURRENT_ORDERS_PREFIX + currentTurn))
						currentTurnText = ((Text)gameEntity.getProperty(GameEntity.CURRENT_ORDERS_PREFIX + currentTurn)).getValue();

				if (local_debug) {
					System.out.println(GameEntity.CURRENT_ORDERS_PREFIX + currentTurn);
					System.out.println("Sending response to client: \n" + currentTurnText);
				}

				//The game hasn't been played yet, but if there are two players...It's on.
				if (currentTurnText == null) {
					if(gameEntity.hasProperty(GameEntity.PLAYER_1_ID)
						&& gameEntity.hasProperty(GameEntity.PLAYER_2_ID)) {
						resp.addIntHeader(NetworkContract.UPDATE_HEADER, NetworkContract.UPDATE);
						resp.addIntHeader(NetworkContract.HOST_HEADER, NetworkContract.HOST);
						gameEntity.setUnindexedProperty(GameEntity.CURRENT_ORDERS_PREFIX + "0", new Text(decoded));
//						System.out.println("Added initial state to prevent breakage...");
						datastore.put(gameEntity);

					}
				}

				//It's already on.
				else {
					resp.addIntHeader(NetworkContract.UPDATE_HEADER, NetworkContract.UPDATE);
				}

                writer.print(currentTurnText);
                resp.setStatus(HttpServletResponse.SC_OK);
            }

        }

		//GameID was less than 0, so trying to find a quick game to play
		else {

			//Try finding a game with a waiting host
			Query.Filter quickGameFilter = new Query.FilterPredicate(GameEntity.GAME_STATUS, Query.FilterOperator.EQUAL, GameEntity.NEW_GAME);
			Query quickGameQuery = new Query(GameEntity.ENTITY_TYPE).setFilter(quickGameFilter);
			PreparedQuery quickGamePQ = datastore.prepare(quickGameQuery);
			List<Entity> gameList = quickGamePQ.asList(FetchOptions.Builder.withDefaults());

			//Assume there was no game
			Long gameID = -1L;

			//There are no waiting games, so become a new host
			if (0 == gameList.size()) {
				//Create a new game but have the datastore give the GameID
				Entity newGameEntity = GameEntity.CreateGameEntity();

				//Set some identifying information and store the new game
				//TODO: Find someway around the double write.
				newGameEntity.setUnindexedProperty(GameEntity.PLAYER_1_ID, uniqueID);
				gameID = datastore.put(newGameEntity).getId();
				newGameEntity.setProperty(GameEntity.GAME_IDENTITY, gameID);
				datastore.put(newGameEntity);

				//Let the user know that you made a game for them
				resp.addIntHeader(NetworkContract.UPDATE_HEADER, NetworkContract.NO_UPDATE);
				if(local_debug) {
					System.out.println("Becoming quick host\n");
					System.out.println(gameID.toString());
				}

			}

			//There was a game!
			else {
				//Get it
				Entity firstGame = gameList.get(0);

				gameID = (Long) firstGame.getProperty(GameEntity.GAME_IDENTITY);
				firstGame.setProperty(GameEntity.GAME_STATUS, GameEntity.IN_PROGRESS);
				firstGame.setUnindexedProperty(GameEntity.PLAYER_2_ID, uniqueID);
				datastore.put(firstGame);

				//Let the user know they're good to start the game
				resp.addIntHeader(NetworkContract.UPDATE_HEADER, NetworkContract.UPDATE);
				resp.addIntHeader(NetworkContract.HOST_HEADER, NetworkContract.GUEST);
//				System.out.println("Found quick game\n");
			}

			//Seems legit if you're here
			resp.addHeader(NetworkContract.GAME_ID_PARAM, Long.toString(gameID));
			resp.setStatus(HttpServletResponse.SC_OK);
		}



    }
}
