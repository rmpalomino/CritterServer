package com.example.RichardPalomino15.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

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
//        super.doGet(req, resp);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity turnEntity = TurnEntity.CreateGameEntity();
        turnEntity.setProperty(TurnEntity.GAME_IDENTITY_PROPERTY, "1");
        turnEntity.setProperty(TurnEntity.TURN_NUMBER_PROPERTY, "1");
        turnEntity.setProperty(TurnEntity.CURRENT_ORDERS_PROPERTY, "MoveStuff");
        turnEntity.setProperty(TurnEntity.RESULTING_STATE_PROPERTY, "AttackStuff");
        datastore.put(turnEntity);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String s = "";
        BufferedReader r = req.getReader();

        while (r.ready())
            s += r.readLine();

        System.out.println(s);
    }
}


