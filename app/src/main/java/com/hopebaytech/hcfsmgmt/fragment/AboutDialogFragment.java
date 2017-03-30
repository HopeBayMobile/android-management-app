package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

public class AboutDialogFragment extends DialogFragment {

    public static final String TAG = AboutDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private final int CLICK_COUNT_FOR_BA = 7;

    private boolean isImeiShown = false;
    private boolean denyGrandPermission = false;
    private int clickCount = CLICK_COUNT_FOR_BA;

    private Handler mHandler;

    private View mView;
    private Context mContext;
    private TextView mImeiOneTitle;
    private TextView mImeiOne;
    private TextView mImeiTwo;
    private LinearLayout mImeiTwoLayout;
    private Snackbar mSnackbar;

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String packageName = mContext.getPackageName();
            Intent teraPermissionSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
            teraPermissionSettings.addCategory(Intent.CATEGORY_DEFAULT);
            teraPermissionSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(teraPermissionSettings);
        }
    };

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.d(CLASSNAME, "onCreate", null);
        mContext = getActivity();
        mHandler = new Handler();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Logs.d(CLASSNAME, "onCreateDialog", null);

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        mView = inflater.inflate(R.layout.about_dialog_fragment, null);
        mImeiOne = (TextView) mView.findViewById(R.id.device_imei_1);
        mImeiTwo = (TextView) mView.findViewById(R.id.device_imei_2);
        mImeiOneTitle = (TextView) mView.findViewById(R.id.device_imei_1_title);
        mImeiTwoLayout = (LinearLayout) mView.findViewById(R.id.device_imei_2_layout);

        TextView teraVersion = (TextView) mView.findViewById(R.id.terafonn_version);
        teraVersion.setText(getString(R.string.tera_version));

        LinearLayout mTeraVersionLayout = (LinearLayout) mView.findViewById(R.id.tera_version_layout);
        mTeraVersionLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                Boolean shown = sharedPreferences.getBoolean(SettingsFragment.PREF_SHOW_BA_LOGGING_OPTION, false);
                if (!shown) {
                    clickCount -= 1;
                    if (clickCount <= 0) {
                        // Show enable BA logging option
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(SettingsFragment.PREF_SHOW_BA_LOGGING_OPTION, true);
                        editor.apply();
                        Toast.makeText(mContext, R.string.settings_extra_log_for_ba_enabled, Toast.LENGTH_LONG).show();
                    }
                    startTimer();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(mView);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent data = new Intent();
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logs.d(CLASSNAME, "onActivityCreated", null);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Intent data = new Intent();
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
    }

    final Runnable resetCounter = new Runnable() {
        @Override
        public void run() {
            clickCount = CLICK_COUNT_FOR_BA;
        }
    };

    public void startTimer() {
        mHandler.removeCallbacks(resetCounter);
        mHandler.postDelayed(resetCounter, 10000);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logs.d(CLASSNAME, "onResume", null);

        if (mSnackbar == null) {
            mSnackbar = Snackbar.make(mView, "", Snackbar.LENGTH_INDEFINITE);
        }

        if (denyGrandPermission) {
            dismiss();
            return;
        }

        if (isImeiShown) {
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            showImei();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                Manifest.permission.READ_PHONE_STATE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString(R.string.alert_dialog_title_warning));
            builder.setMessage(getString(R.string.require_read_phone_state_permission));
            builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                            RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
            });
            builder.setCancelable(false);
            builder.show();
        } else {
            mSnackbar.setText(R.string.require_read_phone_state_permission);
            mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            mSnackbar.setAction(R.string.go, listener);
            mSnackbar.show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logs.d(CLASSNAME, "setMenuVisibility", "requestCode=" + requestCode);

        if (requestCode != RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            return;
        }

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    Manifest.permission.READ_PHONE_STATE)) {
                mSnackbar.setText(R.string.require_read_phone_state_permission);
                mSnackbar.setDuration(Snackbar.LENGTH_LONG);
                mSnackbar.setAction(null, null);
            } else {
                mSnackbar.setText(R.string.require_read_phone_state_permission);
                mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                mSnackbar.setAction(R.string.go, listener);
            }
            denyGrandPermission = true;
        } else {
            showImei();
        }

    }

    public void showImei() {
        TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            if (manager.getPhoneCount() == 0) {
                mImeiOne.setText("-");
                mImeiTwoLayout.setVisibility(View.GONE);
            } else if (manager.getPhoneCount() == 1) {
                mImeiOne.setText(manager.getDeviceId(0));
                mImeiTwoLayout.setVisibility(View.GONE);
                Logs.d(CLASSNAME, "onViewCreated", "imei_1=" + manager.getDeviceId(0));
            } else {
                mImeiOne.setText(manager.getDeviceId(0));
                mImeiTwo.setText(manager.getDeviceId(1));
                Logs.d(CLASSNAME, "onViewCreated", "imei_1=" + manager.getDeviceId(0));
                Logs.d(CLASSNAME, "onViewCreated", "imei_2=" + manager.getDeviceId(1));
            }
        } else {
            mImeiOne.setText(manager.getDeviceId());
            mImeiOneTitle.setText("IMEI");
            mImeiTwoLayout.setVisibility(View.GONE);
            Logs.d(CLASSNAME, "onViewCreated", "imei=" + manager.getDeviceId());
        }
        isImeiShown = true;
    }

}
