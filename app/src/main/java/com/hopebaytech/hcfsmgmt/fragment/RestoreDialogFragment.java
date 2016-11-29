package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/18.
 */
public class RestoreDialogFragment extends DialogFragment {

    public static final String TAG = RestoreDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;
    private Context mContext;

    public static RestoreDialogFragment newInstance() {
        return new RestoreDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int restoreType = args.getInt(RestoreFragment.KEY_RESTORE_TYPE);
        int resId;
        switch (restoreType) {
            case RestoreFragment.RESTORE_TYPE_NEW_DEVICE:
                resId = R.string.restore_content_desc_setup_as_new_device;
                break;
            case RestoreFragment.RESTORE_TYPE_LOCK_DEVICE:
                resId = R.string.restore_content_desc_lock_device;
                break;
            case RestoreFragment.RESTORE_TYPE_NON_LOCK_DEVICE:
                resId = R.string.restore_content_desc_non_lock_device;
                break;
            default:
                resId = R.string.restore_content_desc_setup_as_new_device;
        }

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.restore_dialog_fragment, null);
        TextView message = (TextView) view.findViewById(R.id.hint_message);
        message.setText(resId);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, null);

        return builder.create();
    }
}
