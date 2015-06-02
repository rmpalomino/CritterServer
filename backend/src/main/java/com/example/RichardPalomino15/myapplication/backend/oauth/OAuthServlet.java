package com.example.RichardPalomino15.myapplication.backend.oauth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.http.GenericUrl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by RichardPalomino on 5/13/2015.
 */
public class OAuthServlet extends AbstractAppEngineAuthorizationCodeServlet {



	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
		return OAuthCallbackServlet.GoogleOAuthFlow();
	}

	@Override
	protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
		GenericUrl url = new GenericUrl(req.getRequestURL().toString());
		url.setRawPath("/oauth2callback");
		return url.build();
	}

	@Override
	protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
		return "";
	}

}