package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

public class AboutFragment extends Fragment {

	public static final String TAG = AboutFragment.class.getSimpleName();
	private final String CLASSNAME = AboutFragment.class.getSimpleName();
	private Context mContext;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.about_fragment, container, false);
	}
	
	public static AboutFragment newInstance() {
		return new AboutFragment();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TextView imeiOne = (TextView) view.findViewById(R.id.device_imei_1);
		TextView imeiTwo = (TextView) view.findViewById(R.id.device_imei_2);
		if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
			if (manager.getPhoneCount() == 0) {
				imeiOne.setText("-");
				imeiTwo.setText("-");
			} else if (manager.getPhoneCount() == 1) {
				imeiOne.setText(manager.getDeviceId(0));
			} else {
				imeiOne.setText(manager.getDeviceId(0));
				imeiTwo.setText(manager.getDeviceId(1));
			}
		}

		TextView terafonnVersion = (TextView) view.findViewById(R.id.terafonn_version);
		terafonnVersion.setText(getString(R.string.terafonn_version));
	}

	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
	}

}
