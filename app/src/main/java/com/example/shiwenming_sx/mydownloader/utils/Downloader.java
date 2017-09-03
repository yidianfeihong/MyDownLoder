package com.example.shiwenming_sx.mydownloader.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.entity.LoadInfo;
import com.example.shiwenming_sx.mydownloader.entity.ThreadInfo;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class Downloader {
	private String mDownPath;
	private String mSavePath;

	private String mFileName;
	private int mThreadCount = 5;
	private Handler mHandler;

	private int mFileSize;

	private List<ThreadInfo> infos;// 存放下载信息类的集合
	private int state = INIT;
	private static final int INIT = 1;// 定义三种下载的状态：初始化状态，正在下载状态，暂停状态
	private static final int DOWNLOADING = 2;
	private static final int PAUSE = 3;


	public Downloader(String fileName, String savePath, String url, Handler handler) {
		mSavePath = savePath;
		mDownPath = url;
		mFileName = fileName;
		mHandler = handler;

	}


	public boolean isDownloading() {
		return state == DOWNLOADING;
	}


	public boolean isPause() {
		return state == PAUSE;
	}


	/**
	 * 获取下载器基本信息，用以判断状态是否正常
	 **/

	public LoadInfo getDownloaderInfos() {
		if (isFirst(mDownPath)) {
			if (init())// 第1次下载要进行初始化
			{
				int range = mFileSize / mThreadCount;
				infos = new ArrayList<ThreadInfo>();
				for (int i = 0; i < mThreadCount - 1; i++) {
					ThreadInfo info = new ThreadInfo(i, i * range, (i + 1) * range - 1, 0, mDownPath);
					infos.add(info);
				}

				ThreadInfo info = new ThreadInfo(mThreadCount - 1, (mThreadCount - 1) * range, mFileSize, 0, mDownPath);
				infos.add(info);
				for (ThreadInfo item : infos) {
					item.save();
				}

				LoadInfo loadInfo = new LoadInfo(mFileSize, 0, mDownPath);
				return loadInfo;
			}
			return null;
		} else {
			infos = DataSupport.where("mUrl = ?", mDownPath).find(ThreadInfo.class);
			if (infos != null && infos.size() > 0) {
				int size = 0;
				int completeSize = 0;
				for (ThreadInfo info : infos) {
					completeSize += info.getCompleteSize();
					size += info.getEndPos() - info.getStartPos() + mThreadCount - 1;
				}
				LoadInfo loadInfo = new LoadInfo(size, completeSize, mDownPath);
				return loadInfo;
			}
			return null;
		}
	}

	/**
	 * 初始化
	 */
	private Boolean init() {
		boolean result = false;

		HttpURLConnection conn = null;
		RandomAccessFile randomFile = null;
		try {
			URL url = new URL(mDownPath);
			conn = (HttpURLConnection) url.openConnection();
			constructHttps(conn);
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();
			// 如果http返回的代码是302则获取跳转地址重新发起请求
			if (responseCode == 302) {
				String location = conn.getHeaderField("Location");
				url = new URL(location);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				responseCode = conn.getResponseCode();
			}

			// 如果http返回的代码是200或者206则为连接成功
			if (responseCode == 200 || responseCode == 206) {
				result = true;
				mFileSize = conn.getContentLength();// 得到文件的大小
				if (mFileSize <= 0) {
					System.out.println("网络故障,无法获取文件大小");
					return false;
				}
				File dir = new File(mSavePath);
				// 如果文件目录不存在,则创建
				if (!dir.exists()) {
					if (dir.mkdirs()) {
						System.out.println("mkdirs success.");
					}
				}
				File file = new File(this.mSavePath, this.mFileName);
				randomFile = new RandomAccessFile(file, "rwd");
				randomFile.setLength(mFileSize);// 设置保存文件的大小
				randomFile.close();
				conn.disconnect();
			} else {

				Message message = Message.obtain();
				message.what = 2;
				message.arg1 = responseCode;
				mHandler.sendMessage(message);
			}
		} catch (Exception e) {

			Message message = Message.obtain();
			message.what = 3;
			message.obj = e;
			mHandler.sendMessage(message);

			e.printStackTrace();
			System.out.println("----------------首次下载初始化失败----------------");
		} finally {
			try {
				if (randomFile != null) {
					randomFile.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception e2) {
			}
		}
		return result;
	}

	/**
	 * 利用线程开始下载数据
	 */
	public void download() {
		if (infos != null) {
			if (state == DOWNLOADING) {
				return;
			}
			state = DOWNLOADING;// 把状态设置为正在下载
			for (ThreadInfo info : infos) {
				new MyThread(info.getThreadId(), info.getStartPos(), info.getEndPos(), info.getCompleteSize(), info.getUrl()).start();
			}
		}
	}

	// 设置暂停
	public void pause() {
		state = PAUSE;
	}

	// 重置下载状态,将下载状态设置为init初始化状态
	public void reset() {
		state = INIT;
	}

	/**
	 * 判断是否是第一次下载，利用litepal查询数据库中是否有下载这个地址的记录
	 */
	private boolean isFirst(String url) {
		return DataSupport.where("mUrl = ?", url).count(ThreadInfo.class) == 0;
	}


	private void constructHttps(HttpURLConnection conn) {

		if (conn instanceof HttpsURLConnection) {
			SSLContext context = null;
			try {
				context = SSLContext.getInstance("TLS");
				context.init(null, new TrustManager[]{new TrustAllManager()}, null);
				HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public class MyThread extends Thread {
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


//						FileStatus status = new FileStatus();
//						int sum = DataSupport.where("mUrl = ?", urlstr).sum(ThreadInfo.class, "mCompleteSize", int.class);
//						status.setCompleteSize(sum);
//						status.updateAll("mUrl = ? ", mDownPath);

						// 用消息将下载信息传给进度条，对进度条进行更新
						Message message = Message.obtain();
						message.what = 1;
						message.obj = urlstr;
						message.arg1 = length;
						mHandler.sendMessage(message);// 给DownloadService发送消息
						if (state == PAUSE) {
							System.out.println("-----pause-----");
							return;
						}
					}
					System.out.println("------------线程:" + this.threadId + "下载完成");
				}
			} catch (Exception e) {
				System.out.println("-----下载异常-----");
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
					e.printStackTrace();
				}
			}

		}


		/**
		 * 构建请求连接时的参数 返回开始下载的位置
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


	private class TrustAllManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	}


}
