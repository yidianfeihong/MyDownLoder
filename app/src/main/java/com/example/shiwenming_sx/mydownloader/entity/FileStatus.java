package com.example.shiwenming_sx.mydownloader.entity;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

public class FileStatus extends DataSupport implements Serializable {


	private String mFileName;//文件名字
	private String mUrl;//下载地址
	private int mStatus;//当前状态 0为正在下载 1为已下载
	private int mCompleteSize;//已下载的长度
	private int mFileSize;//文件长度

	public FileStatus() {

	}

	public FileStatus(String fileName, String url, int status, int completeSize, int fileSize) {
		mFileName = fileName;
		mUrl = url;
		mStatus = status;
		mCompleteSize = completeSize;
		mFileSize = fileSize;
	}

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String fileName) {
		mFileName = fileName;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public int getStatus() {
		return mStatus;
	}

	public void setStatus(int status) {
		mStatus = status;
	}

	public int getCompleteSize() {
		return mCompleteSize;
	}

	public void setCompleteSize(int completeSize) {
		mCompleteSize = completeSize;
	}

	public int getFileSize() {
		return mFileSize;
	}

	public void setFileSize(int fileSize) {
		mFileSize = fileSize;
	}
}
