package org.linkedpersonaldata.assistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.media.funf.FunfManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	private ViewPager mViewPager;
	private HashSet<String> mSurveysShown = new HashSet<String>();
	
	static List<SuggestedPlace> suggestedPlaces = new ArrayList<SuggestedPlace>();	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AssistantPreferencesWrapper prefs = new AssistantPreferencesWrapper(this);
		
		PDSWrapper pds = null;
		
		try {
			pds = new PDSWrapper(this);
		} catch (Exception ex) {
			
		}
		
		if (pds != null) {
			setContentView(R.layout.activity_main);
			
			String placesUrl = pds.buildAbsoluteUrl(R.string.places_relative_url);		
			
			Intent placeIntent = getIntent();
			
			if (placeIntent != null && !TextUtils.isEmpty(placeIntent.getDataString())) {
				String uri = placeIntent.getDataString();
				placesUrl += "&place=" + uri.replace("lpd://assistant/", "");
			}
			
			getSupportFragmentManager().beginTransaction().add(R.id.main_content_layout, WebViewFragment.Create(placesUrl, "Places", this, null)).commit();
			//NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			//NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			//builder.setContentTitle("Need gas?").setContentText("River St. Shell  - on the way to work").setSmallIcon(R.drawable.ic_launcher);
			

			//notificationManager.notify(0, builder.build());
			//builder.setContentTitle("Lunch Suggestion").setContentText("Legal Seafood - near work").setSmallIcon(R.drawable.ic_launcher);
			//notificationManager.notify(1, builder.build());
				
			// TODO: make this not suck
			final PDSWrapper localPds = pds;
			new Thread() {				
				@Override
				public void run() {
					suggestedPlaces = localPds.getSuggestedPlaces();					
				}
			}.start();
		
		} else {
			startLoginActivity();
			finish();
		}
	}
	
	private void startLoginActivity() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.suggested_places_menu_item:
				startActivity(new Intent(this, PlaceListActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	
}
