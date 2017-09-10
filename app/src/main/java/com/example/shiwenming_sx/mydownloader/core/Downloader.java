package com.example.shiwenming_sx.mydownloader.core;

import android.os.Handler;
import android.os.Message;

import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.entity.LoadInfo;
import com.example.shiwenming_sx.mydownloader.entity.ThreadInfo;
import com.example.shiwenming_sx.mydownloader.utils.Constants;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;


/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class Downloader implements InitThread.InitListener {
	private String mDownPath;
	private String mSavePath;

	private String mFileName;
	private int mThreadCount;
	private Handler mHandler;

	private int mFileSize;
	private InitThread mInitThread;

	private List<ThreadInfo> mThreadInfoList;// 存放下载信息类的集合
	private int mState = Constants.INIT;


	public Downloader(String fileName, String url, Handler handler) {
		mSavePath = Constants.mSavePath;
		mDownPath = url;
		mFileName = fileName;
		mHandler = handler;
		mThreadCount = Constants.THREAD_COUNT;

	}


	public boolean isDownloading() {
		return mState == Constants.DOWNLOADING;
	}


	public boolean isPaused() {
		return mState == Constants.PAUSED;
	}

	public int getState() {
		return mState;
	}

	public void setState(int state) {
		mState = state;
	}

	/**
	 * 利用线程开始下载数据
	 */
	public void download() {
		if (mThreadInfoList != null) {
			if (mState == Constants.DOWNLOADING) {
				return;
			}
			mState = Constants.DOWNLOADING;// 把状态设置为正在下载
			for (ThreadInfo info : mThreadInfoList) {
				ThreadPoolManager.getThreadPool().execute(new MyThread(info.getThreadId(), info.getStartPos(), info.getEndPos(), info.getCompleteSize(), info.getUrl()));
			}
		}
	}

	// 设置暂停
	public void pause() {
		mState = Constants.PAUSED;
	}

	// 重置下载状态,将下载状态设置为init初始化状态
	public void reset() {
		mState = Constants.INIT;
	}

	/**
	 * 判断是否是第一次下载，利用litepal查询数据库中是否有下载这个地址的记录
	 */
	private boolean isFirst(String url) {
		return DataSupport.where("mUrl = ?", url).count(ThreadInfo.class) == 0;
	}


	private void constructHttps(HttpURLConnection conn) throws Exception {

		if (conn instanceof HttpsURLConnection) {
			SSLContext context = null;

			context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[]{new TrustAllManager()}, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
		}
	}


	@Override
	public void onConnected(int totalLength) {

		mFileSize = totalLength;

		int range = mFileSize / mThreadCount;
		mThreadInfoList = new ArrayList<>();
		for (int i = 0; i < mThreadCount - 1; i++) {
			ThreadInfo info = new ThreadInfo(i, i * range, (i + 1) * range - 1, 0, mDownPath);
			mThreadInfoList.add(info);
		}

		ThreadInfo info = new ThreadInfo(mThreadCount - 1, (mThreadCount - 1) * range, mFileSize, 0, mDownPath);
		mThreadInfoList.add(info);
		for (ThreadInfo item : mThreadInfoList) {
			item.save();
		}

		Message message = Message.obtain();
		message.what = 4;
		message.obj = new LoadInfo(mDownPath, mFileSize, 0);
		mHandler.sendMessage(message);

		download();
	}

	@Override
	public void onWrongResponse(int responseCode) {
		Message ms = Message.obtain();
		ms.what = 2;
		ms.arg1 = responseCode;
		mHandler.sendMessage(ms);
	}

	@Override
	public void onError(String message) {
		Message ms = Message.obtain();
		ms.what = 3;
		ms.obj = message;
		mHandler.sendMessage(ms);

	}

	public void start() {

		if (isFirst(mDownPath)) {
			mInitThread = new InitThread(mDownPath, mFileName, this);
			ThreadPoolManager.getThreadPool().execute(mInitThread);
		} else {
			mThreadInfoList = DataSupport.where("mUrl = ?", mDownPath).find(ThreadInfo.class);
			if (mThreadInfoList != null && mThreadInfoList.size() > 0) {
				int size = 0;
				int completeSize = 0;
				for (ThreadInfo info : mThreadInfoList) {
					completeSize += info.getCompleteSize();
					size += info.getEndPos() - info.getStartPos() + mThreadCount - 1;
				}

				Message message = Message.obtain();
				message.what = 5;
				message.obj = new LoadInfo(mDownPath, size, completeSize);
				mHandler.sendMessage(message);

				download();
			}
		}

	}


	public class MyThread implements Runnable {
		private int threadId;
		private int startPos;
		private int endPos;
		private int compeleteSize;
		private String urlstr;


		public MyThread(int threadId, int startPos, int endPos, int compeleteSize, String urlstr) {
			this.threadId = threadId;
			this.startPos = startPos;
			this.endPos = endPos;
			this.compeleteSize = compeleteSize;
			this.urlstr = urlstr;

		}

		@Override
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile randomAccessFile = null;
			InputStream inStream = null;
			File file = new File(mSavePath, mFileName);
			try {
				URL url = new URL(this.urlstr);
				conn = (HttpURLConnection) url.openConnection();
				constructHttps(conn);
				constructConn(conn);
				int responseCode = conn.getResponseCode();
				System.out.println("responseCode:" + conn.getResponseCode());
				// 如果http返回的代码是302则获取跳转地址重新发起请求
				if (responseCode == 302) {
					String location = conn.getHeaderField("Location");
					url = new URL(location);
					conn = (HttpURLConnection) url.openConnection();
					constructConn(conn);
					responseCode = conn.getResponseCode();
				}

				if (responseCode == 200 || responseCode == 206) {
					randomAccessFile = new RandomAccessFile(file, "rwd");
					randomAccessFile.seek(this.startPos + this.compeleteSize);
					inStream = conn.getInputStream();
					byte buffer[] = new byte[4096];
					int length = 0;

					while ((length = inStream.read(buffer, 0, buffer.length)) != -1) {
						randomAccessFile.write(buffer, 0, length);
						compeleteSize += length;
						// 更新数据库中的下载信息

						ThreadInfo threadInfo = new ThreadInfo();
						threadInfo.setCompleteSize(compeleteSize);
						threadInfo.updateAll("mUrl = ? and mThreadId= ?", mDownPath, threadId + "");


						FileStatus status = new FileStatus();
						int sum = DataSupport.where("mUrl = ?", urlstr).sum(ThreadInfo.class, "mCompleteSize", int.class);
						status.setCompleteSize(sum);
						status.updateAll("mUrl = ?", mDownPath);

						// 用消息将下载信息传给进度条，对进度条进行更新
						Message message = Message.obtain();
						message.what = 1;
						message.obj = urlstr;
						message.arg1 = length;
						mHandler.sendMessage(message);// 给DownloadService发送消息
						if (mState == Constants.PAUSED) {
							System.out.println("-----pause-----");
							return;
						}
					}
					System.out.println("------------线程:" + this.threadId + "下载完成");
				}
			} catch (Exception e) {
				System.out.println("-----下载异常-----");
				pause();
				e.printStackTrace();
			} finally {
				try {
					if (inStream != null) {
						inStream.close();
					}
					if (randomAccessFile != null) {
						randomAccessFile.close();
					}
					if (conn != null) {
						conn.disconnect();
					}
				} catch (Exception e) {
					pause();
					e.printStackTrace();

				}
			}

		}

		/**
		 * 构建请求连接时的参数
		 */
		private void constructConn(HttpURLConnection conn) throws IOException {

			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Charset", "UTF-8");
			int startPosition = startPos + compeleteSize;
			// 设置获取实体数据的范围
			conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + this.endPos);
			conn.connect();
		}

	}

}
