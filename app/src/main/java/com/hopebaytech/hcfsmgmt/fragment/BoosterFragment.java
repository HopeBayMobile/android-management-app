package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MemoryCacheFactory;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aaron Daniel
 *         Created by Daniel on 2016/8/11.
 */
public class BoosterFragment extends Fragment {

    public static final String TAG = BoosterFragment.class.getSimpleName();
    private final String CLASSNAME = BoosterFragment.class.getSimpleName();

    private Context mContext;
    private RecyclerView mRecycleView;
    private TextView mBoostTab;
    private TextView mUnboostTab;
    private TextView mCancel;
    private TextView mAction;
    private TextView mHintMessage;
    private LinearLayout mActionButtonLayout;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;
    private Handler mUiHandler;

    private int mCurrnetTab = Tab.UNBOOSTED;

    public static BoosterFragment newInstance() {
        return new BoosterFragment();
    }

    private static class Tab {
        private static final int BOOSTED = 1;
        private static final int UNBOOSTED = 2;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.d(CLASSNAME, "onCreate", null);

        mContext = getActivity();
        mWorkerThread = new HandlerThread(CLASSNAME);
        mWorkerThread.start();
        mWorkerHandler = new Handler(mWorkerThread.getLooper());
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
        mHintMessage = (TextView) view.findViewById(R.id.hint_message);
        mActionButtonLayout = (LinearLayout) view.findViewById(R.id.action_button_layout);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

        init();
        showUnboostedAppList();
    }

    private void init() {
        mRecycleView.setLayoutManager(new GridLayoutManager(mContext, 3));
        mRecycleView.setAdapter(new BoosterAdapter());

        setUpListener();
    }

