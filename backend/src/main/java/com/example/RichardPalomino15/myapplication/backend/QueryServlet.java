package com.example.RichardPalomino15.myapplication.backend;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Richard Palomino 15 on 2/3/2015.
 */
public class QueryServlet extends HttpServlet {

    public enum GameRequest {RESOLVED_TURN, TURN_ORDERS, BAD_REQUEST};

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader requestReader = req.getReader();
        StringBuilder requestBuilder = new StringBuilder();

        while ( requestReader.ready() )
            requestBuilder.append(requestReader.readLine());

        GameRequest requestType = GameRequest.BAD_REQUEST;

        switch (requestBuilder.toString()) {
            case "state":
                requestType = GameRequest.RESOLVED_TURN;
                break;
            case "allOrders":
                requestType = GameRequest.TURN_ORDERS;
                break;
            default:
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
        }

        resp.setStatus(HttpServletResponse.SC_ACCEPTED);

        switch (requestType) {

            case RESOLVED_TURN:
                break;
            case TURN_ORDERS:
                break;
            default:
                break;
        }

        return;
    }
}

