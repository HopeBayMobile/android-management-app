package com.hopebaytech.hcfsmgmt.fragment;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

public class AboutFragment extends Fragment {

	public static final String TAG = AboutFragment.class.getSimpleName();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.about_fragment, container, false);
	}
	
	public static AboutFragment newInstance() {
		return new AboutFragment();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		View view = getView();
		if (view != null) {
			TextView system_app = (TextView) view.findViewById(R.id.version_app);
			try {
				String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
				system_app.setText(versionName);
			} catch (NameNotFoundException e) {
				Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
			}
		}
	}

	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
//		if (menuVisible) {
//			Log.w(HCFSMgmtUtils.TAG, "visible");
//		} else {
//			Log.w(HCFSMgmtUtils.TAG, "invisible");
//		}
	}

}
