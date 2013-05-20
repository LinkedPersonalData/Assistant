package org.linkedpersonaldata.assistant;

import org.json.JSONException;
import org.json.JSONObject;

public class SuggestedPlace {
	private String mPlaceUri;
	private String mReason;
	private String mPlaceName;
	
	public SuggestedPlace(JSONObject place) throws JSONException {
		mPlaceUri = place.getJSONObject("placeUri").getString("value");
		mReason = place.getJSONObject("reason").getString("value");
		mPlaceName = place.getJSONObject("placeName").getString("value");
	}
	
	public String getUri() {
		return mPlaceUri;
	}
	
	public String getReason() {
		return mReason;
	}
	
	public String getName() {
		return mPlaceName;
	}
}
