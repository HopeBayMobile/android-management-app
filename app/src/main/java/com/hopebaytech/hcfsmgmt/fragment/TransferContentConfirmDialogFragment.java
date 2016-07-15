package com.hopebaytech.hcfsmgmt.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.main.SwitchAccountActivity;
import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentConfirmDialogFragment extends DialogFragment {

    public static final String TAG = TransferContentConfirmDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentConfirmDialogFragment.class.getSimpleName();

    public static TransferContentConfirmDialogFragment newInstance() {
        return new TransferContentConfirmDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.w(CLASSNAME, "onCreate", null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.settings_transfer_content_confirm_dialog, null);
        TextView username = (TextView) view.findViewById(R.id.username);

        final TextView errorMsg = (TextView) view.findViewById(R.id.error_msg);
        errorMsg.setVisibility(View.GONE);

        AccountDAO accountDAO = AccountDAO.getInstance(getActivity());
        List<AccountInfo> accountInfoList = accountDAO.getAll();
        if (accountInfoList.size() != 0) {
            AccountInfo info = accountInfoList.get(0);
            username.setText(info.getEmail());
        }
        accountDAO.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.transfer, null)
                .setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final EditText password = (EditText) view.findViewById(R.id.password);

                positiveButton.setEnabled(false);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isConnected = NetworkUtils.isNetworkConnected(getActivity());
                        if (NetworkUtils.isNetworkConnected(getActivity())) {
                            errorMsg.setVisibility(View.GONE);

                            Intent intent = new Intent(getActivity(), TransferContentActivity.class);
                            startActivity(intent);
                            dismiss();
                        } else {
                            errorMsg.setVisibility(View.VISIBLE);
                            errorMsg.setText(R.string.activate_alert_dialog_message);
                        }
                    }
                });

                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });

                password.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0) {
                            positiveButton.setEnabled(true);
                            positiveButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                        } else {
                            positiveButton.setEnabled(false);
                            positiveButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.C5));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        return alertDialog;
    }

}
