package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;

import java.lang.reflect.Type;

/**
 * Created by rondou.chen on 2017/4/7.
 */

public class AppAuthUtils {

    public static final String AUTH_STATUS_PERF_NAME = "RondouIsDangerous";
    public static final String AUTH_STATUS_PERF_KEYS = "mAuthStateKey";

    private static class UriSerializer implements JsonSerializer<Uri> {
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(final JsonElement src, final Type srcType,
                               final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }

    public <GenericClass> GenericClass getSavedObjectFromPreference(Context context,
                                                                    String preferenceName,
                                                                    String preferenceKey,
                                                                    Class<GenericClass> classType) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Uri.class, new UriDeserializer())
                    .create();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }

    public static void saveObjectToSharedPreference(Context context, String preferenceName, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    public static void saveAppAuthStatusToSharedPreference(Context context, Object object) {
        saveObjectToSharedPreference(context, AUTH_STATUS_PERF_NAME, AUTH_STATUS_PERF_KEYS, object);
        Settings.Global.putInt(context.getContentResolver(),
                AppAuthUtils.AUTH_STATUS_PERF_KEYS, 1);
    }

    public AuthState getSavedAppAuthStatusFromPreference(Context context){
        return getSavedObjectFromPreference(context, AUTH_STATUS_PERF_NAME, AUTH_STATUS_PERF_KEYS, AuthState.class);
    }

    public void resetAccessTokeToHCFS(Context context, AuthState authstate) {
        authstate.performActionWithFreshTokens(new AuthorizationService(context), new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                new AsyncTask<String, Void, Bundle>() {
                    @Override
                    protected Bundle doInBackground(String... tokens) {
                        Log.d("Rondou", "wish have a new access token = " + tokens[0]);
                        HCFSMgmtUtils.setSwiftToken("https://172.16.40.117", tokens[0]);
                        return null;
                    }
                }.execute(accessToken);
            }
        });
    }
}