    private void setUpListener() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BoosterAdapter) mRecycleView.getAdapter()).uncheckAllApps();
            }
        });

        mUnboostTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrnetTab == Tab.UNBOOSTED ||
                        ((BoosterAdapter) mRecycleView.getAdapter()).isProcessing()) {
                    return;
                }
                mCurrnetTab = Tab.UNBOOSTED;

                ((BoosterAdapter) mRecycleView.getAdapter()).init();
                updateUI(true /* update app list */);
            }
        });

        mBoostTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrnetTab == Tab.BOOSTED ||
                        ((BoosterAdapter) mRecycleView.getAdapter()).isProcessing()) {
                    return;
                }
                mCurrnetTab = Tab.BOOSTED;

                ((BoosterAdapter) mRecycleView.getAdapter()).init();
                updateUI(true /* update app list */);
            }
        });

        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdded()) {
                    return;
                }

                String title = getString(R.string.smart_cache_dialog_disable_apps_title);
                StringBuilder builder = new StringBuilder();
                if (mCurrnetTab == Tab.UNBOOSTED) {
                    builder.append(getString(R.string.smart_cache_dialog_disable_apps_message_while_boosting));
                } else {
                    builder.append(getString(R.string.smart_cache_dialog_disable_apps_message_while_unboosting));
                }
                builder.append("\n\n");

                BoosterAdapter adapter = ((BoosterAdapter) mRecycleView.getAdapter());
                for (int i = 0; i < adapter.mCheckedPositionArr.size(); i++) {
                    int position = adapter.mCheckedPositionArr.keyAt(i);
                    builder.append(adapter.getAppList().get(position).getName());
                    builder.append("\n");
                }

                MessageDialogFragment dialog = MessageDialogFragment.newInstance();
                dialog.setTitle(title);
                dialog.setMessage(builder.toString());
                dialog.setOnclickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executeAction();
                    }
                });
                dialog.show(getFragmentManager(), MessageDialogFragment.TAG);
            }
        });
    }

    private void updateUI(boolean updateAppList) {
        switch (mCurrnetTab) {
            case Tab.UNBOOSTED:
                ((View) mUnboostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C1));
                ((View) mBoostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C5));
                mAction.setText(R.string.smart_cache_action_boost);
                mHintMessage.setText(R.string.smart_cache_boost_hint_tap_app_to_boost);

                if (updateAppList) {
                    showUnboostedAppList();
                }
                break;
            case Tab.BOOSTED:
                ((View) mBoostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C1));
                ((View) mUnboostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C5));
                mAction.setText(R.string.smart_cache_action_unboost);
                mHintMessage.setText(R.string.smart_cache_boost_hint_tap_app_to_unboost);

                if (updateAppList) {
                    showBoostedAppList();
                }
                break;
        }

        BoosterAdapter adapter = ((BoosterAdapter) mRecycleView.getAdapter());
        Logs.w(CLASSNAME, "updateUI", "checkedPositionArr.size()=" + adapter.mCheckedPositionArr.size());
        if (adapter.mCheckedPositionArr.size() == 0) {
            mActionButtonLayout.setVisibility(View.GONE);
            mHintMessage.setVisibility(View.VISIBLE);
        } else {
            mActionButtonLayout.setVisibility(View.VISIBLE);
            mHintMessage.setVisibility(View.GONE);
        }
    }

    private void showBoostedAppList() {
        final List<ItemInfo> appList = DisplayTypeFactory.getListOfInstalledApps(mContext, DisplayTypeFactory.APP_USER);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
                adapter.setAppList(appList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showUnboostedAppList() {
        final List<ItemInfo> appList = DisplayTypeFactory.getListOfInstalledApps(mContext, DisplayTypeFactory.APP_SYSTEM);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
                adapter.setAppList(appList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void executeAction() {
        BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
        adapter.setProcessing(true);
        adapter.notifyProcessingApps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onViewCreated", null);

        ((BoosterAdapter) mRecycleView.getAdapter()).freeCache();

        if (mWorkerThread != null) {
            mWorkerThread.quit();
            mWorkerThread = null;
        }
    }

    private class BoosterAdapter extends RecyclerView.Adapter<BoosterAdapter.AppViewHolder> {

        private List<ItemInfo> mAppList;
        private LruCache<Integer, Bitmap> mMemoryCache;

        /**
         * The position which is checked will be added to this array.
         */
        private SparseBooleanArray mCheckedPositionArr;
        private boolean isProcessing;
        private Timer mProcessingTextTimer;

        private BoosterAdapter() {
            this(null);
        }

        private BoosterAdapter(List<ItemInfo> appList) {
            mAppList = appList;
            init();
        }

        private void init() {
            if (mAppList == null) {
                mAppList = new ArrayList<>();
            }
            if (mMemoryCache == null) {
                mMemoryCache = MemoryCacheFactory.createMemoryCache();
            }
            if (mCheckedPositionArr == null) {
                mCheckedPositionArr = new SparseBooleanArray();
            }

            mAppList.clear();
            mMemoryCache.evictAll();
            mCheckedPositionArr.clear();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.app_file_grid_item, parent, false);
            return new BoosterAdapter.AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppViewHolder holder, int position) {
            final boolean isChecked = mCheckedPositionArr.get(position);
            if (isChecked && isProcessing) {
                holder.itemView.setVisibility(View.INVISIBLE);
                return;
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }

            onBindAppViewHolder(holder, position, isChecked);
        }

        @Nullable
        private Bitmap getAppIcon(AppInfo appInfo) {
            Bitmap iconBitmap = mMemoryCache.get(appInfo.hashCode());
            if (iconBitmap == null) {
                iconBitmap = appInfo.getIconImage();
                mMemoryCache.put(appInfo.hashCode(), iconBitmap);
            }
            return iconBitmap;
        }

        private void onBindAppViewHolder(final AppViewHolder holder, int position, boolean isChecked) {
            final AppInfo appInfo = (AppInfo) mAppList.get(position);
            holder.appInfo = appInfo;

            int backgroundColor = isChecked ? R.color.colorSmartCacheItemSelectedBackground : android.R.color.transparent;
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, backgroundColor));

            Bitmap iconBitmap = getAppIcon(appInfo);
            if (iconBitmap != null) {
                holder.itemIcon.setImageBitmap(iconBitmap);
            }
            holder.itemName.setText(appInfo.getName());

            if ((mCurrnetTab == Tab.UNBOOSTED && isChecked) || (mCurrnetTab == Tab.BOOSTED && !isChecked)) {
                holder.pinView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_btn_app_boosted));
            } else {
                ThreadPool.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!holder.appInfo.getName().equals(appInfo.getName())) {
                            return;
                        }

                        final boolean isPinned = HCFSMgmtUtils.isAppPinned(mContext, appInfo);
                        appInfo.setPinned(isPinned);
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.pinView.setImageDrawable(appInfo.getPinUnpinImage(isPinned));
                            }
                        });
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mAppList.size();
        }

        private void setProcessing(boolean processing) {
            isProcessing = processing;
        }

        private boolean isProcessing() {
            return isProcessing;
        }

        private void notifyProcessingApps() {
            startProcessingTextAnim();
            mActionButtonLayout.setVisibility(View.GONE);

            for (int i = 0; i < mCheckedPositionArr.size(); i++) {
                int position = mCheckedPositionArr.keyAt(i);
                boolean isChecked = mCheckedPositionArr.get(position);
                if (isChecked) {
                    notifyItemChanged(position);
                }
            }

            // Suppose apps have been boosted
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyProcessingAppsCompleted();
                }
            }, 3000);
        }

        private void notifyProcessingAppsCompleted() {
            List<Integer> removePositionList = new ArrayList<>();
            for (int i = 0; i < mCheckedPositionArr.size(); i++) {
                int position = mCheckedPositionArr.keyAt(i);
                removePositionList.add(position);
            }

            int removeCount = 0;
            Collections.sort(removePositionList);
            for (int position : removePositionList) {
                int removePosition = position - removeCount;
                mAppList.remove(mAppList.get(removePosition));
                notifyItemRemoved(removePosition);
                removeCount++;
            }

            stopProcessingTextAnim();
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }

                    final View targetView;
                    final String title;
                    final String message;
                    if (mCurrnetTab == Tab.UNBOOSTED) {
                        targetView = (View) mBoostTab.getParent();

                        // In unboosted page, show boost success message after processing
                        title = getString(R.string.smart_cache_dialog_boost_title);
                        message = getString(R.string.smart_cache_dialog_boost_message);
                    } else {
                        targetView = (View) mUnboostTab.getParent();

                        // In boosted page, show unboost success message after processing
                        title = getString(R.string.smart_cache_dialog_unboost_title);
                        message = getString(R.string.smart_cache_dialog_unboost_message);
                    }

                    final Animation scaleAnim = AnimationUtils.loadAnimation(mContext, R.anim.boost_unboust_scale);
                    scaleAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            targetView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.C8));
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mCheckedPositionArr.clear();
                            isProcessing = false;

                            updateUI(false /* update app list */);

                            MessageDialogFragment dialog = MessageDialogFragment.newInstance();
                            dialog.setTitle(title);
                            dialog.setMessage(message);
                            dialog.show(getFragmentManager(), MessageDialogFragment.TAG);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    targetView.startAnimation(scaleAnim);
                }
            }, 500);
        }

        private void startProcessingTextAnim() {
            if (!isAdded()) {
                return;
            }

            final String processingMsg;
            if (mCurrnetTab == Tab.BOOSTED) { // In boosted page, execute unboost action
                processingMsg = getString(R.string.smart_cache_boost_hint_unboosting);
            } else { // In unboosted page, execute boost action
                processingMsg = getString(R.string.smart_cache_boost_hint_boosting);
            }
            mHintMessage.setText(processingMsg);
            mHintMessage.setVisibility(View.VISIBLE);

            mProcessingTextTimer = new Timer();
            mProcessingTextTimer.schedule(new TimerTask() {

                private final int MAX_DOT_NUMS = 3;
                private int dotNum = 0;

                @Override
                public void run() {
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String dot = "";
                            for (int i = 0; i < dotNum; i++) {
                                dot += ".";
                            }
                            String message = processingMsg + dot;
                            mHintMessage.setText(message);
                        }
                    });

                    dotNum++;
                    if (dotNum > MAX_DOT_NUMS) {
                        dotNum = 0;
                    }
                }
            }, 0, 500);
        }

        private void stopProcessingTextAnim() {
            if (mProcessingTextTimer != null) {
                mProcessingTextTimer.cancel();
            }

            if (!isAdded()) {
                return;
            }

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Logs.w(CLASSNAME, "stopProcessingTextAnim", mHintMessage.getText().toString());
                    String message;
                    if (mCurrnetTab == Tab.UNBOOSTED) { // In unboosted page, show boost message after processing
                        message = getString(R.string.smart_cache_boost_hint_tap_app_to_boost);
                    } else { // In boosted page, show unboost message after processing
                        message = getString(R.string.smart_cache_boost_hint_tap_app_to_unboost);
                    }
                    mHintMessage.setText(message);
                    Logs.w(CLASSNAME, "stopProcessingTextAnim", mHintMessage.getText().toString());
                }
            }, 200);
        }

        private void uncheckAllApps() {
            List<Integer> checkedPositionList = new ArrayList<>();
            for (int i = 0; i < mCheckedPositionArr.size(); i++) {
                int position = mCheckedPositionArr.keyAt(i);
                boolean isChecked = mCheckedPositionArr.get(position);
                if (isChecked) {
                    checkedPositionList.add(position);
                }
            }
            mCheckedPositionArr.clear();

            for (int position : checkedPositionList) {
                notifyItemChanged(position);
            }

            mActionButtonLayout.setVisibility(View.GONE);
            mHintMessage.setVisibility(View.VISIBLE);
        }

        public List<ItemInfo> getAppList() {
            return mAppList;
        }

        public void setAppList(List<ItemInfo> mAppList) {
            this.mAppList = mAppList;
        }

        class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private View itemView;
            private ImageView itemIcon;
            private ImageView pinView;
            private TextView itemName;

            private AppInfo appInfo;

            private AppViewHolder(View itemView) {
                super(itemView);

                this.itemView = itemView;
                this.itemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
                this.itemName = (TextView) itemView.findViewById(R.id.item_name);
                this.pinView = (ImageView) itemView.findViewById(R.id.pin_view);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (isProcessing) {
                    return;
                }

                int position = getAdapterPosition();
                boolean isChecked = mCheckedPositionArr.get(position);

                if (isChecked) { // Original state is checked, so un-check it
                    mCheckedPositionArr.delete(position);
                    notifyItemChanged(position);
                } else {
                    mCheckedPositionArr.put(position, true);
//                    showCheckedAppState(this);
                    notifyItemChanged(position);
                }

                if (mCheckedPositionArr.size() != 0) {
                    mHintMessage.setVisibility(View.GONE);
                    mActionButtonLayout.setVisibility(View.VISIBLE);
                } else {
                    mHintMessage.setVisibility(View.VISIBLE);
                    mActionButtonLayout.setVisibility(View.GONE);
                }
            }

        }

        private void freeCache() {
            mMemoryCache.evictAll();
        }

    }

}
