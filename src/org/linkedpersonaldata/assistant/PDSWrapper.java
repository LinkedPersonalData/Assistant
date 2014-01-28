package org.linkedpersonaldata.assistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import edu.mit.media.openpds.client.PersonalDataStore;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class PDSWrapper extends PersonalDataStore {
	private AssistantPreferencesWrapper mPrefs;
	
	public PDSWrapper(Context context) throws Exception {
		super(context);
		mPrefs = new AssistantPreferencesWrapper(context);
		assert(mPrefs.getAccessToken() != null && mPrefs.getPDSLocation() != null && mPrefs.getUUID() != null);
	}

	private String getSparqlUrl() {
		return String.format("%s/%s/sparql", mPrefs.getPDSLocation(), mPrefs.getUUID());
	}
	
	public List<SuggestedPlace> getSuggestedPlaces() {
		if (mPrefs.getSuggestedPlaces() != null && mPrefs.suggestedPlacesAreCurrent()) {
			return mPrefs.getSuggestedPlaces();
		}
		
		List<SuggestedPlace> suggestedPlaces = new ArrayList<SuggestedPlace>();
		StringBuilder sparqlBuilder = new StringBuilder();
		sparqlBuilder.append("prefix lpd: <http://linkedpersonaldata.org/ontology#> ");
		sparqlBuilder.append("prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		sparqlBuilder.append("prefix spatial: <http://geovocab.org/spatial#> ");
		sparqlBuilder.append("select distinct ?placeUri ?placeName ?reason ");
		sparqlBuilder.append("where { ?s lpd:hasSuggestion ?suggestion . ");
		sparqlBuilder.append(" ?suggestion spatial:Feature ?placeUri . ");
		sparqlBuilder.append(" ?suggestion lpd:reason ?reasonUri ");
		sparqlBuilder.append(" ?reasonUri rdfs:label ?reason");
		sparqlBuilder.append("  service <http://live.linkedgeodata.org/sparql> { ?placeUri rdfs:label ?placeName } }");
		Uri.Builder requestUriBuilder = Uri.parse(getSparqlUrl()).buildUpon();
		requestUriBuilder.appendQueryParameter("query", sparqlBuilder.toString());
		requestUriBuilder.appendQueryParameter("format", "json");
		HttpGet getPlacesRequest = new HttpGet(requestUriBuilder.build().toString());
		getPlacesRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
		HttpClient client = new DefaultHttpClient();
		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		
		try {
			responseBody = client.execute(getPlacesRequest, responseHandler);			
		} catch (ClientProtocolException e) {
	        client.getConnectionManager().shutdown();  
			return null;
		} catch (IOException e) {
	        client.getConnectionManager().shutdown();  
			return null;
		}
		
		try {
			JSONObject placesBody = new JSONObject(responseBody);
			JSONArray bindings = placesBody.getJSONObject("results").getJSONArray("bindings");
			
			for (int i = 0; i < bindings.length(); i++) {
				JSONObject place = bindings.optJSONObject(i);
				
				try {
					SuggestedPlace suggestedPlace = new SuggestedPlace(place);
					suggestedPlaces.add(suggestedPlace);
				} catch (JSONException e) {
					Log.e(this.getClass().getName(), "Error while parsing suggested places response", e);
				}
			}
		} catch (JSONException e) {
			return null;
		}
		
		mPrefs.setSuggestedPlaces(suggestedPlaces);
		
		return suggestedPlaces;
	}
}
