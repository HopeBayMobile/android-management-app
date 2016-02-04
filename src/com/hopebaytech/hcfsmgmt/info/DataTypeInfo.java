package com.hopebaytech.hcfsmgmt.info;

import java.util.ArrayList;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class DataTypeInfo extends ItemInfo {

	private final String CLASS_NAME = getClass().getSimpleName();
	private String dataType;
	private int icon_drawable_res_id;
	private Context context;

	public DataTypeInfo(Context context) {
		super(context);
		this.context = context;
	}

	public Bitmap getIconImage() {
		return ((BitmapDrawable) ContextCompat.getDrawable(context, icon_drawable_res_id)).getBitmap();
	}

	public void setIconImage(int icon_drawable_res_id) {
		this.icon_drawable_res_id = icon_drawable_res_id;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getDataTypeStatus() {
		HCFSMgmtUtils.log(Log.DEBUG, CLASS_NAME, "getDataTypeStatus", null);
		int status;
		if (dataType.equals(HCFSMgmtUtils.DATA_TYPE_IMAGE)) {
			ArrayList<String> imagePaths = HCFSMgmtUtils.getAvailableImagePaths(context);
			status = getStatus(imagePaths);
		} else if (dataType.equals(HCFSMgmtUtils.DATA_TYPE_VIDEO)) {
			ArrayList<String> videoPaths = HCFSMgmtUtils.getAvailableVideoPaths(context);
			status = getStatus(videoPaths);
		} else if (dataType.equals(HCFSMgmtUtils.DATA_TYPE_AUDIO)) {
			ArrayList<String> audioPaths = HCFSMgmtUtils.getAvailableAudioPaths(context);
			status = getStatus(audioPaths);
		} else {
			status = -1;
		}
		return status;
	}

	@Override
	public int getLocationStatus() {
		return getDataTypeStatus();
	}

	private int getStatus(ArrayList<String> pathList) {
		int status = FileStatus.LOCAL;
		int num_local = 0;
		int num_hybrid = 0;
		int num_cloud = 0;
		for (String path : pathList) {
			if (!Thread.currentThread().isInterrupted()) {
				if (HCFSMgmtUtils.getFileStatus(path) == FileStatus.HYBRID) {
					status = FileStatus.HYBRID;
					num_hybrid++;
					return status;
				} else if (HCFSMgmtUtils.getFileStatus(path) == FileStatus.LOCAL) {
					num_local++;
				} else if (HCFSMgmtUtils.getFileStatus(path) == FileStatus.HYBRID) {
					num_cloud++;
				}
			}
		}

		if (num_local == 0 && num_cloud == 0 && num_hybrid == 0) {
			status = FileStatus.LOCAL;
		} else if (num_local != 0 && num_cloud == 0 && num_hybrid == 0) {
			status = FileStatus.LOCAL;
		} else if (num_local == 0 && num_cloud != 0 && num_hybrid == 0) {
			status = FileStatus.CLOUD;
		} else {
			status = FileStatus.HYBRID;
		}
		return status;
	}

}
