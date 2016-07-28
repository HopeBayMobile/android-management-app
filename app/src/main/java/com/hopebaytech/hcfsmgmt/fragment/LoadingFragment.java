package com.hopebaytech.hcfsmgmt.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.internal.widget.PreferenceImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/19.
 */
public class LoadingFragment extends Fragment {

    public static final String TAG = LoadingFragment.class.getSimpleName();
    private final String CLASSNAME = LoadingFragment.class.getSimpleName();

    public static LoadingFragment newInstance() {
        return new LoadingFragment();
    }

    private Thread mSplashScreenThread;
    private Runnable mSplashScreenRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 2; i++) {
                    Thread.sleep(1000);
                }

                ActivateWoCodeFragment fragment = ActivateWoCodeFragment.newInstance();
                fragment.setArguments(getArguments());

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, ActivateWoCodeFragment.TAG);
                ft.commit();
            } catch (InterruptedException e) {
                Logs.d(CLASSNAME, "mSplashScreenRunnable", Log.getStackTraceString(e));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loading_activity, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSplashScreenThread == null) {
            mSplashScreenThread = new Thread(mSplashScreenRunnable);
        }
        mSplashScreenThread.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSplashScreenThread != null) {
            mSplashScreenThread.interrupt();
            mSplashScreenThread = null;
        }
    }
}
