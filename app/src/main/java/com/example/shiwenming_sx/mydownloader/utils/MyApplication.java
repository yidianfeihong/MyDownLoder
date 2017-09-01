package com.example.shiwenming_sx.mydownloader.utils;

import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by shiwenming_sx on 2017/9/1.
 */

public class MyApplication extends LitePalApplication {


	private static Context mContext;


	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
	}

	public static Context getContext() {
		return mContext;
	}
}
