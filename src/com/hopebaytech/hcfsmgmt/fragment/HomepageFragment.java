package com.hopebaytech.hcfsmgmt.fragment;

import com.hopebaytech.hcfsmgmt.AddMountPointActivity;
import com.hopebaytech.hcfsmgmt.R;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HomepageFragment extends Fragment {

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.home_page_fragment, container, false);

		LinearLayout cloud_storage = (LinearLayout) view.findViewById(R.id.cloud_storage);
		TextView cloud_storage_title = (TextView) cloud_storage.findViewById(R.id.textViewTitle);
		TextView cloud_storage_usage = (TextView) cloud_storage.findViewById(R.id.textViewUsage);
		ImageView cloud_storage_imageview = (ImageView) cloud_storage.findViewById(R.id.iconView);
		cloud_storage_title.setText(getString(R.string.home_page_cloud_storage));
		cloud_storage_usage.setText("10.4 / 11.7GB");

		LinearLayout local_storage = (LinearLayout) view.findViewById(R.id.local_storage);
		TextView local_storage_title = (TextView) local_storage.findViewById(R.id.textViewTitle);
		TextView local_storage_usage = (TextView) local_storage.findViewById(R.id.textViewUsage);
		local_storage_title.setText(getString(R.string.home_page_local_storage));
		local_storage_usage.setText("8.2 / 29.9GB");

		LinearLayout pinned_storage = (LinearLayout) view.findViewById(R.id.pinned_storage);
		TextView pinned_storage_title = (TextView) pinned_storage.findViewById(R.id.textViewTitle);
		TextView pinned_storage_usage = (TextView) pinned_storage.findViewById(R.id.textViewUsage);
		pinned_storage_title.setText(getString(R.string.home_page_pinned_storage));
		pinned_storage_usage.setText("10.4 / 11.7GB");

		LinearLayout to_be_upload_data = (LinearLayout) view.findViewById(R.id.to_be_upload_data);
		TextView to_be_upload_data_title = (TextView) to_be_upload_data.findViewById(R.id.textViewTitle);
		TextView to_be_upload_data_usage = (TextView) to_be_upload_data.findViewById(R.id.textViewUsage);
		to_be_upload_data_title.setText(getString(R.string.home_page_to_be_upladed_data));
		to_be_upload_data_usage.setText("10.4 / 11.7GB");

		LinearLayout the_amount_of_network_traffic_today = (LinearLayout) view.findViewById(R.id.the_amount_of_network_traffic_today);
		TextView the_amount_of_network_traffic_today_title = (TextView) the_amount_of_network_traffic_today.findViewById(R.id.textViewTitle);
		TextView the_amount_of_network_traffic_today_usage = (TextView) the_amount_of_network_traffic_today.findViewById(R.id.textViewUsage);
		ProgressBar progressBar = (ProgressBar) the_amount_of_network_traffic_today.findViewById(R.id.progressBar);
		int holo_blue_light = getResources().getColor(android.R.color.holo_blue_light);
		int holo_red_light = getResources().getColor(android.R.color.holo_red_light);
		String str_holo_blue_bright = "#" + Integer.toHexString(holo_blue_light).substring(2);
		String str_holo_red_light = "#" + Integer.toHexString(holo_red_light).substring(2);
		Spanned textContent = Html.fromHtml(
				"<font color=\"" + str_holo_blue_bright + "\">下載 125MB</font> / <font color=\"" + str_holo_red_light + "\">上傳 207MB</font>");
		the_amount_of_network_traffic_today_title.setText(getString(R.string.home_page_the_amount_of_network_traffic_today));
		the_amount_of_network_traffic_today_usage.setText(textContent);
		progressBar.setProgress(40);
		progressBar.setSecondaryProgress(100);

		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.add_new_mount_point);
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
		TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		int baseBottom = getResources().getDimensionPixelSize(typedValue.resourceId);
		int bottomMargin = (int) (baseBottom + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));
		int rightMargin = (int) getResources().getDimension(R.dimen.fab_right_margin);
		params.setMargins(0, 0, rightMargin, bottomMargin);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AddMountPointActivity.class);
				startActivity(intent);
				// Snackbar.make(view, "snackbar",
				// Snackbar.LENGTH_LONG).setAction("Action", null).show();
			}
		});
		fab.setTranslationY(bottomMargin);
		fab.animate().translationY(0).setDuration(100).setStartDelay(300);

		return view;
	}

}
