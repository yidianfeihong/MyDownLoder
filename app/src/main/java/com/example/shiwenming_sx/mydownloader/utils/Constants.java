package com.example.shiwenming_sx.mydownloader.utils;

/**
 * Created by shiwenming_sx on 2017/9/10.
 */

public class Constants {


	public static String mSavePath = "/sdcard/MyDownloader/";

	public static final String KEY_DOWNLOAD_STATUS = "key_download_files_status";
	public static final String KEY_DOWNLOAD_URL = "key_download_url";
	public static final int KEY_DOWNLOAD_ACTION_STARTDOWNLOAD = 1;
	public static final int KEY_DOWNLOAD_ACTION_PAUSE = 2;
	public static final int KEY_DOWNLOAD_ACTION_RESUME = 3;
	public static final int KEY_DOWNLOAD_ACTION_DELETE = 4;
	public static final int KEY_DOWNLOAD_ACTION_REDOWNLOAD = 5;
	public static final String KEY_DOWNLOAD_ACTION = "key_download_action";


	public static final int INIT = 0;// 定义四种种下载的状态：初始化状态，正在下载状态，暂停状态，完成状体
	public static final int DOWNLOADING = 1;
	public static final int PAUSED = 2;
	public static final int COMPLETED = 3;

	public static final int THREAD_COUNT = 3;


}
