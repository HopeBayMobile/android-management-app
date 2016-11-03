package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron Daniel
 *         Created by Daniel on 2016/8/11.
 */
public class SmartCacheFragment extends Fragment {

    public static final String TAG = SmartCacheFragment.class.getSimpleName();
    private final String CLASSNAME = SmartCacheFragment.class.getSimpleName();

    private Context mContext;
    private RecyclerView mRecycleView;
    private TextView mBoostTab;
    private TextView mUnboostTab;
    private TextView mCancel;
    private TextView mAction;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;
    private Handler mUiHandler;

    public static SmartCacheFragment newInstance() {
        return new SmartCacheFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mWorkerThread = new HandlerThread(CLASSNAME);
        mWorkerThread.start();
        mUiHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.smart_cache_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

        mRecycleView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mBoostTab = (TextView) view.findViewById(R.id.boost_tab);
        mUnboostTab = (TextView) view.findViewById(R.id.unboost_tab);
        mCancel = (TextView) view.findViewById(R.id.cancel);
        mAction = (TextView) view.findViewById(R.id.action);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onViewCreated", null);
    }

}
