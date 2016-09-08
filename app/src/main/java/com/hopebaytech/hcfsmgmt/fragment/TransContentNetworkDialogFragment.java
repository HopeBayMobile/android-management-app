package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.ChangeAccountActivity;
import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/15.
 */
public class TransContentNetworkDialogFragment extends DialogFragment {

    public static final String TAG = FileMgmtAppDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private Context mContext;

    public static final String KEY_NETWORK_COND = "key_network_cond";

    public static final int COND_MOBILE_WITH_WIFI_ONLY = 1;
    public static final int COND_MOBILE_WITHOUT_WIFI_ONLY = 2;

    public static TransContentNetworkDialogFragment newInstance() {
        return new TransContentNetworkDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.transfer_content_network_dialog, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        CheckBox askCheckbox = (CheckBox) view.findViewById(R.id.ask_checkbox);

        askCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Logs.w(CLASSNAME, "onCheckedChanged", "isChecked=" + isChecked);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SettingsFragment.PREF_ASK_MOBILE_WITHOUT_WIFI_ONLY, isChecked);
                editor.apply();
            }
        });

        int positiveResId;
        int negativeResId;
        Bundle extras = getArguments();
        final int cond = extras.getInt(KEY_NETWORK_COND);
        if (cond == COND_MOBILE_WITH_WIFI_ONLY) {
            positiveResId = R.string.sync_settings;
            negativeResId = R.string.connect_to_wifi;
            askCheckbox.setVisibility(View.GONE);
            title.setText(R.string.settings_transfer_content_syc_data_stopped_title);
            message.setText(R.string.settings_transfer_content_syc_data_stopped_message);
        } else { // COND_MOBILE_WITHOUT_WIFI_ONLY
            positiveResId = R.string.settings;
            negativeResId = R.string._continue;
            askCheckbox.setVisibility(View.VISIBLE);
            title.setText(R.string.settings_transfer_content_use_mobile_network_title);
            message.setText(R.string.settings_transfer_content_use_mobile_network_message);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(positiveResId, null)
                .setNegativeButton(negativeResId, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cond == COND_MOBILE_WITHOUT_WIFI_ONLY) { // Go to android settings page
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                        dismiss();
                    }
                });

                Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setTextColor(ContextCompat.getColor(mContext, R.color.C5));
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cond == COND_MOBILE_WITH_WIFI_ONLY) { // Go to android settings page
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        } else { // Continue
                            Intent intent = new Intent(mContext, TransferContentActivity.class);
                            startActivity(intent);
                        }
                        dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }
}
