package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/31.
 */
public class RestoreDoneFragment extends Fragment {

    public static final String TAG = RestoreDoneFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private Context mContext;

    public static RestoreDoneFragment newInstance() {
        return new RestoreDoneFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.d(CLASSNAME, "onCreate", null);
        mContext = getActivity();

        removeRestoreStatus();
    }

    private void removeRestoreStatus() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HCFSMgmtUtils.PREF_RESTORE_STATUS);
        editor.apply();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logs.d(CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.restore_done_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

        TextView exit = (TextView) view.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) mContext).finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);
    }

}
