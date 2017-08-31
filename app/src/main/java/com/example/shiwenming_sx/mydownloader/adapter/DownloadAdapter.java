package com.example.shiwenming_sx.mydownloader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shiwenming_sx.mydownloader.R;
import com.example.shiwenming_sx.mydownloader.entity.FileStatus;
import com.example.shiwenming_sx.mydownloader.utils.Downloader;

import java.util.List;


/**
 * Created by shiwenming_sx on 2017/8/29.
 */

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {


	public List<FileStatus> mFileStatuses;


	public DownloadAdapter(List<FileStatus> fileStatuses) {
		mFileStatuses = fileStatuses;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {

		FileStatus fileStatus = mFileStatuses.get(position);

		if (fileStatus != null) {
			holder.fileIcon.setImageResource(R.mipmap.ic_launcher);
			holder.fileName.setText(fileStatus.getFileName());
			holder.downProgress.setText(fileStatus.getCompleteSize() / 1024 / 1024 + "M/" + fileStatus.getFileSize() / 1014 / 1024 + "M");
			holder.progressBar.setMax(fileStatus.getFileSize());
			holder.progressBar.setProgress(fileStatus.getCompleteSize());
		}

	}

	@Override
	public int getItemCount() {
		return mFileStatuses.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		ImageView fileIcon;
		TextView downProgress;
		TextView fileName;
		ProgressBar progressBar;

		private ViewHolder(View itemView) {
			super(itemView);

			fileIcon = itemView.findViewById(R.id.iv_file_icon);
			downProgress = itemView.findViewById(R.id.tv_progress);
			fileName = itemView.findViewById(R.id.tv_filename);
			progressBar = itemView.findViewById(R.id.progress_bar);

		}
	}


}
