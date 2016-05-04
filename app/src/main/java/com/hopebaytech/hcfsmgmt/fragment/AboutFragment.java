package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

public class AboutFragment extends Fragment {

    public static final String TAG = AboutFragment.class.getSimpleName();
    private final String CLASSNAME = AboutFragment.class.getSimpleName();
    private boolean isImeiShown = false;
    private Context mContext;
    private TextView mImeiOne;
    private TextView mImeiTwo;

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

        mImeiOne = (TextView) view.findViewById(R.id.device_imei_1);
        mImeiTwo = (TextView) view.findViewById(R.id.device_imei_2);

        TextView terafonnVersion = (TextView) view.findViewById(R.id.terafonn_version);
        terafonnVersion.setText(getString(R.string.terafonn_version));
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            if (!isImeiShown) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    showImei();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_PHONE_STATE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(getString(R.string.alert_dialog_title_warning));
                        builder.setMessage(getString(R.string.main_activity_require_read_phone_state_permission));
                        builder.setPositiveButton(getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{ Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                }
            }
        }
    }

    public void showImei() {
        TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getPhoneCount() == 0) {
            mImeiOne.setText("-");
            mImeiTwo.setText("-");
        } else if (manager.getPhoneCount() == 1) {
            mImeiOne.setText(manager.getDeviceId(0));
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onViewCreated", "imei_1=" + manager.getDeviceId(0));
        } else {
            mImeiOne.setText(manager.getDeviceId(0));
            mImeiTwo.setText(manager.getDeviceId(1));
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onViewCreated", "imei_1=" + manager.getDeviceId(0));
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onViewCreated", "imei_2=" + manager.getDeviceId(1));
        }
        isImeiShown = true;
    }

}
