package com.example.shiwenming_sx.mydownloader.core;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.widget.Toast;

import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.entity.LoadInfo;
import com.example.shiwenming_sx.mydownloader.entity.ThreadInfo;
import com.example.shiwenming_sx.mydownloader.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service {

	public static Map<String, Downloader> mDownloaders = new HashMap<String, Downloader>();
	public static List<FileStatus> mFileStatusList = new ArrayList<FileStatus>();
	public static Map<String, Integer> completeSizes = new HashMap<String, Integer>();
	private FileStatus mFileStatus = null;
	private Downloader mDownloader;


	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {

				case 0:
					Toast.makeText(DownloadService.this, "请求成功，开始下载", Toast.LENGTH_SHORT).show();
					break;
				case 1:
					updateInfo(msg);
					break;
				case 2:
					int responseCode = msg.arg1;
					if (responseCode >= 400 && responseCode < 500) {
						Toast.makeText(DownloadService.this, responseCode + "请求错误", Toast.LENGTH_SHORT).show();
					} else if (responseCode >= 500) {
						Toast.makeText(DownloadService.this, responseCode + "服务器错误", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(DownloadService.this, "状态码异常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 3:
					String str = (String) msg.obj;
					Toast.makeText(DownloadService.this, str, Toast.LENGTH_SHORT).show();
					break;

				case 4:
					LoadInfo loadInfo = (LoadInfo) msg.obj;
					FileStatus fileStatus = new FileStatus(getFileName(loadInfo.getUrl()), loadInfo.getUrl(), 0, loadInfo.getComplete(), loadInfo.getFileSize());
					fileStatus.save();
					completeSizes.put(loadInfo.getUrl(), loadInfo.getComplete());
					mFileStatusList.add(fileStatus);
					break;
				case 5:
					LoadInfo info = (LoadInfo) msg.obj;
					if (!completeSizes.containsKey(info.getUrl())) {
						completeSizes.put(info.getUrl(), info.getComplete());
					}
					break;

			}
		}

	};

	private void updateInfo(Message msg) {

		String url = (String) msg.obj;
		int length = msg.arg1;
		int completeSize = completeSizes.get(url);

		completeSize += length;
		completeSizes.put(url, completeSize);

		for (int i = 0; i < mFileStatusList.size(); i++) {
			FileStatus fileStatus = mFileStatusList.get(i);
			if (fileStatus.getUrl().equals(url)) {
				fileStatus.setCompleteSize(completeSize);
				fileStatus.setStatus(mDownloaders.get(url).getState());
				//更新数据库
				FileStatus temp = new FileStatus();
				if (completeSize == fileStatus.getFileSize()) {
					fileStatus.setStatus(Constants.COMPLETED);
					temp.setStatus(Constants.COMPLETED);
					temp.updateAll("mUrl = ?", url);
				}
				mFileStatus = fileStatus;
			}

			EventBus.getDefault().post(mFileStatus);
		}
	}


//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//
//		if (intent != null) {
//			String url = intent.getStringExtra(Constants.KEY_DOWNLOAD_URL);
//			int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
//			doAction(action, url);
//
//		}
//
//
//		return super.onStartCommand(intent, flags, startId);
//	}
//
//	private void doAction(int action, String url) {
//
//		String fileName = url.substring(url.lastIndexOf('/')+1);
//
//		switch (action) {
//			case Constants.KEY_DOWNLOAD_ACTION_STARTDOWNLOAD:
//				startDownload(fileName,url);
//				break;
//			case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
//				Pause();
//				break;
//			case Constants.KEY_DOWNLOAD_ACTION_RESUME:
//				continueDownload(fileName,url);
//				break;
//			case Constants.KEY_DOWNLOAD_ACTION_DELETE:
//				delete(fileName,url);
//				break;
//			case Constants.KEY_DOWNLOAD_ACTION_REDOWNLOAD:
//				reDownload();
//				break;
//		}
//	}

	public IBinder binder = new MyBinder();

	public class MyBinder extends Binder {

		public DownloadService getService() {
			return DownloadService.this;
		}

	}

	public DownloadService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}


	@Override
	public void onCreate() {
		super.onCreate();

		mFileStatusList = DataSupport.findAll(FileStatus.class);
	}


	public void startDownload(final String fileName, final String url) {

		if (DataSupport.where("mUrl = ?", url).count(FileStatus.class) > 0) {

			Toast.makeText(this, "任务已在列表中", Toast.LENGTH_SHORT).show();
			return;
		}

		mDownloader = mDownloaders.get(url);

		if (mDownloader == null) {
			mDownloader = new Downloader(fileName, url, mHandler);
			mDownloaders.put(url, mDownloader);
		}

		if (mDownloader.isDownloading()) {
			return;
		}

		mDownloader.start();

	}


	//暂停下载
	public void Pause(FileStatus fileStatus) {

		Downloader downloader = mDownloaders.get(fileStatus.getUrl());
		downloader.pause();
	}

	//继续下载
	public void continueDownload(final String fileName, final String url) {

		mDownloader = mDownloaders.get(url);
		if (mDownloader == null) {
			mDownloader = new Downloader(fileName, url, mHandler);
			mDownloaders.put(url, mDownloader);
		}
		if (mDownloader.isDownloading())
			return;

		mDownloader.start();

	}

	public void delete(String fileName, final String url) {
		Downloader down = mDownloaders.get(url);
		if (down != null) {
			down.pause();
		}

		DataSupport.deleteAll(FileStatus.class, "mUrl = ?", url);
		DataSupport.deleteAll(ThreadInfo.class, "mUrl = ?", url);


		File file = new File(Constants.mSavePath + fileName);
		if (file.exists()) {
			file.delete();
		}

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < mFileStatusList.size(); i++) {
					FileStatus fileStatus = mFileStatusList.get(i);
					if (fileStatus.getUrl().equals(url)) {
						mFileStatusList.remove(i);
					}
				}

				mDownloaders.remove(url);
				completeSizes.remove(url);

				EventBus.getDefault().post(url);

			}
		}, 500);

	}


	//暂停下载
	public void reDownload(final FileStatus fileStatus) {


		final String fileName = fileStatus.getFileName();
		final String url = fileStatus.getUrl();

		Downloader downloader = mDownloaders.get(url);
		if (downloader != null) {
			downloader.pause();
		}

		downloader = new Downloader(fileName, url, mHandler);


		ContentValues values = new ContentValues();
		values.put("mCompleteSize", "0");
		DataSupport.updateAll(ThreadInfo.class, values, "mUrl = ?", url);

//		DataSupport.deleteAll(ThreadInfo.class, "mUrl = ?", url);
		mDownloaders.put(url, downloader);

		downloader.start();

		FileStatus stat = new FileStatus();
		stat.setCompleteSize(0);
		stat.setStatus(0);
		stat.updateAll("mUrl = ?", url);

		completeSizes.put(url, 0);


	}


//
//	public interface DownLoadCallback {
//		public void refreshUI(FileStatus fileStatus);
//
//		public void deleteFile(String url);
//	}
//
//	public void setLoadCallback(DownLoadCallback loadCallback) {
//		this.loadCallback = loadCallback;
//	}

	private String getFileName(String url) {
		int start = url.lastIndexOf("/") + 1;
		return url.substring(start);
	}

}
