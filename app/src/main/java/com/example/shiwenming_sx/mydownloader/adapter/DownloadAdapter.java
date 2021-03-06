package com.example.shiwenming_sx.mydownloader.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shiwenming_sx.mydownloader.R;
import com.example.shiwenming_sx.mydownloader.core.DownloadManager;
import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.core.DownloadService;
import com.example.shiwenming_sx.mydownloader.utils.Constants;

import java.util.List;


/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {


	public List<FileStatus> mFileStatusList;
	private DownloadService mService;
	private Context mContext;


	public DownloadAdapter(Context context, DownloadService service,List<FileStatus> fileStatusList) {
		mContext = context;
		mService = service;
		mFileStatusList = fileStatusList;

	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {

		final FileStatus fileStatus = mFileStatusList.get(position);

		if (fileStatus.getStatus()== Constants.COMPLETED) {
			holder.statusChange.setText("已完成");
			holder.downProgress.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.GONE);
		} else {

			if (fileStatus.getStatus()==Constants.DOWNLOADING) {
				holder.statusChange.setText("暂停");
			} else {
				holder.statusChange.setText("开始");
			}

			holder.downProgress.setVisibility(View.VISIBLE);
			holder.progressBar.setVisibility(View.VISIBLE);
		}

		holder.fileIcon.setImageResource(R.drawable.icon);
		holder.fileName.setText(fileStatus.getFileName());
		holder.downProgress.setText(fileStatus.getCompleteSize() / 1024 / 1024 + "M/" + fileStatus.getFileSize() / 1014 / 1024 + "M");
		holder.progressBar.setMax(fileStatus.getFileSize());
		holder.progressBar.setProgress(fileStatus.getCompleteSize());

		holder.statusChange.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (fileStatus.getCompleteSize() == fileStatus.getFileSize()) {
					return;
				}
				if (fileStatus.getStatus()==1) {
					mService.Pause(fileStatus);
				} else {
					mService.continueDownload(fileStatus.getFileName(), fileStatus.getUrl());
				}
			}
		});
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("温馨提示").setMessage("确定要删除文件吗").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mService.delete(fileStatus.getFileName(), fileStatus.getUrl());
					}
				}).setNegativeButton("取消", null).create().show();

			}
		});
		holder.restart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mService.reDownload(fileStatus);
			}
		});


	}

	@Override
	public int getItemCount() {
		return mFileStatusList.size();
	}


	static class ViewHolder extends RecyclerView.ViewHolder {

		ImageView fileIcon;
		TextView downProgress;
		TextView fileName;
		ProgressBar progressBar;

		Button statusChange;
		Button delete;
		Button restart;

		private ViewHolder(View itemView) {
			super(itemView);

			fileIcon = itemView.findViewById(R.id.iv_file_icon);
			downProgress = itemView.findViewById(R.id.tv_progress);
			fileName = itemView.findViewById(R.id.tv_filename);
			progressBar = itemView.findViewById(R.id.progress_bar);

			statusChange = itemView.findViewById(R.id.btn_change_state);
			delete = itemView.findViewById(R.id.btn_delete);
			restart = itemView.findViewById(R.id.btn_restart);


		}
	}


}
