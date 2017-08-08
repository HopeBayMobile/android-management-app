package com.hopebaytech.hcfsmgmt.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by rondou.chen on 2017/8/8.
 */

public class GoogleDriveAPI {
    private static final String GOOGLE_DRIVE_API_HOST_NAME_V2 = "https://www.googleapis.com/drive/v2/files";
    private String mToken = null;

    public GoogleDriveAPI(String token) {
        mToken = token;
    }

    public int get(String filename) {
        int items_size = 0;
        String url = String.format("%s%s\'%s\'", GOOGLE_DRIVE_API_HOST_NAME_V2, "?q=title+contains+" ,filename);

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", String.format("Bearer %s", mToken))
                    .build();
            Response response = new OkHttpClient().newCall(request).execute();
            String response_body = response.body().string();

            JSONObject file_list_info = new JSONObject(response_body);
            List<String> items = Arrays.asList(file_list_info.get("items").toString());
            Log.d("Rondou", file_list_info.get("items").toString());
            Log.d("Rondou", "item size = " + items.size());
            items_size = items.size();

        } catch (IOException e){
        } catch (JSONException e) {
        }

        return items_size;
    }
}
