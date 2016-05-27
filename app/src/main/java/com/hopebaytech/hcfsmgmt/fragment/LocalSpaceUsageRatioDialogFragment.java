package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/15.
 */
public class LocalSpaceUsageRatioDialogFragment extends DialogFragment {

    public static final String TAG = FileMgmtAppDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private Context mContext;

    public static LocalSpaceUsageRatioDialogFragment newInstance() {
        return new LocalSpaceUsageRatioDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_usage_ratio_selection_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
        final String ratio = sharedPreferences.getString(SettingsFragment.PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO, defaultValue).concat("%");

        final List<LinearLayout> radioButtonLayoutList = new ArrayList<>();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                for (LinearLayout layout: radioButtonLayoutList) {
//                    ImageView radioImage = (ImageView) layout.findViewById(R.id.radio_image);
//                    radioImage.setImageResource(R.drawable.icon_btn_unselected);
//                }
//                ImageView radioImage = (ImageView) v.findViewById(R.id.radio_image);
//                radioImage.setImageResource(R.drawable.icon_btn_selected);
//
                TextView radioName = (TextView) v.findViewById(R.id.radio_name);

                Intent data = new Intent();
                data.putExtra(SettingsFragment.KEY_RATIO, radioName.getText().toString());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);

                dismiss();
            }
        };

        final LinearLayout radioButtonContainer = (LinearLayout) view.findViewById(R.id.radio_btn_container);
        String[] ratioNameArr = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_name);
        for (String ratioName: ratioNameArr) {
            LinearLayout radioButtonLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.settings_radio_button, radioButtonContainer, false);
            radioButtonLayout.setOnClickListener(listener);

            TextView radioName = (TextView) radioButtonLayout.findViewById(R.id.radio_name);
            radioName.setText(ratioName);

            if (ratioName.equals(ratio)) {
                ImageView radioImage = (ImageView) radioButtonLayout.findViewById(R.id.radio_image);
                radioImage.setImageResource(R.drawable.icon_btn_selected);
            }

            radioButtonContainer.addView(radioButtonLayout);
            radioButtonLayoutList.add(radioButtonLayout);
        }

        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
