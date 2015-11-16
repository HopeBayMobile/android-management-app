package com.hopebaytech.hcfsmgmt.info;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

public class DataTypeInfo extends ItemInfo {

	private String dataType;
	private ArrayList<String> filePathList;
	private int icon_drawable_res_id;
	
	public DataTypeInfo(Context context) {
		super(context);
	}
	
	public Drawable getIconImage() {
		return ContextCompat.getDrawable(context, icon_drawable_res_id);
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

	public ArrayList<String> getFilePathList() {
		return filePathList;
	}

	public void setFilePathList(ArrayList<String> filePathList) {
		this.filePathList = filePathList;
	}

}