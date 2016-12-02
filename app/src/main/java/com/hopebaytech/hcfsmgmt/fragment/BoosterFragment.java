package com.hopebaytech.hcfsmgmt.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.Toast;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MemoryCacheFactory;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;
import com.hopebaytech.hcfsmgmt.utils.UiHandler;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/07.
 */
public class BoosterFragment extends Fragment {

    public static final String TAG = BoosterFragment.class.getSimpleName();
    private final String CLASSNAME = BoosterFragment.class.getSimpleName();

    private int mCurrentTab = Tab.UNBOOSTED;

    private Context mContext;
    private TextView mUsedSpaceInfo;
    private ImageView mRefresh;
    private RecyclerView mRecycleView;
    private TextView mBoostTab;
    private TextView mUnboostTab;
    private TextView mCancel;
    private TextView mAction;
    private TextView mHintMessage;
    private LinearLayout mActionButtonLayout;

    private BoosterReceiver mBoosterReceiver;

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
        mBoosterReceiver = new BoosterReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logs.d(CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.booster_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

        mUsedSpaceInfo = (TextView) view.findViewById(R.id.used_space_info);
        mRefresh = (ImageView) view.findViewById(R.id.refresh);
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mBoostTab = (TextView) view.findViewById(R.id.boost_tab);
        mUnboostTab = (TextView) view.findViewById(R.id.unboost_tab);
        mCancel = (TextView) view.findViewById(R.id.cancel);
        mAction = (TextView) view.findViewById(R.id.action);
        mHintMessage = (TextView) view.findViewById(R.id.hint_message);
        mActionButtonLayout = (LinearLayout) view.findViewById(R.id.action_button_layout);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logs.d(CLASSNAME, "onActivityCreated", null);

        init();
        showUnboostedAppList();
    }

    @Override
    public void onStart() {
        super.onStart();
        Logs.d(CLASSNAME, "onStart", null);
        mBoosterReceiver.register(mContext);

        BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
        if (!adapter.isProcessing()) {
            // The UI is not processing boost/unboost yet, but we need to check whether the
            // boost/unboost was triggered before. If it has been triggered, we have to show the
            // processing UI.
            boolean isProcessing = true;
            switch (Booster.currentProcessBoostStatus(mContext)) {
                case UidInfo.BoostStatus.UNBOOSTING:
                    setCurrentTab(Tab.BOOSTED);
                    break;
                case UidInfo.BoostStatus.BOOSTING:
                    setCurrentTab(Tab.UNBOOSTED);
                    break;
                default:
                    isProcessing = false;
            }
            if (isProcessing) {
                adapter.notifyProcessingApps();
            }
        }
    }

    private void setCurrentTab(int currentTab) {
        mCurrentTab = currentTab;
        updateUI(true /* update app list */);
    }

    @Override
    public void onStop() {
        super.onStop();
        Logs.d(CLASSNAME, "onStop", null);
        mBoosterReceiver.unregister(mContext);
    }

    private void init() {
        mRecycleView.setLayoutManager(new GridLayoutManager(mContext, 3));
        mRecycleView.setAdapter(new BoosterAdapter());

        setUpListener();
        updateUsedSpaceInfo();
    }

    private void updateUsedSpaceInfo() {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                UiHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) {
                            return;
                        }

