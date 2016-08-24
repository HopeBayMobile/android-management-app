package com.hopebaytech.hcfsmgmt.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hopebaytech.hcfsmgmt.R;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/23.
 */
public class RestoreRestoringFragment extends Fragment {

    public static final String TAG = RestoreRestoringFragment.class.getSimpleName();

    public static RestoreRestoringFragment newInstance() {
        return new RestoreRestoringFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_restoring_fragment, container, false);
    }

}
