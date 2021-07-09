/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.info;

import java.util.ArrayList;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.LocationStatus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

public class DataTypeInfo extends ItemInfo {

	private final String CLASSNAME = getClass().getSimpleName();
	private String dataType;
	private int icon_drawable_res_id;
	private Context context;
	private long date_updated;
	private long date_pinned;

	public DataTypeInfo(Context context) {
		super(context);
		this.context = context;
	}

	public Bitmap getIconImage() {
		return ((BitmapDrawable) ContextCompat.getDrawable(context, icon_drawable_res_id)).getBitmap();
	}

	@Nullable
	@Override
	public Drawable getIconDrawable() {
		return null;
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
		int status = LocationStatus.LOCAL;
//		if (dataType.equals(DataTypeDAO.DATA_TYPE_IMAGE)) {
//			ArrayList<String> imagePaths = HCFSMgmtUtils.getAvailableImagePaths(mContext);
//			status = getStatus(imagePaths);
//		} else if (dataType.equals(DataTypeDAO.DATA_TYPE_VIDEO)) {
//			ArrayList<String> videoPaths = HCFSMgmtUtils.getAvailableVideoPaths(mContext);
//			status = getStatus(videoPaths);
//		} else if (dataType.equals(DataTypeDAO.DATA_TYPE_AUDIO)) {
//			ArrayList<String> audioPaths = HCFSMgmtUtils.getAvailableAudioPaths(mContext);
//			status = getStatus(audioPaths);
//		} else {
//			status = -1;
//		}
		return status;
	}

//	private int getStatus(ArrayList<String> pathList) {
//		int status = LocationStatus.LOCAL;
//		int num_local = 0;
//		int num_hybrid = 0;
//		int num_cloud = 0;
//		for (String path : pathList) {
//			if (!Thread.currentThread().isInterrupted()) {
//				if (HCFSMgmtUtils.getFileLocationStatus(path) == LocationStatus.HYBRID) {
//					status = LocationStatus.HYBRID;
//					num_hybrid++;
//					return status;
//				} else if (HCFSMgmtUtils.getFileLocationStatus(path) == LocationStatus.LOCAL) {
//					num_local++;
//				} else if (HCFSMgmtUtils.getFileLocationStatus(path) == LocationStatus.HYBRID) {
//					num_cloud++;
//				}
//			}
//		}
//
//		if (num_local == 0 && num_cloud == 0 && num_hybrid == 0) {
//			status = LocationStatus.LOCAL;
//		} else if (num_local != 0 && num_cloud == 0 && num_hybrid == 0) {
//			status = LocationStatus.LOCAL;
//		} else if (num_local == 0 && num_cloud != 0 && num_hybrid == 0) {
//			status = LocationStatus.CLOUD;
//		} else {
//			status = LocationStatus.HYBRID;
//		}
//		return status;
//	}

	public long getDateUpdated() {
		return date_updated;
	}

	public long getDatePinned() {
		return date_pinned;
	}

	public void setDatePinned(long date_pinned) {
		this.date_pinned = date_pinned;
	}

	public void setDate_updated(long date_updated) {
		this.date_updated = date_updated;
	}

	public void setDateUpdated(long date_updated) {
		this.date_updated = date_updated;
	}

	@Override
	public int hashCode() {
		return getDataType().hashCode();
	}

	@Override
	public int getIconAlpha() {
		return 0;
	}

}
