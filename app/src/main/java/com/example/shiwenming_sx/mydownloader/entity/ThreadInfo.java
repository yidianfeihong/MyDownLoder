package com.example.shiwenming_sx.mydownloader.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class ThreadInfo extends DataSupport {

	private int mThreadId;
	private int mStartPos;
	private int mEndPos;
	private int mCompeleteSize;
	private String mUrl;

	public ThreadInfo() {

	}

	public ThreadInfo(int threadId, int startPos, int endPos, int compeleteSize, String url) {
		mThreadId = threadId;
		mStartPos = startPos;
		mEndPos = endPos;
		mCompeleteSize = compeleteSize;
		mUrl = url;
	}

	public int getThreadId() {
		return mThreadId;
	}

	public void setThreadId(int threadId) {
		mThreadId = threadId;
	}

	public int getStartPos() {
		return mStartPos;
	}

	public void setStartPos(int startPos) {
		mStartPos = startPos;
	}

	public int getEndPos() {
		return mEndPos;
	}

	public void setEndPos(int endPos) {
		mEndPos = endPos;
	}

	public int getCompeleteSize() {
		return mCompeleteSize;
	}

	public void setCompeleteSize(int compeleteSize) {
		mCompeleteSize = compeleteSize;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}
}
