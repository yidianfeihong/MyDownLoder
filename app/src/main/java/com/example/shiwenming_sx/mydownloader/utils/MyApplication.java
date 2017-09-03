package com.example.shiwenming_sx.mydownloader.utils;

import android.app.Application;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * Created by shiwenming_sx on 2017/9/1.
 */

public class MyApplication extends Application {


	private static Context mContext;


	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		LitePal.initialize(mContext);
	}

	public static Context getContext() {
		return mContext;
	}
}
