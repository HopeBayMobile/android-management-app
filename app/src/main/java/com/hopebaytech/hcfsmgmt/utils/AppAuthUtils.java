package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by rondou.chen on 2017/4/7.
 */

public class AppAuthUtils {

    public final String AUTH_STATUS_PERF_NAME = "RondouIsDangerous";
    public final String AUTH_STATUS_PERF_KEYS = "mAuthStateKey";

    private class UriSerializer implements JsonSerializer<Uri> {
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

    public void saveObjectToSharedPreference(Context context, String preferenceName, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }
}
