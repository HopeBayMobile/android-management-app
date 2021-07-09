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
package com.hopebaytech.hcfsmgmt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hopebaytech.hcfsmgmt.service.TeraApiServer;

/**
 * @author Vince
 *      Created by Vince on 2016/9/5.
 */
public class TeraReceiver extends BroadcastReceiver {
    public static final String CREATE_THUMBNAIL_ACTION = "com.teraservice.create.thumbnail";
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent teraAPIServer = new Intent(context, TeraApiServer.class);
            context.startService(teraAPIServer);
        } else if (action.equals(CREATE_THUMBNAIL_ACTION)) {
            long id = intent.getLongExtra("id", -1);
            int type = intent.getIntExtra("type", 0);
            if ( id >= 0 ) {
                Intent teraAPIServer  = new Intent(context, TeraApiServer.class);
                teraAPIServer.setAction(CREATE_THUMBNAIL_ACTION);
                teraAPIServer.putExtra("id", id);
                teraAPIServer.putExtra("type", type);
                context.startService(teraAPIServer);
            }
        }
    }
}
