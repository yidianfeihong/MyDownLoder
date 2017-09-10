package com.example.shiwenming_sx.mydownloader.activity;

import android.Manifest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.IBinder;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.example.shiwenming_sx.mydownloader.R;
import com.example.shiwenming_sx.mydownloader.adapter.DownloadAdapter;
import com.example.shiwenming_sx.mydownloader.core.DownloadService;
import com.example.shiwenming_sx.mydownloader.entity.FileStatus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener {

	private Button mBtnDownload;
	private Button mTest1;
	private Button mTest2;
	private Button mTest3;
	private DownloadService mService;
	private EditText mEtUrl;
	private String mName;
	private String mUrl;
	private RecyclerView mRecyclerView;
	private DownloadAdapter mAdapter;


	private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE};


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onUpdateEvent(FileStatus fileStatus) {
		if (fileStatus.getCompleteSize() == fileStatus.getFileSize()) {
			mAdapter.notifyDataSetChanged();
			return;
		}

		for (int i = 0; i < mAdapter.mFileStatusList.size(); i++) {

			if (mAdapter.mFileStatusList.get(i).getUrl().equals(fileStatus.getUrl())) {
				mAdapter.mFileStatusList.set(i, fileStatus);
				mAdapter.notifyItemChanged(i);
				return;
			}
		}

	}

//
//	@Subscribe(threadMode = ThreadMode.BACKGROUND)
//	public void onDatabaseEvent(FileStatus fileStatus) {
//
//
//	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDeleteEvent(String url) {

		mAdapter.notifyDataSetChanged();

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();

		if (Build.VERSION.SDK_INT >= 23) {
			verifyStoragePermissions(this);
		}


		EventBus.getDefault().register(this);

		Intent intent = new Intent(this, DownloadService.class);
		startService(intent);
		bindService(intent, this, Context.BIND_AUTO_CREATE);

	}


	public static void verifyStoragePermissions(Activity activity) {

		try {

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

		mTest1 = (Button) findViewById(R.id.btn_test1);
		mTest1.setOnClickListener(this);
		mTest2 = (Button) findViewById(R.id.btn_test2);
		mTest2.setOnClickListener(this);
		mTest3 = (Button) findViewById(R.id.btn_test3);
		mTest3.setOnClickListener(this);

		mEtUrl = (EditText) findViewById(R.id.et_input);


		mEtUrl.setText("http://gdown.baidu.com/data/wisegame/76c57604973e1ff3/zhihu_529.apk");
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
			((DefaultItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {

	}


	@Override
	public void onClick(View view) {

		switch (view.getId()) {
			case R.id.btn_download:
				mUrl = mEtUrl.getText().toString().trim();
				mName = getFileName(mUrl);

				mService.startDownload(mName, mUrl);
				break;

			case R.id.btn_test1:

				mEtUrl.setText("http://gdown.baidu.com/data/wisegame/e54e3c5bc84070c2/weixin_1100.apk");
				break;
			case R.id.btn_test2:

				mEtUrl.setText("http://gdown.baidu.com/data/wisegame/74fc094b8c60d732/QQliulanqi_7803540.apk");
				break;
			case R.id.btn_test3:

				mEtUrl.setText("http://gdown.baidu.com/data/wisegame/67c8bec51d1abe78/aiqiyi_80930.apk");
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
		EventBus.getDefault().unregister(this);
		unbindService(this);
	}

}
