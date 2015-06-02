package com.example.RichardPalomino15.myapplication.backend.oauth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by RichardPalomino on 5/13/2015.
 */

public class OAuthCallbackServlet extends AbstractAppEngineAuthorizationCodeCallbackServlet {

	private static final DatastoreService DATASTORE_SERVICE = DatastoreServiceFactory.getDatastoreService();
	private static final AppEngineDataStoreFactory APP_ENGINE_DATA_STORE_FACTORY = AppEngineDataStoreFactory.getDefaultInstance();

	private static final String CLIENT_FILE_NAME = "client_secret_683634772464-nd7s4s1d32thmv9pcbnlelolpno7ssb9.apps.googleusercontent.com.json";
	static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();


	@Override
	protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential) throws ServletException, IOException {
		StoredCredential storeCredential = new StoredCredential(credential);
		DataStore<StoredCredential> newDataStore = APP_ENGINE_DATA_STORE_FACTORY.getDataStore("credentials");
		//Update with actual username
		newDataStore.set("heyyou", storeCredential);
		resp.setStatus(HttpServletResponse.SC_OK);
		//Have some nice confirmation page
		resp.sendRedirect("http://127.0.0.1:8080/index.html");
	}

	@Override
	protected void onError(HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse) throws ServletException, IOException {
		System.out.println("Error!");
	}

	protected static AuthorizationCodeFlow GoogleOAuthFlow() throws IOException {
		GoogleClientSecrets secretSecret = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(OAuthCallbackServlet.class.getResourceAsStream(String.format("/%s", CLIENT_FILE_NAME))));
		return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				secretSecret.getDetails().getClientId(), secretSecret.getDetails().getClientSecret(), Collections.singleton("profile")).setApprovalPrompt("auto").build();
	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
		return GoogleOAuthFlow();
	}

	@Override
	protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
		System.out.println("Started from the bottom\n");
		GenericUrl home = new GenericUrl(req.getRequestURL().toString());
		home.setRawPath("/oauth2callback");
		System.out.println(home.build());
		return home.build();
	}

	@Override
	protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
		return "";
	}
}

