package com.hopebaytech.hcfsmgmt.customview;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MultiLineSwitchPreference extends SwitchPreference {

	public MultiLineSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public MultiLineSwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MultiLineSwitchPreference(Context context) {
		super(context);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		TextView title = (TextView) view.findViewById(android.R.id.title);
		title.setSingleLine(false);
	}

}
 