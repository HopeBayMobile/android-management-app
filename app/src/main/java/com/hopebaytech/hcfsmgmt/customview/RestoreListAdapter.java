package com.hopebaytech.hcfsmgmt.customview;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.hopebaytech.hcfsmgmt.R;

import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;

import java.util.List;

public class RestoreListAdapter extends BaseAdapter{

    private LayoutInflater mLayoutInflater;
    List<ListItem> listItems;

    public RestoreListAdapter(Context context, List<ListItem> listItems) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems == null? 0 : listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.restore_child_item, parent, false);

        TextView model = (TextView) view.findViewById(R.id.model);
        model.setText(((ListItem)getItem(position)).getModel());

        TextView imei = (TextView) view.findViewById(R.id.imei);
        imei.setText(((ListItem)getItem(position)).getImei());

        ImageView radioBtn = (ImageView) view.findViewById(R.id.radio_btn);
        radioBtn.setImageResource(((ListItem)getItem(position)).isSelected ? R.drawable.icon_btn_selected : R.drawable.icon_btn_unselected);
        radioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListItem)getItem(position)).setSelected(!((ListItem)getItem(position)).isSelected); // TODO: move to view
            }
        });

        //convertView.setTag(childInfo);
        return view;
    }

    public List<ListItem> getListItems() {
        return listItems;
    }

    public int getNumOfSelectedItems() {
        int count = 0;
        for(ListItem item : listItems) {
            if(item.isSelected()) {
                count++;
            }
        }
        return count;
    }

    public ListItem getSelectedItem() {
        for(ListItem listItem : listItems) {
            if(listItem.isSelected()) {
                return listItem;
            }
        }

        return null;
    }

    public static class ListItem {

        private boolean isSelected;
        private DeviceStatusInfo deviceStatusInfo;

        public ListItem(DeviceStatusInfo deviceStatusInfo) {
            this.isSelected = false;
            this.deviceStatusInfo = deviceStatusInfo;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getModel() {
            return deviceStatusInfo.getModel();
        }

        public String getImei() {
            return deviceStatusInfo.getImei();
        }
    }
}
