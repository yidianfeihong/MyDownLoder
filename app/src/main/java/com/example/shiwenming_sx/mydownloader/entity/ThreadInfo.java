package com.example.shiwenming_sx.mydownloader.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class ThreadInfo extends DataSupport {

	private int mThreadId;
	private int mStartPos;
	private int mEndPos;
	private int mCompleteSize;
	private String mUrl;

	public ThreadInfo() {

	}

	public ThreadInfo(int threadId, int startPos, int endPos, int completeSize, String url) {
		mThreadId = threadId;
		mStartPos = startPos;
		mEndPos = endPos;
		mCompleteSize = completeSize;
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

	public int getCompleteSize() {
		return mCompleteSize;
	}

	public void setCompleteSize(int completeSize) {
		mCompleteSize = completeSize;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}
}
