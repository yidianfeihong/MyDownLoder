package com.example.shiwenming_sx.mydownloader.core;

import android.content.Context;
import android.content.Intent;

import com.example.shiwenming_sx.mydownloader.utils.Constants;

/**
 * Created by shiwenming_sx on 2017/9/10.
 */

public class DownloadManager {

	private static DownloadManager mInstance;
	private final Context context;


	private DownloadManager(Context context) {
		this.context = context;
		context.startService(new Intent(context, DownloadService.class));
	}

	public synchronized static DownloadManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DownloadManager(context);
		}
		return mInstance;

	}


	public void startDownload(String url) {
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_URL,url);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_STARTDOWNLOAD);
		context.startService(intent);

	}


	public void pause(String url) {

		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_URL,url);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE);
		context.startService(intent);


	}

	public void resume(String url) {

		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_URL,url);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME);
		context.startService(intent);
	}


	public void delete(String url) {

		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_URL,url);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_DELETE);
		context.startService(intent);
	}


	public void reDownload(String url) {

		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_URL,url);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_REDOWNLOAD);
		context.startService(intent);
	}


}
