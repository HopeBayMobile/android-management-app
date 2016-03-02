package com.hopebaytech.hcfsmgmt.fragment;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

	public static final String TAG = AboutFragment.class.getSimpleName();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.about_fragment, container, false);
		return view;
	}
	
	public static AboutFragment newInstance() {
		AboutFragment aboutFragment = new AboutFragment();
		return aboutFragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		View view = getView();
		TextView system_app = (TextView) view.findViewById(R.id.version_app);
		try {
			String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			system_app.setText(versionName);
		} catch (NameNotFoundException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
	}

	public void onBackPressed() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_container, HomepageFragment.newInstance(), HomepageFragment.TAG);
		ft.commit();
	}

}
