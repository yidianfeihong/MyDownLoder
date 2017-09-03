package com.example.shiwenming_sx.mydownloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.entity.LoadInfo;
import com.example.shiwenming_sx.mydownloader.entity.ThreadInfo;
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
						Toast.makeText(DownloadService.this, responseCode+"请求错误", Toast.LENGTH_SHORT).show();
					} else if (responseCode >= 500) {
						Toast.makeText(DownloadService.this, responseCode+"服务器错误", Toast.LENGTH_SHORT).show();
					}else {
						Toast.makeText(DownloadService.this, "状态码异常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 3:
					String str = ((Exception) msg.obj).toString();
					Toast.makeText(DownloadService.this, str, Toast.LENGTH_SHORT).show();
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

		synchronized (mFileStatusList) {
			for (int i = 0; i < mFileStatusList.size(); i++) {
				FileStatus fileStatus = mFileStatusList.get(i);
				if (fileStatus.getUrl().equals(url)) {
					fileStatus.setCompleteSize(completeSize);
					//更新数据库
					FileStatus temp = new FileStatus();
					if (completeSize == fileStatus.getFileSize()) {

						temp.setStatus(1);
						temp.updateAll("mUrl = ?", url);

					} else {

						temp.setStatus(0);
					}

					mFileStatus = fileStatus;
				}
			}


			if (loadCallback != null && mFileStatus != null) {
				loadCallback.refreshUI(mFileStatus);
			}
		}
	}


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

				LoadInfo loadInfo = mDownloader.getDownloaderInfos();

				if (loadInfo != null) {
					FileStatus fileStatus = new FileStatus(fileName, url, 0, loadInfo.getComplete(), loadInfo.getFileSize());
					fileStatus.save();
					completeSizes.put(url, loadInfo.getComplete());
					mFileStatusList.add(fileStatus);
					Message message = Message.obtain();
					message.arg1 = 0;
					mHandler.sendMessage(message);
					mDownloader.download();

				}


			}
		}).start();
	}

	//暂停下载
	public void Pause(Downloader downloader) {
		downloader.pause();
	}

	//继续下载
	public void continueDownload(final String fileName, final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mDownloader = mDownloaders.get(url);
				if (mDownloader == null) {
					mDownloader = new Downloader(fileName, url, mHandler);
					mDownloaders.put(url, mDownloader);
				}
				if (mDownloader.isDownloading())
					return;

				LoadInfo loadInfo = mDownloader.getDownloaderInfos();

				if (loadInfo != null && !fileName.equals("")) {
					if (!completeSizes.containsKey(url)) {
						completeSizes.put(url, loadInfo.getComplete());
					}
					mDownloader.download();

					Toast.makeText(DownloadService.this, "继续下载成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(DownloadService.this, "继续下载失败", Toast.LENGTH_SHORT).show();
				}
			}
		}).start();
	}

	public void delete(final String url) {
		Downloader down = mDownloaders.get(url);
		if (down != null) {
			down.pause();
		}

		DataSupport.deleteAll(FileStatus.class, "mUrl = ?", url);
		DataSupport.deleteAll(ThreadInfo.class, "mUrl = ?", url);


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
				if (loadCallback != null) {
					loadCallback.deleteFile(url);
				}
			}
		}, 500);

	}


	//暂停下载
	public void reDownload(final FileStatus fileStatus) {


		final String fileName = fileStatus.getFileName();
		final String url = fileStatus.getUrl();
		new Thread(new Runnable() {
			@Override
			public void run() {

				Downloader downloader = mDownloaders.get(url);
				if (downloader != null) {
					downloader.pause();
				}

				downloader = new Downloader(fileName, url, mHandler);

				DataSupport.deleteAll(ThreadInfo.class, "mUrl = ?", url);
				mDownloaders.put(url, downloader);

				LoadInfo loadInfo = downloader.getDownloaderInfos();
				if (loadInfo != null) {

					FileStatus stat = new FileStatus();
					stat.setCompleteSize(0);
					stat.setStatus(0);
					stat.updateAll("mUrl = ?", url);
					completeSizes.put(url, loadInfo.getComplete());
					downloader.download();

					Toast.makeText(DownloadService.this, "重新下载成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(DownloadService.this, "重新下载失败", Toast.LENGTH_SHORT).show();
				}


			}
		}).start();


	}


	public interface DownLoadCallback {
		public void refreshUI(FileStatus fileStatus);

		public void deleteFile(String url);
	}

	public void setLoadCallback(DownLoadCallback loadCallback) {
		this.loadCallback = loadCallback;
	}


}
