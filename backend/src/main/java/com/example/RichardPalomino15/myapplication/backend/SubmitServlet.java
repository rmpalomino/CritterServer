package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.api.client.util.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 *
 * Handles the client's turn submissions.
 */
public class SubmitServlet extends HttpServlet {


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		BufferedReader requestReader = req.getReader();
		StringBuilder requestBuilder = new StringBuilder();

		while (requestReader.ready())
			requestBuilder.append(requestReader.readLine());

		boolean badRequest = true;

		try {

			String[] tokens = requestBuilder.toString().split("&");

			String[] gameTokens = tokens[0].split("=");
			String[] playerToken = tokens[1].split("=");
			String[] turnTokens = tokens[2].split("=");
			String[] gameStateTokens = tokens[3].split("=");

			String decoded = URLDecoder.decode(gameStateTokens[1], Charsets.UTF_8.name());
			System.out.printf("Working with submission string: %s\n", decoded);

			int gameNum = Integer.parseInt(gameTokens[1]);
			int turnNum = Integer.parseInt(turnTokens[1]);
			int playerNum = Integer.parseInt(playerToken[1]);

			if (gameNum >= 0) {
				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

				Query.Filter gameFilter = new Query.FilterPredicate(GameEntity.GAME_IDENTITY, Query.FilterOperator.EQUAL, gameNum);
				Query gameQuery = new Query(GameEntity.GAME_TYPE).setFilter(gameFilter);
				PreparedQuery preparedGameQuery = datastore.prepare(gameQuery);

				Entity gameEntity = preparedGameQuery.asSingleEntity();

				long turnStatus = -9;
				if (gameEntity.hasProperty(GameEntity.TURN_STATUS)) {
					if (gameEntity.getProperty(GameEntity.TURN_STATUS) != null)
						turnStatus = (Long) gameEntity.getProperty(GameEntity.TURN_STATUS);
				}
				else
					System.out.println("The entity is missing turn status.");

				long currTurn = (Long) gameEntity.getProperty(GameEntity.TURN_NUMBER);

				if (turnNum == currTurn) {

					if (turnStatus == GameEntity.NO_ORDERS) {

						if (gameEntity.hasProperty(GameEntity.CURRENT_ORDERS_PREFIX + turnNum)) {
							gameEntity.setUnindexedProperty(GameEntity.TURN_STATUS, playerNum);
						}

						gameEntity.setUnindexedProperty(GameEntity.CURRENT_ORDERS_PREFIX + turnNum, new Text(decoded));
						datastore.put(gameEntity);
						System.out.printf("Received first set of orders from player %d\n", playerNum);
					}

					else if (1 - turnStatus == playerNum) {

						String receivedState = decoded + "";
						String gameState = ((Text) gameEntity.getProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum))).getValue();

						JsonParser rJP = new JsonParser();
						JsonObject rJ = rJP.parse(receivedState).getAsJsonObject();
						JsonArray rUnitAJ = rJ.get(GameStateJSONContract.UNITS).getAsJsonArray();

						JsonParser gJP = new JsonParser();
						JsonObject gameJ = gJP.parse(gameState).getAsJsonObject();
						JsonArray unitAJ = gameJ.get(GameStateJSONContract.UNITS).getAsJsonArray();
						for (int i = 0; i < unitAJ.size(); i++) {

							JsonObject unitJ = unitAJ.get(i).getAsJsonObject();
							if (unitJ.get(GameStateJSONContract.PLAYER).getAsInt() == playerNum) {
								unitJ.entrySet().clear();
								for (Map.Entry<String, JsonElement> item : rUnitAJ.get(i).getAsJsonObject().entrySet()) {
									unitJ.add(item.getKey(), item.getValue());
								}
							}
						}

						gameEntity.setUnindexedProperty(GameEntity.TURN_STATUS, GameEntity.ALL_ORDERS);
						gameEntity.setUnindexedProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum), new Text(gameJ.toString()));
						datastore.put(gameEntity);

						System.out.println("Received both orders, attempted to finalize turn.");
					}
					badRequest = false;
				}
				else if (turnNum == currTurn + 1) {
					//Received orders for next turn, update current turn and status of turn
					gameEntity.setUnindexedProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum), new Text(decoded));
					gameEntity.setUnindexedProperty(GameEntity.TURN_STATUS, playerNum);
					gameEntity.setUnindexedProperty(GameEntity.TURN_NUMBER, turnNum);
					datastore.put(gameEntity);
					badRequest = false;
				}
			}
		} catch (NumberFormatException nE) {
			nE.printStackTrace();
		} catch (JsonSyntaxException jE) {
			System.out.println("Received orders were invalid JSON.");
			jE.printStackTrace();
		} catch (PreparedQuery.TooManyResultsException pE) {
			System.out.println("Check the game number storage, there are multiple games with same number.");
			pE.printStackTrace();
		}
		finally {
			if(badRequest) {
				System.out.println("Server is in an invalid state.");
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}


	}
}


