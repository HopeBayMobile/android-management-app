package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

public class AboutFragment extends Fragment {

    public static final String TAG = AboutFragment.class.getSimpleName();
    private final String CLASSNAME = AboutFragment.class.getSimpleName();
    private boolean isImeiShown = false;
    private Context mContext;
    private TextView mImeiOne;
    private TextView mImeiTwo;
    private Snackbar mSnackbar;

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String packageName = getContext().getPackageName();
            Intent teraPermissionSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
            teraPermissionSettings.addCategory(Intent.CATEGORY_DEFAULT);
            teraPermissionSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(teraPermissionSettings);
        }
    };

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSnackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);

        mImeiOne = (TextView) view.findViewById(R.id.device_imei_1);
        mImeiTwo = (TextView) view.findViewById(R.id.device_imei_2);

        TextView teraVersion = (TextView) view.findViewById(R.id.terafonn_version);
        teraVersion.setText(getString(R.string.tera_version));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            showImei();
            if (mSnackbar != null) {
                mSnackbar.dismiss();
            }
        }
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
                        builder.setMessage(getString(R.string.require_read_phone_state_permission));
                        builder.setPositiveButton(getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        mSnackbar.setText(R.string.require_read_phone_state_permission);
                        mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                        mSnackbar.setAction(R.string.enable_permission, listener);
                        mSnackbar.show();
                    }
                }
            }
        } else {
            if (mSnackbar != null) {
                mSnackbar.dismiss();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE:
                /** If request is cancelled, the result arrays are empty. */
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_PHONE_STATE)) {
                        mSnackbar.setText(R.string.require_read_phone_state_permission);
                        mSnackbar.setDuration(Snackbar.LENGTH_LONG);
                        mSnackbar.setAction(null, null);
                    } else {
                        mSnackbar.setText(R.string.require_read_phone_state_permission);
                        mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                        mSnackbar.setAction(R.string.enable_permission, listener);
                    }
                    mSnackbar.show();
                } else {
                    showImei();
                }
                break;
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
