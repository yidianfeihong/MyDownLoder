package com.example.shiwenming_sx.mydownloader.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class LoadInfo {


	private int mFileSize;
	private int mComplete;
	private String mUrl;


	public LoadInfo(int fileSize, int complete, String url) {

		mFileSize = fileSize;
		mComplete = complete;
		mUrl = url;
	}


	public int getFileSize() {
		return mFileSize;
	}

	public void setFileSize(int fileSize) {
		mFileSize = fileSize;
	}

	public int getComplete() {
		return mComplete;
	}

	public void setComplete(int complete) {
		mComplete = complete;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}


}
