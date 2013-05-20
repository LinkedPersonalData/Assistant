package org.linkedpersonaldata.assistant;

import android.app.Activity;
import android.support.v4.view.ViewPager;


public class WebViewFragmentJavascriptInterface {
	
	protected ViewPager mViewPager;
	protected Activity mActivity;
	
	public WebViewFragmentJavascriptInterface(ViewPager viewPager, Activity activity) {
		mViewPager = viewPager;
		mActivity = activity;
	}
	
	public boolean hideWebNavBar() {
		return true;
	}
	
	public boolean handleTabChange(final String dimension, final int tabNumber) {
		if (mActivity != null && mViewPager != null) {
			mActivity.runOnUiThread(new Runnable() { 
				public void run() {
					mViewPager.setCurrentItem(tabNumber + 1);
				}
			});
		}
		return true;
	}
	
	private String mPlaceUri;
	
	public void setPlaceUri(String uri) {
		mPlaceUri = uri;
	}
	
	public String getPlaceUri() {
		return mPlaceUri;
	}
}