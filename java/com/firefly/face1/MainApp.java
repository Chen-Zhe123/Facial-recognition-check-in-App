package com.firefly.face1;

import android.app.Application;

import com.firefly.api.FireflyApi;

public class MainApp extends Application {

	private static FireflyApi mFireflyApi;
	public static FireflyApi getFireflyApi() {
		return mFireflyApi;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mFireflyApi = FireflyApi.getInstance();
	}

}
