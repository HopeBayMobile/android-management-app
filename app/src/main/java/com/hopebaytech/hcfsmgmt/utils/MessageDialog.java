package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;

/**
 * A template dialog which contains title, message, positive button and negative button. The
 * MessageDialog can be shown from a {@link android.app.Service}.
 *
 * @author Aaron
 *         Created by Aaron on 2016/12/19.
 */
public class MessageDialog {

    /**
     * Get a customized alert dialog according the parameters given by caller. <strong>Note: If the
     * string resource id of negativeText is 0, the negative button will be hidden.</strong>
     *
     * @param title            the string resource id of title
     * @param message          the string resource id of message
     * @param positiveText     the positive string resource id of positive button
     * @param positiveListener the {@link android.view.View.OnClickListener} of positive button
     * @param negativeText     the negative string resource id of negative button
     * @param negativeListener the {@link android.view.View.OnClickListener} of negative button
     * @return a customized alert dialog according the parameters given by caller.
     */
    public static AlertDialog getDialog(final Context context,
                                        @StringRes int title,
                                        @StringRes int message,
                                        @StringRes int positiveText,
                                        final View.OnClickListener positiveListener,
                                        @StringRes int negativeText,
                                        final View.OnClickListener negativeListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_dialog_fragment, null);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        ((TextView) view.findViewById(R.id.message)).setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatDialog);
        builder.setView(view);
        builder.setPositiveButton(positiveText, null);
        if (negativeText != 0) {
            builder.setNegativeButton(negativeText, null);
        }
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (positiveListener != null) {
                            positiveListener.onClick(v);
                        }
                        alertDialog.dismiss();
                    }
                });

                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (negativeButton != null) {
                    negativeButton.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (negativeListener != null) {
                                negativeListener.onClick(v);
                            }
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        });
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        }
        return alertDialog;
    }

    /**
     * @return a alert dialog without negative button.
     * @see MessageDialog#getDialog(Context, int, int, int, View.OnClickListener, int, View.OnClickListener)
     */
    public static AlertDialog getDialog(final Context context,
                                        @StringRes int title,
                                        @StringRes int message,
                                        @StringRes int positiveText,
                                        final View.OnClickListener positiveListener) {
        return getDialog(context, title, message, positiveText, positiveListener, 0, null);
    }

    /**
     * @return a alert dialog which has default positive text and positive button, but without
     * negative button.
     * @see MessageDialog#getDialog(Context, int, int, int, View.OnClickListener, int, View.OnClickListener)
     */
    public static AlertDialog getDialog(final Context context,
                                        @StringRes int title,
                                        @StringRes int message) {
        return getDialog(context, title, message, R.string.ok, null, 0, null);
    }
}
