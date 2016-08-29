package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

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
                Uri uri = Uri.parse("cs@hbmobile.com");
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                PackageManager manager = mContext.getPackageManager();
                List<ResolveInfo> infoList = manager.queryIntentActivities(intent, 0);
                if (infoList.size() != 0) {
                    startActivity(intent);
                } else {
                    Snackbar snackbar = Snackbar.make(mView, R.string.settings_snackbar_no_available_email_app_found, Snackbar.LENGTH_LONG);
                    TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    textView.setMaxLines(10);
                    snackbar.show();
                }
            }
        });
    }

}
