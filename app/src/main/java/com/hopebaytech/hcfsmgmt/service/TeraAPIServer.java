package com.hopebaytech.hcfsmgmt.service;

import android.app.Service;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.IBinder;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.info.HCFSEventInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

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
 *         Created by Vince on 2016/7/4.
 */

public class TeraAPIServer extends Service {

    private final String CLASSNAME = getClass().getSimpleName();
    public static String SOCKET_ADDRESS = "mgmt.api.sock";
    private boolean stopped = false;
    private ExecutorService pool = Executors.newFixedThreadPool(3);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.d(CLASSNAME, this.getClass().getName(), "onStartCommand");

        new SocketListener().start();

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        Thread.sleep(500);
                        String jsonResult = HCFSApiUtils.setNotifyServer(SOCKET_ADDRESS);
                        boolean isSuccess = new JSONObject(jsonResult).getBoolean("result");
                        if (isSuccess) {
                            break;
                        } else {
                            Logs.e(CLASSNAME, this.getClass().getName(), jsonResult);
                        }
                    }
                } catch(Exception e){
                    Logs.e(CLASSNAME, this.getClass().getName(), e.toString());
                }
            }
        }).start();

        return START_REDELIVER_INTENT;
        //return START_STICKY;
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

    private void writeSocket(int result) throws IOException {
        Logs.i(CLASSNAME, "writeSocket", String.valueOf(result));
        LocalSocket sender = new LocalSocket();
        sender.connect(new LocalSocketAddress(SOCKET_ADDRESS));
        sender.getOutputStream().write(result);
        sender.getOutputStream().close();
        sender.close();
    }

    class SocketListener extends Thread {
        @Override
        public void run() {
            Logs.d(CLASSNAME, this.getClass().getName(), "Server socket run . . . start");
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

                            String inputLine = null;
                            inputLine = bufferedReader.readLine();
                            Logs.d(CLASSNAME, this.getClass().getName(), inputLine);
                            try {
                                JSONArray jsonArray = new JSONArray(inputLine);
                                receiver.getOutputStream().write(1);
                                pool.execute(new Thread(new mgmtApiUtils(jsonArray)));

                            } catch (JSONException e) {
                                Logs.e(CLASSNAME, this.getClass().getName(), e.toString());
                                receiver.getOutputStream().write(0);
                            }

                            bufferedReader.close();
                            receiver.close();
                        }
                    } catch (Exception e) {
                        Logs.e(CLASSNAME, this.getClass().getName(), e.getMessage());
                    }
                }
            } catch (IOException e) {
                Logs.e(CLASSNAME, this.getClass().getName(), e.getMessage());

            } finally {
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        Log.e(getClass().getName(), e.getMessage());
                    }
                }
            }

            Logs.d(CLASSNAME, this.getClass().getName(), "Server socket run . . . end");
        }
    }

    class mgmtApiUtils implements Runnable {

        JSONArray jsonArray;
        public mgmtApiUtils(JSONArray j) {
            this.jsonArray = j;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    int eventID = jsonObj.getInt("event_id");

                    switch (eventID) {
                        case HCFSEventInfo.TEST:
                            Logs.d(CLASSNAME, this.getClass().getName() + ".test", jsonObj.toString());
                            break;
                    }
                }

            } catch (Exception e) {
                Logs.e(CLASSNAME, this.getClass().getName(), e.toString());
            }

        }
    }

}
