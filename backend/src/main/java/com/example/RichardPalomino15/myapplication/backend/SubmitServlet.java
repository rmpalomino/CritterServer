package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;


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
        super.doGet(req, resp);
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
            String[] requestType = tokens[3].split("=");

            int gameNum = Integer.parseInt(gameTokens[1]);
            int turnNum = Integer.parseInt(turnTokens[1]);

            if (gameNum >= 0) {
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

                Query.Filter gameFilter = new Query.FilterPredicate(GameEntity.GAME_IDENTITY, Query.FilterOperator.EQUAL, gameNum);
                Query gameQuery = new Query(GameEntity.GAME_TYPE).setFilter(gameFilter);
                PreparedQuery preparedGameQuery = datastore.prepare(gameQuery);

                Entity gameEntity = preparedGameQuery.asSingleEntity();

                Integer turn = (Integer)gameEntity.getProperty(GameEntity.TURN_NUMBER);

                String gameState = (String)gameEntity.getProperty(GameEntity.CURRENT_ORDERS_PREFIX + turn.toString());

                JsonParser jp = new JsonParser();



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


