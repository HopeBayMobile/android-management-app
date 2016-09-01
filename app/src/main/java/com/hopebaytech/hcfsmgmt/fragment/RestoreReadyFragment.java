package com.hopebaytech.hcfsmgmt.fragment;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.PowerUtils;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/25.
 */
public class RestoreReadyFragment extends Fragment {

    public static final String TAG = RestoreReadyFragment.class.getSimpleName();

    private Context mContext;

    public static RestoreReadyFragment newInstance() {
        return new RestoreReadyFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_ready_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView systemReboot = (TextView) view.findViewById(R.id.system_reboot);
        systemReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.FULL_RESTORE_IN_PROGRESS);
                editor.apply();

                PowerUtils.rebootSystem(mContext);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        NotificationEvent.cancel(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
    }

    @Override
    public void onStop() {
        super.onStop();

        String rebootAction = getString(R.string.restore_system_reboot);
        Intent rebootIntent = new Intent(mContext, TeraMgmtService.class);
        rebootIntent.setAction(TeraIntent.ACTION_REBOOT_SYSTEM);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, rebootIntent, 0);
        NotificationCompat.Action action = new NotificationCompat.Action(0, rebootAction, pendingIntent);

        int notifyId = HCFSMgmtUtils.NOTIFY_ID_ONGOING;
        int flag = NotificationEvent.FLAG_ON_GOING | NotificationEvent.FLAG_HEADS_UP;
        String title = getString(R.string.restore_ready_title);
        String message = getString(R.string.restore_ready_message);
        NotificationEvent.notify(mContext, notifyId, title, message, action, flag);
    }

}
