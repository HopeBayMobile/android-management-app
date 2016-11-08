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
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/7.
 */

public class MessageDialogFragment extends DialogFragment {

    public static final String TAG = MessageDialogFragment.class.getSimpleName();

    private final String CLASSNAME = TAG;

    private Context mContext;
    private String mTitle;
    private String mMessage;

    private DialogInterface.OnClickListener mOnclickListener;

    public static MessageDialogFragment newInstance() {
        return new MessageDialogFragment();
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
        View view = inflater.inflate(R.layout.message_dialog_fragment, null);
        if (mTitle != null) {
            ((TextView) view.findViewById(R.id.title)).setText(mTitle);
        }
        if (mMessage != null) {
            TextView message = (TextView) view.findViewById(R.id.hint_message);
            message.setMovementMethod(new ScrollingMovementMethod());
            message.setText(mMessage);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, mOnclickListener);
        return builder.create();
    }

    void setMessage(String message) {
        mMessage = message;
    }

    void setTitle(String title) {
        mTitle = title;
    }

    public void setOnclickListener(DialogInterface.OnClickListener listener) {
        this.mOnclickListener = listener;
    }
}
