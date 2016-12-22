package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.customview.BoosterSeekBar;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.MessageDialog;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;
import com.hopebaytech.hcfsmgmt.utils.UiHandler;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/14.
 */

public class EnableBoosterDialogFragment extends DialogFragment {

    public static final String TAG = EnableBoosterDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    public static final String KEY_BOOSTER_SIZE = "key_booster_size";
    public static final int RESULT_FAILED = -2;

    private Context mContext;

    public static EnableBoosterDialogFragment newInstance() {
        return new EnableBoosterDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = ((MainActivity) mContext).getLayoutInflater();
        final View view = inflater.inflate(R.layout.enable_booster_dialog, null);

        final BoosterSeekBar boosterSeekBar = (BoosterSeekBar) view.findViewById(R.id.booster_seek_bar);
        boosterSeekBar.setEnabled(false);
        boosterSeekBar.setValueFormatter(new BoosterSeekBar.ValueFormatter() {
            @Override
            public String getFormatValue(double value) {
                return UnitConverter.convertByteToProperUnit((long) value);
            }
        });
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final long minimumBoosterSpace = Booster.getMinimumBoosterSpace();
                final long maximumBoosterSpace = Booster.getAvailableBoosterSpace();
                UiHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        boosterSeekBar.setEnabled(true);
                        boosterSeekBar.setValueRange(minimumBoosterSpace, maximumBoosterSpace);
                    }
                });
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(R.string._continue, null)
                .setNegativeButton(R.string.cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button continueBtn = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                final Button cancelBtn = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);

                continueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        continueBtn.setEnabled(false);
                        cancelBtn.setEnabled(false);

                        startBoost(view);
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelBoost();
                    }
                });
            }
        });
        return alertDialog;
    }

    private void startBoost(View view) {
        BoosterSeekBar boosterSeekBar = (BoosterSeekBar) view.findViewById(R.id.booster_seek_bar);
        LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        contentLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        final double boosterSize = boosterSeekBar.getValue();
        if (boosterSize < Booster.getMinimumBoosterSpace()) {
            Toast.makeText(
                    mContext,
                    R.string.booster_enable_dialog_insufficient_pinned_space,
                    Toast.LENGTH_LONG
            ).show();
            cancelBoost();
            return;
        }

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {

                boolean isSuccess = Booster.enableBooster((long) boosterSize);

                final int resultCode;
                if (isSuccess) {
                    SettingsInfo settingsInfo = new SettingsInfo();
                    settingsInfo.setKey(SettingsFragment.PREF_ENABLE_BOOSTER);
                    settingsInfo.setValue(String.valueOf(true));

                    SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                    settingsDAO.update(settingsInfo);

                    resultCode = Activity.RESULT_OK;
                } else {
                    resultCode = RESULT_FAILED;
                }

                UiHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
                        dismiss();
                    }
                });
            }
        });

    }

    private void cancelBoost() {
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
        dismiss();
    }

}