                        String usedSpaceInfo = String.format(
                                Locale.getDefault(),
                                getString(R.string.booster_used_space_info),
                                UnitConverter.convertByteToProperUnit(Booster.getBoosterUsedSpace()),
                                UnitConverter.convertByteToProperUnit(Booster.getBoosterTotalSpace())
                        );
                        mUsedSpaceInfo.setText(usedSpaceInfo);
                    }
                });
            }
        });
    }

    private void setUpListener() {
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((BoosterAdapter) mRecycleView.getAdapter()).isProcessing()) {
                    return;
                }
                updateUsedSpaceInfo();
                updateUI(true /* update app list */);
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BoosterAdapter) mRecycleView.getAdapter()).uncheckAllApps();
            }
        });

        mUnboostTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
                if (mCurrentTab == Tab.UNBOOSTED || adapter.isProcessing()) {
                    return;
                }
                adapter.init();
                setCurrentTab(Tab.UNBOOSTED);
            }
        });

        mBoostTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
                if (mCurrentTab == Tab.BOOSTED || adapter.isProcessing()) {
                    return;
                }
                adapter.init();
                setCurrentTab(Tab.BOOSTED);
            }
        });

        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdded()) {
                    return;
                }

                String title = getString(R.string.booster_dialog_disable_apps_title);
                StringBuilder builder = new StringBuilder();
                if (mCurrentTab == Tab.UNBOOSTED) {
                    builder.append(getString(R.string.booster_dialog_disable_apps_message_while_boosting));
                } else {
                    builder.append(getString(R.string.booster_dialog_disable_apps_message_while_unboosting));
                }
                builder.append("\n\n");

                BoosterAdapter adapter = ((BoosterAdapter) mRecycleView.getAdapter());
                for (int i = 0; i < adapter.mCheckedPosition.size(); i++) {
                    int position = adapter.mCheckedPosition.keyAt(i);
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
        switch (mCurrentTab) {
            case Tab.UNBOOSTED:
                ((View) mUnboostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C1));
                ((View) mBoostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C5));
                mAction.setText(R.string.booster_action_boost);
                mHintMessage.setText(R.string.booster_boost_hint_tap_app_to_boost);

                if (updateAppList) {
                    showUnboostedAppList();
                }
                break;
            case Tab.BOOSTED:
                ((View) mBoostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C1));
                ((View) mUnboostTab.getParent()).setBackgroundColor(ContextCompat.getColor(mContext, R.color.C5));
                mAction.setText(R.string.booster_action_unboost);
                mHintMessage.setText(R.string.booster_boost_hint_tap_app_to_unboost);

                if (updateAppList) {
                    showBoostedAppList();
                }
                break;
        }

        BoosterAdapter adapter = ((BoosterAdapter) mRecycleView.getAdapter());
        if (adapter.mCheckedPosition.size() == 0) {
            mActionButtonLayout.setVisibility(View.GONE);
            mHintMessage.setVisibility(View.VISIBLE);
        } else {
            mActionButtonLayout.setVisibility(View.VISIBLE);
            mHintMessage.setVisibility(View.GONE);
        }
    }

    private void showBoostedAppList() {
        final List<AppInfo> appList = Booster.getAppList(mContext, Booster.Type.BOOSTED);
        UiHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
                adapter.setAppList(appList);
                adapter.setCheckedApps(getCheckedApps(appList, UidInfo.BoostStatus.INIT_UNBOOST));
                adapter.notifyDataSetChanged();
            }
        });
    }

    private SparseBooleanArray getCheckedApps(List<AppInfo> appList, int checkedStatus) {
        SparseBooleanArray checkedPosition = new SparseBooleanArray();
        for (int position = 0; position < appList.size(); position++) {
            int boostStatus = appList.get(position).getBoostStatus();
            if (boostStatus == checkedStatus) {
                checkedPosition.put(position, true);
            }
        }
        return checkedPosition;
    }

    private void showUnboostedAppList() {
        final List<AppInfo> appList = Booster.getAppList(mContext, Booster.Type.UNBOOSTED);
        UiHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
                adapter.setAppList(appList);
                adapter.setCheckedApps(getCheckedApps(appList, UidInfo.BoostStatus.INIT_BOOST));
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void executeAction() {
        BoosterAdapter adapter = (BoosterAdapter) mRecycleView.getAdapter();
        adapter.notifyProcessingApps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);

        ((BoosterAdapter) mRecycleView.getAdapter()).freeCache();
    }

    private class BoosterAdapter extends RecyclerView.Adapter<BoosterAdapter.AppViewHolder> {

        private List<AppInfo> mAppList;
        private LruCache<Integer, Drawable> mMemoryCache;

        /**
         * The position which is checked will be added to this array.
         */
        private SparseBooleanArray mCheckedPosition;
        private boolean isProcessing;
        private Timer mProcessingTextTimer;

        private BoosterAdapter() {
            this(null);
        }

        private BoosterAdapter(List<AppInfo> appList) {
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
            if (mCheckedPosition == null) {
                mCheckedPosition = new SparseBooleanArray();
            }

            mAppList.clear();
            mMemoryCache.evictAll();
            mCheckedPosition.clear();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.app_file_grid_item, parent, false);
            return new BoosterAdapter.AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppViewHolder holder, int position) {
            final boolean isChecked = mCheckedPosition.get(position);
            if (isChecked && isProcessing) {
                holder.itemView.setVisibility(View.INVISIBLE);
                return;
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }

            onBindAppViewHolder(holder, position, isChecked);
        }

        @Nullable
        private Drawable getAppIcon(AppInfo appInfo) {
            Drawable iconDrawable = mMemoryCache.get(appInfo.hashCode());
            if (iconDrawable == null) {
                iconDrawable = appInfo.getIconDrawable();
                mMemoryCache.put(appInfo.hashCode(), iconDrawable);
            }
            return iconDrawable;
        }

        private void onBindAppViewHolder(final AppViewHolder holder, int position, boolean isChecked) {
            final AppInfo appInfo = mAppList.get(position);
            holder.appInfo = appInfo;

            int backgroundColor = isChecked ? R.color.colorSmartCacheItemSelectedBackground : android.R.color.transparent;
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, backgroundColor));

            Drawable iconBitmap = getAppIcon(appInfo);
            if (iconBitmap != null) {
                holder.itemIcon.setImageDrawable(iconBitmap);
            }
            holder.itemName.setText(appInfo.getName());

            if ((mCurrentTab == Tab.UNBOOSTED && isChecked) || (mCurrentTab == Tab.BOOSTED && !isChecked)) {
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
                        UiHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                holder.pinView.setImageDrawable(appInfo.getPinViewImage(isPinned));
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

        private boolean isProcessing() {
            return isProcessing;
        }

        private void notifyProcessingApps() {
            isProcessing = true;

            startProcessingTextAnim();
            mActionButtonLayout.setVisibility(View.GONE);

            final List<AppInfo> checkedApps = new LinkedList<>();
            for (int i = 0; i < mCheckedPosition.size(); i++) {
                int position = mCheckedPosition.keyAt(i);
                boolean isChecked = mCheckedPosition.get(position);
                if (isChecked) {
                    checkedApps.add(mAppList.get(position));
                    notifyItemChanged(position);
                }
            }

            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentTab == Tab.UNBOOSTED) {
                        if (Booster.isEnoughBoosterSpace(mContext, checkedApps)) {
                            Booster.updateBoostStatusInSharedPreferenceXml(mContext, UidInfo.BoostStatus.BOOSTING);

                            UidDAO uidDAO = UidDAO.getInstance(mContext);
                            ContentValues cv = new ContentValues();
                            cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.INIT_BOOST);
                            for (AppInfo appInfo : checkedApps) {
                                uidDAO.update(appInfo.getPackageName(), cv);
                                Booster.disableApp(mContext, appInfo.getPackageName());
                            }
                            Booster.triggerBoost();
                            return;
                        }
                    } else { // Tab.BOOSTED
                        if (Booster.isEnoughUnboosterSpace(mContext, checkedApps)) {
                            Booster.updateBoostStatusInSharedPreferenceXml(mContext, UidInfo.BoostStatus.UNBOOSTING);

                            UidDAO uidDAO = UidDAO.getInstance(mContext);
                            ContentValues cv = new ContentValues();
                            cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.INIT_UNBOOST);
                            for (AppInfo appInfo : checkedApps) {
                                uidDAO.update(appInfo.getPackageName(), cv);
                                Booster.disableApp(mContext, appInfo.getPackageName());
                            }
                            Booster.triggerUnboost();
                            return;
                        }
                    }

                    // Processing failed due to insufficient space
                    Booster.removeBoostStatusInSharedPreferenceXml(mContext);
                    UiHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            insufficientSpaceFailed();
                        }
                    });
                }
            });

        }

        private void insufficientSpaceFailed() {
            isProcessing = false;
            uncheckAllApps();
            stopProcessingTextAnim();

            if (mCurrentTab == Tab.UNBOOSTED) {
                Toast.makeText(mContext, R.string.booster_boost_apps_insufficient_space, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, R.string.booster_unboost_apps_insufficient_space, Toast.LENGTH_LONG).show();
            }
        }

        private void notifyProcessingAppsFailed() {
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Booster.enableApps(mContext);
                    Booster.recoverBoostStatusWhenFailed(mContext);
                    Booster.removeBoostStatusInSharedPreferenceXml(mContext);

                    UiHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            isProcessing = false;
                            uncheckAllApps();
                            stopProcessingTextAnim();

                            int resId;
                            if (mCurrentTab == Tab.UNBOOSTED) {
                                resId = R.string.booster_boost_apps_failed;
                            } else { // Tab.BOOSTED
                                resId = R.string.booster_unboost_apps_failed;
                            }
                            Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        private void notifyProcessingAppsCompleted() {
            Booster.enableApps(mContext);
            Booster.removeBoostStatusInSharedPreferenceXml(mContext);

            List<Integer> removePositionList = new ArrayList<>();
            for (int i = 0; i < mCheckedPosition.size(); i++) {
                int position = mCheckedPosition.keyAt(i);
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
            UiHandler.getInstance().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }

                    final View targetView;
                    final String title;
                    final String message;
                    if (mCurrentTab == Tab.UNBOOSTED) {
                        targetView = (View) mBoostTab.getParent();

                        // In unboosted page, show boost success message after processing
                        title = getString(R.string.booster_dialog_boost_title);
                        message = getString(R.string.booster_dialog_boost_message);
                    } else {
                        targetView = (View) mUnboostTab.getParent();

                        // In boosted page, show unboost success message after processing
                        title = getString(R.string.booster_dialog_unboost_title);
                        message = getString(R.string.booster_dialog_unboost_message);
                    }

                    final Animation scaleAnim = AnimationUtils.loadAnimation(mContext, R.anim.boost_unboust_scale);
                    scaleAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            targetView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.C8));
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mCheckedPosition.clear();
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
            if (mCurrentTab == Tab.BOOSTED) { // In boosted page, execute unboost action
                processingMsg = getString(R.string.booster_boost_hint_unboosting);
            } else { // In unboosted page, execute boost action
                processingMsg = getString(R.string.booster_boost_hint_boosting);
            }
            mHintMessage.setText(processingMsg);
            mHintMessage.setVisibility(View.VISIBLE);

            mProcessingTextTimer = new Timer();
            mProcessingTextTimer.schedule(new TimerTask() {

                private final int MAX_DOT_NUMS = 3;
                private int dotNum = 0;

                @Override
                public void run() {
                    UiHandler.getInstance().post(new Runnable() {
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

            UiHandler.getInstance().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String message;
                    if (mCurrentTab == Tab.UNBOOSTED) { // In unboosted page, show boost message after processing
                        message = getString(R.string.booster_boost_hint_tap_app_to_boost);
                    } else { // In boosted page, show unboost message after processing
                        message = getString(R.string.booster_boost_hint_tap_app_to_unboost);
                    }
                    mHintMessage.setText(message);
                }
            }, 200);
        }

        private void uncheckAllApps() {
            List<Integer> checkedPositionList = new ArrayList<>();
            for (int i = 0; i < mCheckedPosition.size(); i++) {
                int position = mCheckedPosition.keyAt(i);
                boolean isChecked = mCheckedPosition.get(position);
                if (isChecked) {
                    checkedPositionList.add(position);
                }
            }
            mCheckedPosition.clear();

            for (int position : checkedPositionList) {
                notifyItemChanged(position);
            }

            mActionButtonLayout.setVisibility(View.GONE);
            mHintMessage.setVisibility(View.VISIBLE);
        }

        private List<AppInfo> getAppList() {
            return mAppList;
        }

        private void setAppList(List<AppInfo> mAppList) {
            this.mAppList = mAppList;
        }

        public void setCheckedApps(SparseBooleanArray checkedPosition) {
            this.mCheckedPosition = checkedPosition;
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
                boolean isChecked = mCheckedPosition.get(position);

                if (isChecked) { // Original state is checked, so un-check it
                    mCheckedPosition.delete(position);
                    notifyItemChanged(position);
                } else {
                    mCheckedPosition.put(position, true);
                    notifyItemChanged(position);
                }

                if (mCheckedPosition.size() != 0) {
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

    private class BoosterReceiver extends BroadcastReceiver {

        private boolean isRegistered;

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationEvent.cancel(mContext, NotificationEvent.ID_BOOSTER);
            BoosterAdapter adapter = ((BoosterAdapter) mRecycleView.getAdapter());
            switch (intent.getAction()) {
                case TeraIntent.ACTION_BOOSTER_PROCESS_COMPLETED:
                    adapter.notifyProcessingAppsCompleted();
                    break;
                case TeraIntent.ACTION_BOOSTER_PROCESS_FAILED:
                    adapter.notifyProcessingAppsFailed();
                    break;
            }
        }

        private void register(Context context) {
            if (!isRegistered) {
                isRegistered = true;

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(TeraIntent.ACTION_BOOSTER_PROCESS_COMPLETED);
                intentFilter.addAction(TeraIntent.ACTION_BOOSTER_PROCESS_FAILED);
                context.registerReceiver(this, intentFilter);
            }
        }

        private void unregister(Context context) {
            if (isRegistered) {
                isRegistered = false;

                context.unregisterReceiver(this);
            }
        }

    }

}
