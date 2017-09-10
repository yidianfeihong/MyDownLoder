package com.example.shiwenming_sx.mydownloader.entity;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class LoadInfo implements Serializable{


	private int mFileSize;
	private int mComplete;
	private String mUrl;


	public LoadInfo(String url, int fileSize, int complete) {

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
