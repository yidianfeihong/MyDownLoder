package com.example.shiwenming_sx.mydownloader.activity;

import android.Manifest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shiwenming_sx.mydownloader.R;
import com.example.shiwenming_sx.mydownloader.adapter.DownloadAdapter;
import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.service.DownloadService;
import com.example.shiwenming_sx.mydownloader.utils.Downloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener {

	private Button mBtnDownload;
	private DownloadService mService;
	private EditText mEtUrl;
	private String mName;
	private String mUrl;
	private RecyclerView mRecyclerView;
	private DownloadAdapter mAdapter;


	private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
			} else if (msg.what == 2) {
//				Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		verifyStoragePermissions(this);

		Intent intent = new Intent(this, DownloadService.class);
		startService(intent);
		bindService(intent, this, Context.BIND_AUTO_CREATE);


	}


	public static void verifyStoragePermissions(Activity activity) {

		try {
			//检测是否有写的权限
			int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (permission != PackageManager.PERMISSION_GRANTED) {
				// 没有写的权限，去申请写的权限，会弹出对话框
				ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView() {

		mBtnDownload = (Button) findViewById(R.id.btn_download);
		mBtnDownload.setOnClickListener(this);

		mEtUrl = (EditText) findViewById(R.id.et_input);
//		mEtUrl.setText("http://dlsw.baidu.com/sw-search-sp/soft/40/12856/QIYImedia_1_06.1400202272.exe");
		mEtUrl.setText("http://182.254.212.207:8080/download/newsreader.apk");
		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		switch (requestCode) {
			case 1:
				if (grantResults.length < 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, "权限获取失败，应用无法启动", Toast.LENGTH_SHORT).show();
					finish();
				}
		}
	}

	@Override
	public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
		mService = ((DownloadService.MyBinder) iBinder).getService();
		if (mService != null) {

			mAdapter = new DownloadAdapter(MainActivity.this, mService, DownloadService.mFileStatusList);
			mRecyclerView.setAdapter(mAdapter);

			mService.setLoadCallback(new DownloadService.DownLoadCallback() {
				@Override
				public void refreshUI(List<FileStatus> fileStatuses) {
					mAdapter.mFileStatuses = fileStatuses;
					mAdapter.notifyDataSetChanged();
				}

				@Override
				public void deleteFile(String url) {
					mAdapter.notifyDataSetChanged();
				}

			});
		}


	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {

	}


	@Override
	public void onClick(View view) {

		switch (view.getId()) {
			case R.id.btn_download:
				Toast.makeText(mService, "开始下载", Toast.LENGTH_SHORT).show();
				mUrl = mEtUrl.getText().toString().trim();
				mName = getFileName(mUrl);
				mService.startDownload(mName, mUrl, mHandler);
				break;
		}

	}

	private String getFileName(String url) {
		int start = url.lastIndexOf("/") + 1;
		return url.substring(start);
	}


	@Override
	protected void onResume() {
		super.onResume();

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(this);
	}

}