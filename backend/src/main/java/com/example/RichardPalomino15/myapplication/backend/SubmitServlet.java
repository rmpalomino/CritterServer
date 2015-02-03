package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

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
//        super.doGet(req, resp);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity gameEntity = GameEntity.CreateGameEntity();
        gameEntity.setProperty(GameEntity.GAME_IDENTITY_PROPERTY, "Game1");
        gameEntity.setProperty(GameEntity.PLAYER_1_TURN_PROPERTY, "MoveStuff");
        gameEntity.setProperty(GameEntity.PLAYER_2_TURN_PROPERTY, "AttackStuff");
        gameEntity.setProperty(GameEntity.STATUS_PROPERTY, "Initial");

        datastore.put(gameEntity);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}


