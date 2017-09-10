package com.example.shiwenming_sx.mydownloader.core;

import com.example.shiwenming_sx.mydownloader.utils.Constants;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 * Created by shiwenming_sx on 2017/9/10.
 */

public class InitThread implements Runnable {

	private final String downPath;
	private final InitListener listener;
	private String savePath;
	private String fileName;

	public InitThread(String url, String fileName, InitListener listener) {
		this.downPath = url;
		this.listener = listener;
		this.savePath = Constants.mSavePath;
		this.fileName = fileName;
	}

	@Override
	public void run() {

		HttpURLConnection conn = null;
		RandomAccessFile randomFile = null;
		try {
			URL url = new URL(downPath);
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
				int fileSize = conn.getContentLength();// 得到文件的大小

				if (fileSize <= 0) {
					System.out.println("网络故障,无法获取文件大小");
				} else {
					listener.onConnected(fileSize);
				}
				File dir = new File(savePath);
				// 如果文件目录不存在,则创建
				if (!dir.exists()) {
					if (dir.mkdirs()) {
						System.out.println("mkdirs success.");
					}
				}
				File file = new File(this.savePath, this.fileName);
				randomFile = new RandomAccessFile(file, "rwd");
				randomFile.setLength(fileSize);// 设置保存文件的大小
				randomFile.close();
				conn.disconnect();
			} else {
				listener.onWrongResponse(responseCode);
			}
		} catch (Exception e) {
			listener.onError(e.toString());
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
				listener.onError(e2.toString());
			}
		}

	}


	interface InitListener {

		void onConnected(int totalLength);

		void onWrongResponse(int responseCode);

		void onError(String message);
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

}
