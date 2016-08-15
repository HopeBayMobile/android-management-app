package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/12.
 */
public class RestorationFragment extends Fragment {

    public static final String TAG = RestorationFragment.class.getSimpleName();

    private final String CLASSNAME = TAG;

    private Context mContext;
    private ExpandableListView mExpandableListView;
    private TextView mBackButton;
    private TextView mNextButton;
    private ProgressBar mProgressCircle;
    private TextView mSearchBackup;

    private RestoreListAdapter mRestoreListAdapter;
    private Checkable mPrevCheckableInfo;

    public static RestorationFragment newInstance() {
        return new RestorationFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restoration_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mExpandableListView = (ExpandableListView) view.findViewById(R.id.expanded_list);
        mBackButton = (TextView) view.findViewById(R.id.back_btn);
        mNextButton = (TextView) view.findViewById(R.id.next_btn);
        mProgressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);
        mSearchBackup = (TextView) view.findViewById(R.id.search_backup);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<GroupInfo> groupList = new ArrayList<>();

        GroupInfo groupInfo1 = new GroupInfo();
        groupInfo1.setCheckable(true);
        groupInfo1.setTitle("Setup as a new devices");

        GroupInfo groupInfo2 = new GroupInfo();
        groupInfo2.setCheckable(true);
        groupInfo2.setTitle("Restore from myTera");

        List<ChildInfo> childList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ChildInfo childInfo = new ChildInfo();
            childInfo.setModel("Nexus 5X -" + i);
            childInfo.setImei("123-456-789");
            childList.add(childInfo);
        }

        GroupInfo groupInfo3 = new GroupInfo();
        groupInfo3.setCheckable(false);
        groupInfo3.setTitle("Restore from backup");
        groupInfo3.setChildList(childList);

        groupList.add(groupInfo1);
//        groupList.add(groupInfo2);
        groupList.add(groupInfo3);

        mRestoreListAdapter = new RestoreListAdapter(groupList);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setAdapter(mRestoreListAdapter);
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ChildInfo childInfo = (ChildInfo) v.getTag();
                childInfo.setChecked(true);

                if (mPrevCheckableInfo != null) {
                    if (mPrevCheckableInfo != childInfo) {
                        boolean isChecked = mPrevCheckableInfo.isChecked();
                        mPrevCheckableInfo.setChecked(!isChecked);
                    }
                }
                mRestoreListAdapter.notifyDataSetChanged();
                mPrevCheckableInfo = childInfo;
                return true;
            }
        });
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                GroupInfo groupInfo = (GroupInfo) v.getTag();
                groupInfo.setChecked(true);
                if (groupInfo.isCheckable()) {
                    if (mPrevCheckableInfo != null) {
                        if (mPrevCheckableInfo != groupInfo) {
                            boolean isChecked = mPrevCheckableInfo.isChecked();
                            mPrevCheckableInfo.setChecked(!isChecked);
                        }
                    }
                    mRestoreListAdapter.notifyDataSetChanged();
                    mPrevCheckableInfo = groupInfo;
                }
                return false;
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrevCheckableInfo instanceof GroupInfo) {
                    String title = ((GroupInfo) mPrevCheckableInfo).getTitle();
                    Logs.d(CLASSNAME, "onClick", "title=" + title);
                } else { // ChildInfo
                    String model = ((ChildInfo) mPrevCheckableInfo).getModel();
                    Logs.d(CLASSNAME, "onClick", "model=" + model);
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.d(CLASSNAME, "onClick", "BackButton");
            }
        });
    }

    public class RestoreListAdapter extends BaseExpandableListAdapter {

        private List<GroupInfo> groupList;

        public RestoreListAdapter(List<GroupInfo> groupList) {
            this.groupList = groupList;
        }

        public List<GroupInfo> getGroupList() {
            return groupList;
        }

        public void setGroupList(List<GroupInfo> groupList) {
            this.groupList = groupList;
        }

        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            List<ChildInfo> childList = groupList.get(groupPosition).getChildList();
            if (childList != null) {
                return childList.size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groupList.get(groupPosition).getChildList().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return getGroup(groupPosition).hashCode();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition, childPosition).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupInfo groupInfo = (GroupInfo) getGroup(groupPosition);
            if (groupInfo.isCheckable()) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_radio_btn, null);
                } else {
                    if (convertView.findViewById(R.id.radio_btn) == null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_radio_btn, null);
                    }
                }
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(((GroupInfo) getGroup(groupPosition)).getTitle());

                ImageView radioBtn = (ImageView) convertView.findViewById(R.id.radio_btn);
                if (groupInfo.isChecked()) {
                    radioBtn.setImageResource(R.drawable.icon_btn_selected);
                } else {
                    radioBtn.setImageResource(R.drawable.icon_btn_unselected);
                }
            } else {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_dropdown_arrow, null);
                } else {
                    if (convertView.findViewById(R.id.radio_btn) != null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_dropdown_arrow, null);
                    }
                }
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(((GroupInfo) getGroup(groupPosition)).getTitle());
            }
            convertView.setTag(groupInfo);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildInfo childInfo = (ChildInfo) getChild(groupPosition, childPosition);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_child_item, null);
            }

            TextView model = (TextView) convertView.findViewById(R.id.model);
            model.setText(childInfo.getModel());

            TextView imei = (TextView) convertView.findViewById(R.id.imei);
            imei.setText(childInfo.getImei());


            ImageView radioBtn = (ImageView) convertView.findViewById(R.id.radio_btn);
            if (childInfo.isChecked()) {
                radioBtn.setImageResource(R.drawable.icon_btn_selected);
            } else {
                radioBtn.setImageResource(R.drawable.icon_btn_unselected);
            }

            convertView.setTag(childInfo);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private interface Checkable {

        boolean isChecked();

        void setChecked(boolean isChecked);

    }

    private class GroupInfo implements Checkable {

        private String title;

        private boolean isChecked;
        private boolean isCheckable;

        private List<ChildInfo> childList;

        public void setCheckable(boolean checkable) {
            isCheckable = checkable;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<ChildInfo> getChildList() {
            return childList;
        }

        public void setChildList(List<ChildInfo> childList) {
            this.childList = childList;
        }

        @Override
        public boolean isChecked() {
            return isChecked;
        }

        @Override
        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }

        public boolean isCheckable() {
            return isCheckable;
        }
    }

    public class ChildInfo implements Checkable {

        private String model;
        private String imei;

        private boolean isChecked;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        @Override
        public boolean isChecked() {
            return isChecked;
        }

        @Override
        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }

}
