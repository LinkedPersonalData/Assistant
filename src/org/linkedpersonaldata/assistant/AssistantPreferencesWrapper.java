package org.linkedpersonaldata.assistant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.media.funf.time.TimeUtil;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class AssistantPreferencesWrapper extends edu.mit.media.openpds.client.PreferencesWrapper {
	
	public AssistantPreferencesWrapper(Context context) {
		super(context);
	}

	public boolean suggestedPlacesAreCurrent() {
		List<SuggestedPlace> suggestedPlaces = getSuggestedPlaces();
		if (suggestedPlaces == null || suggestedPlaces.isEmpty()) {
			return false;
		}
		
		long lastTimestamp = mPreferences.getLong("placesTimestamp", 0);
		
		return TimeUtil.getTimestamp().longValue() - lastTimestamp < 3600;
	}
	
	public List<SuggestedPlace> getSuggestedPlaces() {
		Set<String> placesJson = mPreferences.getStringSet("places", null);
		
		if (placesJson == null) {
			return null;
		}
		
		List<SuggestedPlace> suggestedPlaces = new ArrayList<SuggestedPlace>();
		
		for (String placeJson : placesJson) {
			JSONObject placeJsonObject;
			try {
				placeJsonObject = new JSONObject(placeJson);
				SuggestedPlace suggestedPlace = new SuggestedPlace(placeJsonObject);
				suggestedPlaces.add(suggestedPlace);	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		return suggestedPlaces;
	}
	
	public void setSuggestedPlaces(List<SuggestedPlace> suggestedPlaces) {
		Set<String> suggestedPlaceStrings = new HashSet<String>();
		
		for (SuggestedPlace place : suggestedPlaces) {
			JSONObject placeObject = new JSONObject();
			try {
				JSONObject placeUri = new JSONObject();
				JSONObject reason = new JSONObject();
				JSONObject placeName = new JSONObject();
				placeUri.put("value", place.getUri());
				reason.put("value", place.getReason());
				placeName.put("value", place.getName());				
				
				placeObject.put("placeUri", placeUri);
				placeObject.put("reason", reason);
				placeObject.put("placeName", placeName);
				
				if (!suggestedPlaceStrings.contains(placeObject.toString())) {
					suggestedPlaceStrings.add(placeObject.toString());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (!suggestedPlaceStrings.isEmpty()) {
			Editor editor = mPreferences.edit();
			editor.putStringSet("places", suggestedPlaceStrings);
			editor.putLong("placesTimestamp", TimeUtil.getTimestamp().longValue()).commit();
		}
	}
	
	public Set<String> getPendingSurveys() {
		// NOTE: we're returning a copy here, rather than the original
		// This is so that modifications to the set will be saved properly if we store them in the same preferences key.
		return new HashSet<String>(mPreferences.getStringSet("surveys", null));
	}
	
	public void addPendingSurvey(String survey) {
		Editor editor = mPreferences.edit();
		Set<String> pendingSurveys = (getPendingSurveys() == null)? new HashSet<String>() : getPendingSurveys();
		
		pendingSurveys.remove(survey);
		pendingSurveys.add(survey);
	
		editor.putStringSet("surveys", pendingSurveys);
		
		editor.commit();
	}
	
	public void removePendingSurvey(String survey) {
		Editor editor = mPreferences.edit();
		
		Set<String> pendingSurveys = getPendingSurveys();
		
		if (pendingSurveys == null) {
			return;
		}
		
		pendingSurveys.remove(survey);
		
		editor.putStringSet("surveys", pendingSurveys);
		editor.commit();
	}
}
