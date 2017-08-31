package com.example.shiwenming_sx.mydownloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.entity.LoadInfo;
import com.example.shiwenming_sx.mydownloader.utils.Downloader;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadService extends Service {

	public static Map<String, Downloader> mDownloaders = new HashMap<String, Downloader>();
	public static List<FileStatus> mFileStatusList = new ArrayList<FileStatus>();
	private static Map<String, Integer> completeSizes = new HashMap<String, Integer>();
	private FileStatus mFileStatus = null;

	private Downloader mDownloader;
	private DownLoadCallback loadCallback;


	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (msg.what == 1) {

				System.out.println(msg.obj);
				String url = (String) msg.obj;
				int length = msg.arg1;
				int completeSize = completeSizes.get(url);

				completeSize += length;
				completeSizes.put(url, completeSize);

				synchronized (mFileStatusList) {
					for (int i = 0; i < mFileStatusList.size(); i++) {
						FileStatus fileStatus = mFileStatusList.get(i);
						if (fileStatus.getUrl().equals(url)) {

							if (completeSize == fileStatus.getFileSize()) {
								System.out.println("-----------下载完成:" + fileStatus.getFileName() + ":" + completeSize + "-----" + fileStatus.getFileSize());
								FileStatus status = new FileStatus(fileStatus.getFileName(), fileStatus.getUrl(), 1, completeSize, fileStatus.getFileSize());
								mFileStatusList.set(i, status);
								status.updateAll("mUrl = ?", url);

							} else {
								mFileStatusList.set(i, new FileStatus(fileStatus.getFileName(), fileStatus.getUrl(), 0, completeSize, fileStatus.getFileSize()));
							}
							mFileStatus = mFileStatusList.get(i);
						}
					}


					if (loadCallback != null && mFileStatus != null) {
						loadCallback.refreshUI(mFileStatusList);
					}
				}
			}
		}
	};

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

	public void startDownload(final String fileName, final String url, final Handler handler) {

		if (DataSupport.where("mUrl = ?", url).count(FileStatus.class)>0) {

			Toast.makeText(this, "任务已在下载中", Toast.LENGTH_SHORT).show();
			return;
		}


		new Thread(new Runnable() {
			@Override
			public void run() {

				mDownloader = mDownloaders.get(url);
				if (mDownloader == null) {
					mDownloader = new Downloader(fileName, url, mHandler);
					mDownloaders.put(url, mDownloader);
				}

				if (mDownloader.isDownloading()) {
					return;
				}

				LoadInfo loadInfo = mDownloader.getDownloaderInfors();

				if (loadInfo != null) {
					FileStatus fileStatus = new FileStatus(fileName, url, 0, loadInfo.getComplete(), loadInfo.getFileSize());
					fileStatus.save();
					completeSizes.put(url, loadInfo.getComplete());

					mFileStatusList.add(fileStatus);
					mDownloader.download();

					Message msg = new Message();
					msg.what = 1;
					handler.sendMessage(msg);
				} else {
					Message msg = new Message();
					msg.what = 2;
					handler.sendMessage(msg);
				}


			}
		}).start();
	}

	//暂停下载
	public void Pause(Downloader downloader) {
		downloader.pause();
	}

//	//继续下载
//	public void reDownload(final Button button, final String url, final String name, final Handler mHandler) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				String fileName = dao.getFileName(url);
//
//				downloader = downloaders.get(url);
//				if (downloader == null) {
//					downloader = new Downloader(url, savePath, fileName, threadCount, DownloadService.this, handler);
//					downloaders.put(url, downloader);
//				}
//				if (downloader.isDownloading())
//					return;
//
//				LoadInfo loadInfo = downloader.getDownloaderInfors();
//
//				if (loadInfo != null && !fileName.equals("")) {
//					if (!completeSizes.containsKey(url)) {
//						completeSizes.put(url, loadInfo.getComplete());
//					}
//					if (!fileSizes.containsKey(url)) {
//						fileSizes.put(url, loadInfo.getFileSize());
//					}
//
//					downloader.download();
//
//					Message msg = new Message();
//					msg.what = 1;
//					msg.obj = button;
//					mHandler.sendMessage(msg);
//				} else {
//					Message msg = new Message();
//					msg.what = 2;
//					msg.obj = button;
//					mHandler.sendMessage(msg);
//				}
//			}
//		}).start();
//	}

	public void delete(final String url) {
		Downloader down = mDownloaders.get(url);
		if (down != null) {
			down.pause();
		}

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				DataSupport.deleteAll(FileStatus.class, "mUrl = ?", url);

				for (int i = 0; i < mFileStatusList.size(); i++) {
					FileStatus fileStatus = mFileStatusList.get(i);
					if (fileStatus.getUrl().equals(url)) {
						mFileStatusList.remove(i);
					}
				}

				mDownloaders.remove(url);
				completeSizes.remove(url);
				if (loadCallback != null) {
					loadCallback.deleteFile(url);
				}
			}
		}, 1000);
	}

	public interface DownLoadCallback {
		public void refreshUI(List<FileStatus> fileStatuses);

		public void deleteFile(String url);
	}

	public void setLoadCallback(DownLoadCallback loadCallback) {
		this.loadCallback = loadCallback;
	}


}
