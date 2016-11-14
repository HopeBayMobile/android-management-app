package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.customview.BoosterSeekBar;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
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

    private Context mContext;

    private double mMinSize;
    private double mMaxSize;

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
        final View view = inflater.inflate(R.layout.booster_dialog_fragment, null);
        final BoosterSeekBar boosterSeekBar = (BoosterSeekBar) view.findViewById(R.id.booster_seek_bar);
        boosterSeekBar.setValueRange(mMinSize, mMaxSize);
        boosterSeekBar.setValueFormatter(new BoosterSeekBar.ValueFormatter() {
            @Override
            public String getFormatValue(double value) {
                return UnitConverter.convertByteToProperUnit((long) value);
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
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SettingsFragment.PREF_ENABLE_BOOSTER, true);
                editor.apply();

                UiHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
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

    public void setMinSize(double minSize) {
        this.mMinSize = minSize;
    }

    public void setMaxSize(double maxSize) {
        this.mMaxSize = maxSize;
    }

}
