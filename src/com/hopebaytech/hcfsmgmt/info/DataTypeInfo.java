package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

public class DataTypeInfo extends ItemInfo {

	private String dataType;
	private int icon_drawable_res_id;
	private Context context;
	
	public DataTypeInfo(Context context) {
		super(context);
		this.context = context;
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

}