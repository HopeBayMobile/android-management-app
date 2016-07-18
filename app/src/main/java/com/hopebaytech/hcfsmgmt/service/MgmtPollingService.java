package com.hopebaytech.hcfsmgmt.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vince on 2016/7/14.
 */

public class MgmtPollingService extends Service {

    private final String CLASSNAME = getClass().getSimpleName();
    private boolean stopped = false;
    private String lockMsg;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Logs.i(CLASSNAME, this.getClass().getName(), "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int interval = intent.getIntExtra("interval", -1);
        Logs.i(CLASSNAME, this.getClass().getName(), String.valueOf(interval));

        new PollingThread(interval).start();

        //return super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        stopped = true;
        super.onDestroy();
    }

    class PollingThread extends Thread {
        private long interval;

        public PollingThread(int i) {
            this.interval = i;
        }

        @Override
        public void run() {
            Logs.d(CLASSNAME, this.getClass().getName(), "Start Polling ...");
            final Action action = new Action();

            while (!stopped) {
                try {
                    if (NetworkUtils.isNetworkConnected(MgmtPollingService.this)) {
                        MgmtCluster.getJwtToken(MgmtPollingService.this, new MgmtCluster.FetchJwtTokenListener() {
                            @Override
                            public void onFetchSuccessful(String jwt) {

                                String imei = HCFSMgmtUtils.getDeviceImei(MgmtPollingService.this);
                                try {
                                    JSONObject result = MgmtCluster.getMgmtCommands(jwt, imei);
                                    Logs.d(CLASSNAME, this.getClass().getName(), result.toString());

                                    JSONObject piggyback = result.getJSONObject("piggyback");
                                    String category = piggyback.getString("category");

                                    switch (category) {
                                        //Lock
                                        case "pb_001":
                                            action.lock(piggyback);
                                            break;

                                        // Reset
                                        case "pb_002":
                                            action.reset();
                                            break;

                                        // TXWaiting
                                        case "pb_003":
                                            break;

                                        // Unregistered
                                        case "pb_004":
                                            action.unregistered();
                                            break;
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFetchFailed() {
                                Logs.e(CLASSNAME, this.getClass().getName(), "Failed to get JWT");
                            }

                        });
                    }

                } catch (Exception e) {
                    Logs.e(CLASSNAME, this.getClass().getName(), e.toString());
                }

                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class Action {

        private void lock(JSONObject piggyback) {
            try {
                lockMsg = piggyback.getString("message");
                Logs.d(CLASSNAME, this.getClass().getName(), lockMsg);

                mHandler.sendEmptyMessage(0);

            } catch (JSONException e) {
                Logs.e(CLASSNAME, this.getClass().getName() + ".Lock", e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void reset() {

        }

        private void unregistered() {
            sendBroadcast(new Intent("hbt.intent.action.TRANSFER_COMPLETED"));
        }

    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Intent lockDeviceActivit = new Intent(MgmtPollingService.this, com.hopebaytech.hcfsmgmt.main.LockDeviceActivity.class);
                    lockDeviceActivit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putString("lockMsg", lockMsg);
                    lockDeviceActivit.putExtras(bundle);
                    startActivity(lockDeviceActivit);

                    break;

                default:

            }
        }
    };


}

