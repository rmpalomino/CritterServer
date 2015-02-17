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
public class MatchServlet extends HttpServlet {
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

        String[] tokens = requestBuilder.toString().split("&");
        String[] gameTokens = tokens[0].split("=");
        int gameNum = Integer.parseInt(gameTokens[1]);

        System.out.printf("Made contact, game %d was requested.\n", gameNum);

        if (gameNum >= 0) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            Query.Filter gameFilter = new Query.FilterPredicate(GameEntity.GAME_IDENTITY, Query.FilterOperator.EQUAL, gameNum);
            Query gameQuery = new Query(GameEntity.GAME_TYPE).setFilter(gameFilter);
            PreparedQuery preparedGameQuery = datastore.prepare(gameQuery);

            Entity gameEntity = preparedGameQuery.asSingleEntity();

            if (gameEntity == null) {
                Entity newGame = GameEntity.CreateGameEntity();
                newGame.setProperty(GameEntity.GAME_IDENTITY, gameNum);
                newGame.setProperty(GameEntity.GAME_STATUS, GameEntity.NEW_GAME);
                datastore.put(newGame);
                System.out.printf("Created new game, num is %d\n", gameNum);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }

            else {
                PrintWriter writer = resp.getWriter();
                String respText = ((Text)gameEntity.getProperty(GameEntity.CURRENT_ORDERS_PREFIX + gameEntity.getProperty(GameEntity.TURN_NUMBER))).getValue();
                System.out.println("Sending response to client: \n" + respText);
                writer.print(respText);
                resp.setStatus(HttpServletResponse.SC_OK);
            }

        }

    }
}

