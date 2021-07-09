/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        TextView containerIndex = (TextView) view.findViewById(R.id.containerIndex);
        containerIndex.setText((((ListItem)getItem(position)).getContainerIndex()));

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

        public String getContainerIndex() {
            return deviceStatusInfo.getContainerIndex();
        }
    }
}
