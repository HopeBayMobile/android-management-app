package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.ZipUtils;

import java.io.File;
import java.util.List;

/**
 * Created by daniel on 2016/8/11.
 */
public class HelpFragment extends Fragment {

    public static final String TAG = HelpFragment.class.getSimpleName();
    private final String CLASSNAME = HelpFragment.class.getSimpleName();

    private Context mContext;
    private View mView;
    private LinearLayout mFAQ;
    private LinearLayout mUserManual;
    private LinearLayout mQuickTour;
    private LinearLayout mCS;
    private LinearLayout mFeedback;

    private boolean cancelAttachLog = false;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.help_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logs.d(CLASSNAME, "onViewCreated", null);
        mView = view;

        mFAQ = (LinearLayout) view.findViewById(R.id.faq);
        mUserManual = (LinearLayout) view.findViewById(R.id.user_manual);
        mQuickTour = (LinearLayout) view.findViewById(R.id.quick_tour);
        mCS = (LinearLayout) view.findViewById(R.id.customer_center);
        mFeedback = (LinearLayout) view.findViewById(R.id.email_feedback);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.faq_url).toString()));
                startActivity(intent);
            }
        });

        mUserManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.faq_url).toString()));
                startActivity(intent);
            }
        });

        mQuickTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.faq_url).toString()));
                startActivity(intent);
            }
        });

        mCS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.faq_url).toString()));
                startActivity(intent);
            }
        });

        mFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(getString(R.string.alert_dialog_title_warning));
                        builder.setMessage(getString(R.string.require_write_external_storage_permission));
                        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        RequestCode.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        attachLogInMail();
                    }
                } else {
                    attachLogInMail();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAttachLog = true;
    }

    private void attachLogInMail() {
        cancelAttachLog = false;
        final ProgressDialog mProgressDialog;
        mProgressDialog = getProgressDialog(
                getString(R.string.settings_feedback_log_collecting_title),
                getString(R.string.settings_feedback_log_collecting_message));
        mProgressDialog.show();

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    boolean collectLogSuccess = HCFSMgmtUtils.collectSysLogs();

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new StringBuilder()
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_time) + "</b></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_opinions) + "</b></p>")
                            .append("<p></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_description) + "</b></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_contact) + "</b></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_phone) + "</b></p>")
                            .toString())
                    );
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cs@hbmobile.com"});

                    PackageManager manager = mContext.getPackageManager();
                    List<ResolveInfo> infoList = manager.queryIntentActivities(intent, 0);
                    if (infoList.size() != 0) {
                        if (collectLogSuccess) {
                            final String source = getString(R.string.zip_source_path);
                            final String target = getString(R.string.zip_target_path);
                            boolean isSuccess = ZipUtils.zip(source, target);
                            File file = new File(target);
                            if (file.exists() && isSuccess) {
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                                intent.setType("application/zip");
                            }
                        }
                        if (!cancelAttachLog) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    mProgressDialog.hide();
                                }
                            });
                            startActivity(intent);
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mProgressDialog.hide();
                                Snackbar snackbar = Snackbar.make(
                                        mView,
                                        R.string.settings_snackbar_no_available_email_app_found,
                                        Snackbar.LENGTH_LONG);
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setMaxLines(10);
                                snackbar.show();
                            }
                        });
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private ProgressDialog getProgressDialog(String title, String msg) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelAttachLog = true;
                        dialog.dismiss();
                    }
                });
        return progressDialog;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        attachLogInMail();
    }

}
