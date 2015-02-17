package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 */
public class QueryServlet extends HttpServlet {

    public static final int STATUS_REQUEST = 0;
    public static final int ORDERS_REQUEST = 1;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BufferedReader requestReader = req.getReader();
        StringBuilder requestBuilder = new StringBuilder();

        while ( requestReader.ready() )
            requestBuilder.append(requestReader.readLine());

        boolean badRequest = true;

        try {

            String[] tokens = requestBuilder.toString().split("&");
            String[] gameTokens = tokens[0].split("=");
            String[] playerToken = tokens[1].split("=");
            String[] turnTokens = tokens[2].split("=");

            int gameNum = Integer.parseInt(gameTokens[1]);
            int playerNum = Integer.parseInt(playerToken[1]);
            int turnNum = Integer.parseInt(turnTokens[1]);

            if (gameNum >= 0) {
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

                Query.Filter gameFilter = new Query.FilterPredicate(GameEntity.GAME_IDENTITY, Query.FilterOperator.EQUAL, gameNum);
                Query gameQuery = new Query(GameEntity.GAME_TYPE).setFilter(gameFilter);
                PreparedQuery preparedGameQuery = datastore.prepare(gameQuery);

                Entity gameEntity = preparedGameQuery.asSingleEntity();

                if (gameEntity != null) {
                    System.out.printf("Received a legit query for game %d, turn %d from player %d\n", gameNum, turnNum, playerNum);
                    resp.addHeader("TURN_STATE", ((Long) gameEntity.getProperty(GameEntity.TURN_STATUS)).toString());

                    if (gameEntity.hasProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum))) {
                        Text toSend = (Text) gameEntity.getProperty(GameEntity.CURRENT_ORDERS_PREFIX + Integer.toString(turnNum));
                        resp.getWriter().print(toSend.getValue());
                        badRequest = false;
                    }
                }
            }

            else
                badRequest = true;
        }

        catch (NumberFormatException e) {
            e.printStackTrace();
        }

        finally {
            if(badRequest)
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

