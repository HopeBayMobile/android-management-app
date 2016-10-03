package com.hopebaytech.hcfsmgmt.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.GetDeviceInfo;
import com.hopebaytech.hcfsmgmt.info.TeraIntent;
import com.hopebaytech.hcfsmgmt.utils.FactoryResetUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.MgmtPollingUtils;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Vince
 *         Created by Vince on 2016/7/14.
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
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int interval = intent.getIntExtra(MgmtPollingUtils.KEY_INTERVAL, -1);
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

    public void stopPollingService() {
        stopSelf();
    }

    private class PollingThread extends Thread {

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
                        MgmtCluster.getJwtToken(MgmtPollingService.this, new MgmtCluster.OnFetchJwtTokenListener() {
                            @Override
                            public void onFetchSuccessful(String jwt) {
                                String imei = HCFSMgmtUtils.getDeviceImei(MgmtPollingService.this);
                                MgmtCluster.GetDeviceServiceInfoProxy proxy = new MgmtCluster.GetDeviceServiceInfoProxy(jwt, imei);
                                proxy.setOnGetDeviceServiceInfoListener(new MgmtCluster.
                                        GetDeviceServiceInfoProxy.OnGetDeviceServiceInfoListener() {
                                    @Override
                                    public void onGetDeviceServiceInfoSuccessful(DeviceServiceInfo deviceServiceInfo) {
                                        DeviceServiceInfo.Piggyback piggyback = deviceServiceInfo.getPiggyback();
                                        String category = piggyback.getCategory();
                                        switch (category) {
                                            case GetDeviceInfo.Category.LOCK:
                                                action.lock(piggyback.getMessage());
                                                break;
                                            case GetDeviceInfo.Category.RESET:
                                                action.reset();
                                                break;
                                            case GetDeviceInfo.Category.TX_WAITING:
                                                break;
                                            case GetDeviceInfo.Category.UNREGISTERED:
                                                action.unregistered();
                                                break;
                                            default:
                                                if (deviceServiceInfo.getState().equals(GetDeviceInfo.State.ACTIVATED)) {
                                                    stopped = true;
                                                    stopPollingService();
                                                }
                                        }
                                    }

                                    @Override
                                    public void onGetDeviceServiceInfoFailed(DeviceServiceInfo deviceServiceInfo) {
                                        Logs.e(CLASSNAME, "onGetDeviceServiceInfoFailed", null);
                                    }
                                });
                                proxy.get();
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
                    Logs.e(CLASSNAME, "onGetDeviceInfoFailed", Log.getStackTraceString(e));
                }
            }

        }
    }

    private class Action {

        private void lock(String message) {
            Logs.d(CLASSNAME, "Action", "lock", "message=" + message);
            lockMsg = message;
            mHandler.sendEmptyMessage(0);
        }

        private void reset() {
            FactoryResetUtils.reset(MgmtPollingService.this);
        }

        private void unregistered() {
            sendBroadcast(new Intent(TeraIntent.ACTION_TRANSFER_COMPLETED));
        }

    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Intent lockDeviceActivity = new Intent(MgmtPollingService.this, com.hopebaytech.hcfsmgmt.main.LockDeviceActivity.class);
                    lockDeviceActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putString("lockMsg", lockMsg);
                    lockDeviceActivity.putExtras(bundle);
                    startActivity(lockDeviceActivity);
                    break;
                default:
            }
        }
    };

}

