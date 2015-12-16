package com.hopebaytech.hcfsmgmt.fragment;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.main.AddMountPointActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HomepageFragment extends Fragment {

	public static String TAG = HomepageFragment.class.getSimpleName();
	private NetworkBroadcastReceiver networkStatusRecevier;
	private HCFSStatInfo statInfo;

	public static HomepageFragment newInstance() {
		HomepageFragment fragment = new HomepageFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		networkStatusRecevier = new NetworkBroadcastReceiver();
		statInfo = HCFSMgmtUtils.getHCFSStatInfo();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.home_page_fragment, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setTitle(getString(R.string.app_name));

		View view = getView();

		// StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
		// Log.w(HCFSMgmtUtils.TAG, "Environment.getDataDirectory().getPath(): " + Environment.getDataDirectory().getPath());
		// Log.w(HCFSMgmtUtils.TAG, "statFs.getTotalBytes(): " + statFs.getTotalBytes());
		// Log.w(HCFSMgmtUtils.TAG, "statFs.getFreeBytes(): " + statFs.getFreeBytes());

		TextView system = (TextView) view.findViewById(R.id.system);
		system.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				boolean isSDCard1 = false;
				ft.replace(R.id.fragment_container, FileManagementFragment.newInstance(isSDCard1), FileManagementFragment.TAG);
				ft.commit();
			}
		});

		if (statInfo != null) {
			LinearLayout cloudStorage = (LinearLayout) view.findViewById(R.id.cloud_storage);
			TextView cloudStorageTitle = (TextView) cloudStorage.findViewById(R.id.textViewTitle);
			TextView cloudStorageUsage = (TextView) cloudStorage.findViewById(R.id.textViewUsage);
//			ImageView cloudStorageImageview = (ImageView) cloudStorage.findViewById(R.id.iconView);
			ProgressBar cloudStorageProgressBar = (ProgressBar) cloudStorage.findViewById(R.id.progressBar);
			cloudStorageTitle.setText(getString(R.string.home_page_cloud_storage));
			cloudStorageUsage.setText(statInfo.getCloudUsed() + " / " + statInfo.getCloudTotal());
			cloudStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
			cloudStorageProgressBar.setProgress(statInfo.getCloudUsedPercentage());

			LinearLayout localStorage = (LinearLayout) view.findViewById(R.id.local_storage);
			TextView localStorageTitle = (TextView) localStorage.findViewById(R.id.textViewTitle);
			TextView localStorageUsage = (TextView) localStorage.findViewById(R.id.textViewUsage);
			ProgressBar localStorageProgressBar = (ProgressBar) localStorage.findViewById(R.id.progressBar);
			localStorageTitle.setText(getString(R.string.home_page_local_storage));
			localStorageUsage.setText(statInfo.getCacheUsed() + " / " + statInfo.getCloudTotal());
			localStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
			localStorageProgressBar.setProgress(statInfo.getCacheUsedPercentage());

			LinearLayout pinnedStorage = (LinearLayout) view.findViewById(R.id.pinned_storage);
			TextView pinnedStorageTitle = (TextView) pinnedStorage.findViewById(R.id.textViewTitle);
			TextView pinnedStorageUsage = (TextView) pinnedStorage.findViewById(R.id.textViewUsage);
			ProgressBar pinnedStorageProgressBar = (ProgressBar) pinnedStorage.findViewById(R.id.progressBar);
			pinnedStorageTitle.setText(getString(R.string.home_page_pinned_storage));
			pinnedStorageUsage.setText(statInfo.getPinTotal() + " / " + statInfo.getPinMax());
			pinnedStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
			pinnedStorageProgressBar.setProgress(statInfo.getPinnedUsedPercentage());

			LinearLayout toBeUploadData = (LinearLayout) view.findViewById(R.id.to_be_upload_data);
			TextView toBeUploadDataTitle = (TextView) toBeUploadData.findViewById(R.id.textViewTitle);
			TextView toBeUploadDataUsage = (TextView) toBeUploadData.findViewById(R.id.textViewUsage);
			ProgressBar toBeUploadDataUsageProgressBar = (ProgressBar) toBeUploadData.findViewById(R.id.progressBar);
			toBeUploadDataTitle.setText(getString(R.string.home_page_to_be_upladed_data));
			toBeUploadDataUsage.setText(statInfo.getCacheDirtyUsed() + " / " + statInfo.getCloudTotal());
			toBeUploadDataUsageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
			toBeUploadDataUsageProgressBar.setProgress(statInfo.getDirtyPercentage());

			LinearLayout network_xfer_today = (LinearLayout) view.findViewById(R.id.network_xfer_today);
			TextView network_xfer_today_title = (TextView) network_xfer_today.findViewById(R.id.textViewTitle);
			TextView network_xfer_up = (TextView) network_xfer_today.findViewById(R.id.xfer_up);
			TextView network_xfer_down = (TextView) network_xfer_today.findViewById(R.id.xfer_down);
			ProgressBar xferProgressBar = (ProgressBar) network_xfer_today.findViewById(R.id.progressBar);
			String xferDownload = statInfo.getXferDownload();
			String xferUpload = statInfo.getXferUpload();
			network_xfer_today_title.setText(getString(R.string.home_page_the_amount_of_network_traffic_today));
			network_xfer_up.setText(xferUpload);
			network_xfer_down.setText(xferDownload);
			xferProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.xfer_progressbar));
			xferProgressBar.setProgress(statInfo.getXterDownloadPercentage());
			xferProgressBar.setSecondaryProgress(100);
		}

		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.add_new_mount_point);
		// ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
		TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		int baseBottom = getResources().getDimensionPixelSize(typedValue.resourceId);
		// int bottomMargin = (int) (baseBottom + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));
		// int rightMargin = (int) getResources().getDimension(R.dimen.fab_right_margin);
		// params.setMargins(0, 0, rightMargin, bottomMargin);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AddMountPointActivity.class);
				startActivity(intent);
			}
		});
		fab.setTranslationY(baseBottom + getResources().getDimension(R.dimen.fab_bottom_margin));
		fab.animate().translationY(0).setDuration(100).setStartDelay(400);

	}

	public class NetworkBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
				TextView networkConnStatus = (TextView) getActivity().findViewById(R.id.network_conn_status);
				if (netInfo != null && netInfo.isConnected()) {
					networkConnStatus.setText(getString(R.string.home_page_network_status_connected));
				} else {
					networkConnStatus.setText(getString(R.string.home_page_network_status_disconnected));
				}
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(networkStatusRecevier, filter);
	}

	@Override
	public void onPause() {
		getActivity().unregisterReceiver(networkStatusRecevier);
		super.onPause();
	}

}
