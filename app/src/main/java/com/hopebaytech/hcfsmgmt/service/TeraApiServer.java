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
package com.hopebaytech.hcfsmgmt.service;

import android.app.Service;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.IBinder;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.receiver.TeraReceiver;
import com.hopebaytech.hcfsmgmt.utils.HCFSEvent;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;
import com.hopebaytech.hcfsmgmt.utils.ThumbnailApiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vince
 *      Created by Vince on 2016/9/5.
 */
public class TeraApiServer extends Service {

    private final String CLASSNAME = getClass().getSimpleName();
    public static String SOCKET_ADDRESS = "mgmt.api.sock";
    private boolean stopped = false;
    private ExecutorService pool = Executors.newFixedThreadPool(5);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.d(CLASSNAME, "onStartCommand", null);
        String action = null;
        if (intent != null)
            action = intent.getAction();

        if (action!=null && action.equals(TeraReceiver.CREATE_THUMBNAIL_ACTION)) {
            long id = intent.getLongExtra("id", -1);
            int type = intent.getIntExtra("type", 0);
            if (id >= 0)
                new ThumbnailApiUtils().getThumbnail(TeraApiServer.this.getApplicationContext(), id, type);
        } else {
            new SocketListener().start();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopped = true;
        pool.shutdown();
        super.onDestroy();
    }

    class SocketListener extends Thread {
        @Override
        public void run() {
            Logs.d(CLASSNAME, "run", "Server socket run . . . start");
            LocalServerSocket server = null;
            try {
                server = new LocalServerSocket(SOCKET_ADDRESS);
                while (!stopped) {
                    try {
                        LocalSocket receiver = server.accept();
                        if (receiver != null) {
                            InputStream inputStream = receiver.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                            String inputLine = bufferedReader.readLine();
                            Log.d(CLASSNAME, inputLine);
                            try {
                                JSONArray jsonArray = new JSONArray(inputLine);
                                receiver.getOutputStream().write(1);
                                pool.execute(new Thread(new MgmtApiUtils(jsonArray)));
                            } catch (JSONException e) {
                                Log.e(CLASSNAME, Log.getStackTraceString(e));
                                receiver.getOutputStream().write(0);
                            }

                            bufferedReader.close();
                            receiver.close();
                        }
                    } catch (Exception e) {
                        Logs.e(CLASSNAME, "run", Log.getStackTraceString(e));
                    }
                }
            } catch (IOException e) {
                Log.e(CLASSNAME, Log.getStackTraceString(e));
            } finally {
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        Log.e(CLASSNAME, Log.getStackTraceString(e));
                    }
                }
            }
            Logs.d(CLASSNAME, "run", "Server socket run . . . end");
        }
    }

    class MgmtApiUtils implements Runnable {
        private JSONArray jsonArray;
        public MgmtApiUtils(JSONArray j) {
            this.jsonArray = j;
        }
        @Override
        public void run() {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    int eventID = jsonObj.getInt("event_id");
                    Logs.w(CLASSNAME, "run", "eventID = " + eventID);
                    switch (eventID) {
                        case HCFSEvent.TEST:
                            Logs.d(CLASSNAME, CLASSNAME + ".test", jsonObj.toString());
                            break;
                        case HCFSEvent.TOKEN_EXPIRED:
                            checkTokenExpiredCause();
                            break;
                        case HCFSEvent.UPLOAD_COMPLETED:
                            sendUploadCompletedIntent();
                            break;
                        case HCFSEvent.RESTORE_STAGE_1:
                            sendRestoreStage1Intent(jsonObj);
                            break;
                        case HCFSEvent.RESTORE_STAGE_2:
                            sendRestoreStage2Intent(jsonObj);
                            break;
                        case HCFSEvent.EXCEED_PIN_MAX:
                            notifyUserExceedPinMax();
                            break;
                        case HCFSEvent.CREATE_THUMBNAIL:
                            new ThumbnailApiUtils().createThumbnailImages(TeraApiServer.this.getApplicationContext(), jsonObj);
                            break;
                        case HCFSEvent.BOOSTER_PROCESS_COMPLETED:
                            sendBoosterProcessCompletedIntent();
                            break;
                        case HCFSEvent.BOOSTER_PROCESS_FAILED:
                            sendBoosterProcessFailedIntent();
                            break;

                    }
                }
            } catch (Exception e) {
                Logs.e(CLASSNAME, "run", Log.getStackTraceString(e));
            }
        }

        private void notifyUserExceedPinMax() {
            Intent intent = new Intent();
            intent.setAction(TeraIntent.ACTION_EXCEED_PIN_MAX);
            sendBroadcast(intent);
        }

        private void checkTokenExpiredCause() {
            Intent intent = new Intent();
            intent.setAction(TeraIntent.ACTION_TOKEN_EXPIRED);
            sendBroadcast(intent);
        }

        private void sendUploadCompletedIntent() {
            Intent intent = new Intent();
            intent.setAction(TeraIntent.ACTION_UPLOAD_COMPLETED);
            sendBroadcast(intent);
        }

        private void sendRestoreStage1Intent(JSONObject jsonObj) {
            int errorCode = -1;
            try {
                errorCode = jsonObj.getInt("result");
            } catch (JSONException e) {
                Logs.w(CLASSNAME, "sendRestoreStage1Intent", "jsonObj=" + jsonObj.toString());
            }

            Intent intent = new Intent();
            intent.setAction(TeraIntent.ACTION_RESTORE_STAGE_1);
            if (errorCode != -1) {
                intent.putExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, errorCode);
            }
            sendBroadcast(intent);
        }

        private void sendRestoreStage2Intent(JSONObject jsonObj) {
            int errorCode = -1;
            try {
                errorCode = jsonObj.getInt("result");
            } catch (JSONException e) {
                Logs.w(CLASSNAME, "sendRestoreStage1Intent", "jsonObj=" + jsonObj.toString());
            }

            Intent intent = new Intent();
            intent.setAction(TeraIntent.ACTION_RESTORE_STAGE_2);
            if (errorCode != -1) {
                intent.putExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, errorCode);
            }
            sendBroadcast(intent);
        }

        private void sendBoosterProcessCompletedIntent() {
            Intent intent = new Intent();
            intent.setAction(TeraIntent.ACTION_BOOSTER_PROCESS_COMPLETED);
            sendBroadcast(intent);
        }

        private void sendBoosterProcessFailedIntent() {
            Intent intent = new Intent();
            intent.setAction(TeraIntent.ACTION_BOOSTER_PROCESS_FAILED);
            sendBroadcast(intent);
        }

    }

}
