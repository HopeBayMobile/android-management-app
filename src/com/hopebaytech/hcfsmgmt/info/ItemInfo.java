package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

public class ItemInfo {

	public static final int DATA_STATUS_CLOUD = 0;
	public static final int DATA_STATUS_HYBRID = 1;
	public static final int DATA_STATUS_LOCAL = 2;

	protected Context context;
	private boolean isPinned;
	private int dataStatus;
	private String infoName;
	
	public ItemInfo(Context context) {
		this.context = context;
	}

	public String getItemName() {
		return infoName;
	}

	public void setItemName(String infoName) {
		this.infoName = infoName;
	}

//	public void setIconImage(int iconImagePath) {
//		String mimeType = HCFSMgmtUtils.getMimeType(iconImagePath);
//		if (mimeType != null) {
//			fileDirInfo.setMimeType(mimeType);
//			Log.d(HCFSMgmtUtils.TAG, "mimeType: " + mimeType);
//			int width, height;
//			width = height = (int) getResources().getDimension(R.dimen.item_image_height_width);
//			if (mimeType.contains(MIME_TYPE_IMAGE)) {
//				Bitmap image = BitmapFactory.decodeFile(absoluteFilePath);
//				Bitmap thumbImage = ThumbnailUtils.extractThumbnail(image, width, height);
//				fileDirInfo.setIconImage(new BitmapDrawable(getResources(), thumbImage));
//			} else if (mimeType.contains(MIME_TYPE_VIDEO)) {
//				Bitmap image = ThumbnailUtils.createVideoThumbnail(absoluteFilePath, MediaStore.Video.Thumbnails.MICRO_KIND);
//				Bitmap thumbImage = ThumbnailUtils.extractThumbnail(image, width, height);
//				fileDirInfo.setIconImage(new BitmapDrawable(getResources(), thumbImage));
//			} else if (mimeType.contains(MIME_TYPE_APPLICATION)) {
//				String archiveFilePath = absoluteFilePath;
//				PackageManager pm = getActivity().getPackageManager();
//				PackageInfo packageInfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
//				ApplicationInfo appInfo = packageInfo.applicationInfo;
//				appInfo.sourceDir = archiveFilePath;
//				appInfo.publicSourceDir = archiveFilePath;
//				fileDirInfo.setIconImage(appInfo.loadIcon(pm));
//			} else if (mimeType.contains(MIME_TYPE_AUDIO)) {
//				fileDirInfo.setIconImage(getResources().getDrawable(R.drawable.ic_audio_white));
//			} else {
//				fileDirInfo.setIconImage(getResources().getDrawable(R.drawable.ic_file_black));
//			}
//		} else {
//			fileDirInfo.setIconImage(getResources().getDrawable(R.drawable.ic_file_black));
//		}
//		
//		
//		this.icon_drawable_res_id = icon_res_id;
//	}

	public Drawable getPinImage() {
		Drawable pinDrawable;
		if (isPinned) {
			pinDrawable = ContextCompat.getDrawable(context, R.drawable.pinned);
		} else {
			pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned);
		}
		return pinDrawable;
	}

	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}

	public int getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(int dataStatus) {
		this.dataStatus = dataStatus;
	}

}