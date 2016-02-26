package com.hopebaytech.hcfsmgmt.fragment;

import java.util.List;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HomepageFragment extends Fragment {

	public static String TAG = HomepageFragment.class.getSimpleName();
	private final String CLASSNAME = getClass().getSimpleName();
	private NetworkBroadcastReceiver networkStatusRecevier;
	// private HCFSStatInfo statInfo;
	private Thread uiRefreshThread;

	public static HomepageFragment newInstance() {
		HomepageFragment fragment = new HomepageFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		networkStatusRecevier = new NetworkBroadcastReceiver();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.home_page_fragment, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		View view = getView();

		// TextView system = (TextView) view.findViewById(R.id.system);
		// system.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// FragmentManager fm = getFragmentManager();
		// FragmentTransaction ft = fm.beginTransaction();
		// boolean isSDCard1 = false;
		// ft.replace(R.id.fragment_container, FileManagementFragment.newInstance(isSDCard1), FileManagementFragment.TAG);
		// ft.commit();
		// }
		// });
		HorizontalScrollView horizontalScrollViewView = (HorizontalScrollView) view.findViewById(R.id.mount_point_scrollview);
		horizontalScrollViewView.setVisibility(View.GONE);

		// if (statInfo != null) {
		LinearLayout cloudStorage = (LinearLayout) view.findViewById(R.id.cloud_storage);
		final TextView cloudStorageTitle = (TextView) cloudStorage.findViewById(R.id.textViewTitle);
		final TextView cloudStorageUsage = (TextView) cloudStorage.findViewById(R.id.textViewUsage);
		final ImageView cloudStorageImageview = (ImageView) cloudStorage.findViewById(R.id.iconView);
		final ProgressBar cloudStorageProgressBar = (ProgressBar) cloudStorage.findViewById(R.id.progressBar);
		cloudStorageTitle.setText(getString(R.string.home_page_used_space));
		// cloudStorageUsage.setText(statInfo.getVolUsed() + " / " + statInfo.getCloudTotal());
		cloudStorageImageview.setImageResource(R.drawable.cloudspace_128x128);
		// cloudStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
		// cloudStorageProgressBar.setProgress(statInfo.getCloudUsedPercentage());

		LinearLayout localStorage = (LinearLayout) view.findViewById(R.id.local_storage);
		localStorage.setVisibility(View.GONE);
		// TextView localStorageTitle = (TextView) localStorage.findViewById(R.id.textViewTitle);
		// TextView localStorageUsage = (TextView) localStorage.findViewById(R.id.textViewUsage);
		// ImageView localStorageImageview = (ImageView) localStorage.findViewById(R.id.iconView);
		// ProgressBar localStorageProgressBar = (ProgressBar) localStorage.findViewById(R.id.progressBar);
		// localStorageTitle.setText(getString(R.string.home_page_local_storage));
		// localStorageUsage.setText(statInfo.getCacheUsed() + " / " + statInfo.getCacheTotal());
		// localStorageImageview.setImageResource(R.drawable.localspace_128x128);
		// localStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
		// localStorageProgressBar.setProgress(statInfo.getCacheUsedPercentage());

		LinearLayout pinnedStorage = (LinearLayout) view.findViewById(R.id.pinned_storage);
		final TextView pinnedStorageTitle = (TextView) pinnedStorage.findViewById(R.id.textViewTitle);
		final TextView pinnedStorageUsage = (TextView) pinnedStorage.findViewById(R.id.textViewUsage);
		final ImageView pinnedStorageImageview = (ImageView) pinnedStorage.findViewById(R.id.iconView);
		final ProgressBar pinnedStorageProgressBar = (ProgressBar) pinnedStorage.findViewById(R.id.progressBar);
		pinnedStorageTitle.setText(getString(R.string.home_page_pinned_storage));
		// pinnedStorageUsage.setText(statInfo.getPinTotal());
		pinnedStorageImageview.setImageResource(R.drawable.pinspace_128x128);
		pinnedStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
		// pinnedStorageProgressBar.setProgress(statInfo.getPinnedUsedPercentage());

		LinearLayout waitToUploadData = (LinearLayout) view.findViewById(R.id.to_be_upload_data);
		final TextView waitToUploadDataTitle = (TextView) waitToUploadData.findViewById(R.id.textViewTitle);
		final TextView waitToUploadDataUsage = (TextView) waitToUploadData.findViewById(R.id.textViewUsage);
		final ImageView waitToUploadDataUsageImageview = (ImageView) waitToUploadData.findViewById(R.id.iconView);
		final ProgressBar waitToUploadDataUsageProgressBar = (ProgressBar) waitToUploadData.findViewById(R.id.progressBar);
		waitToUploadDataTitle.setText(getString(R.string.home_page_to_be_upladed_data));
		// waitToUploadDataUsage.setText(statInfo.getCacheDirtyUsed());
		waitToUploadDataUsageImageview.setImageResource(R.drawable.uploading_128x128);
		waitToUploadDataUsageProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.storage_progressbar));
		// waitToUploadDataUsageProgressBar.setProgress(statInfo.getDirtyPercentage());

		LinearLayout network_xfer_today = (LinearLayout) view.findViewById(R.id.network_xfer_today);
		final TextView network_xfer_today_title = (TextView) network_xfer_today.findViewById(R.id.textViewTitle);
		final TextView network_xfer_up = (TextView) network_xfer_today.findViewById(R.id.xfer_up);
		final TextView network_xfer_down = (TextView) network_xfer_today.findViewById(R.id.xfer_down);
		final ImageView networkXferImageview = (ImageView) network_xfer_today.findViewById(R.id.iconView);
		final ProgressBar xferProgressBar = (ProgressBar) network_xfer_today.findViewById(R.id.progressBar);
		// String xferDownload = statInfo.getXferDownload();
		// String xferUpload = statInfo.getXferUpload();
		network_xfer_today_title.setText(getString(R.string.home_page_the_amount_of_network_traffic_today));
		// network_xfer_up.setText(xferUpload);
		// network_xfer_down.setText(xferDownload);
		networkXferImageview.setImageResource(R.drawable.load_128x128);
		xferProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.xfer_progressbar));
		// if (xferDownload.equals("0B") && xferUpload.equals("0B")) {
		// xferProgressBar.setProgress(0);
		// xferProgressBar.setSecondaryProgress(0);
		// } else {
		// xferProgressBar.setProgress(statInfo.getXterDownloadPercentage());
		// xferProgressBar.setSecondaryProgress(100);
		// }
		// }

		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.add_new_mount_point);
		fab.setVisibility(View.GONE);
		// TypedValue typedValue = new TypedValue();
		// getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		// int baseBottom = getResources().getDimensionPixelSize(typedValue.resourceId);
		// fab.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// Intent intent = new Intent(getActivity(), AddMountPointActivity.class);
		// startActivity(intent);
		// }
		// });
		// fab.setTranslationY(baseBottom + getResources().getDimension(R.dimen.fab_bottom_margin));
		// fab.animate().translationY(0).setDuration(100).setStartDelay(400);

		uiRefreshThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						final HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
						Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (statInfo != null) {
										cloudStorageUsage.setText(statInfo.getVolUsed() + " / " + statInfo.getCloudTotal());
										cloudStorageProgressBar.setProgress(statInfo.getCloudUsedPercentage());

										pinnedStorageUsage.setText(statInfo.getPinTotal());
										pinnedStorageProgressBar.setProgress(statInfo.getPinnedUsedPercentage());

										waitToUploadDataUsage.setText(statInfo.getCacheDirtyUsed());
										waitToUploadDataUsageProgressBar.setProgress(statInfo.getDirtyPercentage());

										String xferDownload = statInfo.getXferDownload();
										String xferUpload = statInfo.getXferUpload();
										network_xfer_up.setText(xferUpload);
										network_xfer_down.setText(xferDownload);
										if (xferDownload.equals("0B") && xferUpload.equals("0B")) {
											xferProgressBar.setProgress(0);
											xferProgressBar.setSecondaryProgress(0);
										} else {
											xferProgressBar.setProgress(statInfo.getXterDownloadPercentage());
											xferProgressBar.setSecondaryProgress(100);
										}
									} else {
										cloudStorageUsage.setText("-");
										cloudStorageProgressBar.setProgress(0);

										pinnedStorageUsage.setText("-");
										pinnedStorageProgressBar.setProgress(0);

										waitToUploadDataUsage.setText("-");
										waitToUploadDataUsageProgressBar.setProgress(0);

										network_xfer_up.setText("-");
										network_xfer_down.setText("-");
										xferProgressBar.setProgress(0);
										xferProgressBar.setSecondaryProgress(0);
									}
								}
							});
						}
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
		uiRefreshThread.start();

	}

	public class NetworkBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
				ImageView networkConnStatusImage = (ImageView) getView().findViewById(R.id.network_conn_status_icon);
				TextView networkConnStatusText = (TextView) getView().findViewById(R.id.network_conn_status);
				if (netInfo != null) {
					if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
						HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is connected");
						networkConnStatusImage.setImageResource(R.drawable.connect_96x96);
						networkConnStatusText.setText(getString(R.string.home_page_network_status_connected));
					} else if (netInfo.getState() == NetworkInfo.State.CONNECTING) {
						HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is connecting");
						networkConnStatusImage.setImageResource(R.drawable.connect_connecting_96x96);
						networkConnStatusText.setText(getString(R.string.home_page_network_status_connecting));
					} else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
						HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is disconnected");
						networkConnStatusImage.setImageResource(R.drawable.connect_stop_96x96);
						networkConnStatusText.setText(getString(R.string.home_page_network_status_disconnected));
					}
				} else {
					networkConnStatusImage.setImageResource(R.drawable.connect_stop_96x96);
					networkConnStatusText.setText(getString(R.string.home_page_network_status_disconnected));
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiRefreshThread.interrupt();
	}

}
