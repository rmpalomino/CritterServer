package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Richard Palomino 15 on 2/3/2015. Updated 3/7/2015.
 *
 * Answers client's game state requests.
 */
public class QueryServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BufferedReader requestReader = req.getReader();
        StringBuilder requestBuilder = new StringBuilder();

        while ( requestReader.ready() )
            requestBuilder.append(requestReader.readLine());

		//Strict query check. Should only be updated to false if the query was successful
        boolean badRequest = true;
		int turnNum = -1;
		long gameNum = -1L;
		int playerNum = -1;

        try {

            String[] tokens = requestBuilder.toString().split("&");
            String[] gameTokens = tokens[0].split("=");
            String[] playerToken = tokens[1].split("=");
            String[] turnTokens = tokens[2].split("=");

			//May throw NumberFormatException if the query text is bad
            gameNum = Long.parseLong(gameTokens[1]);
			playerNum = Integer.parseInt(playerToken[1]);
			turnNum = Integer.parseInt(turnTokens[1]);

			if (gameNum >= 0) {
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

                Query.Filter gameFilter = new Query.FilterPredicate(GameEntity.GAME_IDENTITY, Query.FilterOperator.EQUAL, gameNum);
                Query gameQuery = new Query(GameEntity.ENTITY_TYPE).setFilter(gameFilter);
                PreparedQuery preparedGameQuery = datastore.prepare(gameQuery);

				Entity gameEntity = null;
				try {
					gameEntity = preparedGameQuery.asSingleEntity();
				} catch(PreparedQuery.TooManyResultsException e) {
					e.printStackTrace();
				}

				//Ensure turn status persisted
				if (!gameEntity.hasProperty(GameEntity.TURN_STATUS))
					System.out.println("failed to save turn status.");

				//Get the turn status and number saved in the datastore
                long turnStatus = ((Long) gameEntity.getProperty(GameEntity.TURN_STATUS)).longValue();
                long currentTurn = ((Long) gameEntity.getProperty(GameEntity.TURN_NUMBER));

				//If the
                if (turnNum == currentTurn) {
                    System.out.printf("Received a legit query for game %d, turn %d from player %d\n", gameNum, turnNum, playerNum);
                    System.out.println("Sending state: " + turnStatus);

					resp.addHeader(NetworkContract.TURN_STATUS_HEADER, Long.toString(turnStatus));
					Text toSend = (Text) gameEntity.getProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum));
					resp.getWriter().print(toSend.getValue());
					badRequest = false;

                }

				else if(turnStatus == GameEntity.ALL_ORDERS && turnNum == currentTurn + 1) {
					resp.addHeader(NetworkContract.TURN_STATUS_HEADER, Integer.toString(GameEntity.NO_ORDERS));
					badRequest = false;
				}

            }

        }

        catch (NumberFormatException e) {
            e.printStackTrace();
        }

        finally {
            if(badRequest) {
				System.out.println("Server is in an invalid state during query.");
				System.out.printf("Request was from player %d, send game %d, turn %d\n", playerNum, gameNum, turnNum);
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
        }
    }
}

