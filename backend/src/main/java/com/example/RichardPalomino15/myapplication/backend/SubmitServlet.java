package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.api.client.util.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.appengine.api.datastore.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 */
public class SubmitServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BufferedReader requestReader = req.getReader();
        StringBuilder requestBuilder = new StringBuilder();

        while ( requestReader.ready() )
            requestBuilder.append(requestReader.readLine());

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

                //TODO: Extend past the first turn. (Check for turn number and turn status to check latest.)

                if (gameEntity.hasProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum))) {
                      long turn_status = (Long) gameEntity.getProperty(GameEntity.TURN_STATUS);

                      if (turn_status == GameEntity.NO_ORDERS) {
                            gameEntity.setProperty(GameEntity.CURRENT_ORDERS_PREFIX + turnNum, new Text(decoded));
                            gameEntity.setProperty(GameEntity.TURN_STATUS, playerNum);
                            datastore.put(gameEntity);
                            System.out.printf("Received first set of orders from player %d\n", playerNum);
                      }

                      else if (1 - turn_status == playerNum) {
                          //TODO: Combine the two states.
                          String receivedState = decoded + "";
                          String gameState = ((Text)gameEntity.getProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum))).getValue();
                          String cop = gameState + "";

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
                                  Iterator<Map.Entry<String, JsonElement>> it = rUnitAJ.get(i).getAsJsonObject().entrySet().iterator();
                                  while (it.hasNext()) {
                                      Map.Entry<String, JsonElement> item = it.next();
                                      unitJ.add(item.getKey(), item.getValue());
                                  }
                              }
                          }

                          System.out.println(decoded);
                          System.out.println(cop);
                          System.out.println(gameJ.toString());

                          gameEntity.setProperty(GameEntity.TURN_STATUS, GameEntity.ALL_ORDERS);
                          datastore.put(gameEntity);
                          System.out.println("Received both orders, attempted to finalize turn.");
                      }
                        else
                          System.out.println("Received orders for finalized turn.");

                }

                else {
                    gameEntity.setProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum), new Text(decoded));
                    gameEntity.setProperty(GameEntity.TURN_NUMBER, turnNum);
                    datastore.put(gameEntity);
                }





            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            System.out.println(turnNum);
            System.out.println(tokens[1]);
        }

        catch (NumberFormatException e) {
            e.printStackTrace();
        }

        finally {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }



    }
}


