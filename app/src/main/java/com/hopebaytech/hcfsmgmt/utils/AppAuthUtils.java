package com.hopebaytech.hcfsmgmt.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;

import java.lang.reflect.Type;

/**
 * Created by rondou.chen on 2017/4/7.
 */

public class AppAuthUtils {

    public static final String AUTH_STATUS_PERF_NAME = "RondouIsDangerous";
    public static final String AUTH_STATUS_PERF_KEYS = "mAuthStateKey";

    private static int OPERATING_MODE = Context.MODE_PRIVATE;

    private static class UriSerializer implements JsonSerializer<Uri> {
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(
                final JsonElement src, final Type srcType,
                final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }

    public static <GenericClass> GenericClass getSavedObjectFromPreference(
            Context context, String preferenceName, String preferenceKey,
            Class<GenericClass> classType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                preferenceName, OPERATING_MODE);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Uri.class, new UriDeserializer())
                    .create();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }

    public static void saveObjectToSharedPreference(
            Context context, String preferenceName, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                preferenceName, OPERATING_MODE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.commit();
    }

    public static boolean saveAppAuthStatusToSharedPreference(Context context, Object object) {
        saveObjectToSharedPreference(context, AUTH_STATUS_PERF_NAME, AUTH_STATUS_PERF_KEYS, object);
        return Settings.Global.putInt(context.getContentResolver(), AppAuthUtils.AUTH_STATUS_PERF_KEYS, 1);
    }

    public static AuthState getSavedAppAuthStatusFromPreference(Context context){
        return getSavedObjectFromPreference(context, AUTH_STATUS_PERF_NAME, AUTH_STATUS_PERF_KEYS, AuthState.class);
    }

    public static void refreshAccessToken(final Context context) {
        final AuthState authState = getSavedAppAuthStatusFromPreference(context);
        if (authState != null) {
            authState.setNeedsTokenRefresh(true);
            authState.performActionWithFreshTokens(new AuthorizationService(context),
                    new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken,
                                            @Nullable String idToken,
                                            @Nullable AuthorizationException exception) {
                            if (exception != null) {
                                Logs.d("refresh tokens failed : " + exception);
                                return;
                            }

                            Log.d("Rondou", "wish have a new access token = " + accessToken);
                            HCFSMgmtUtils.setSwiftToken("https://127.0.0.1", accessToken);
                            saveAppAuthStatusToSharedPreference(context, authState);
                        }
                    });
        }
    }

    public static void appAuthorization(Context context) {
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse(GoogleDriveAPI.GOOGLE_ENDPOINT_AUTH), /* auth endpoint */
                Uri.parse(GoogleDriveAPI.GOOGLE_ENDPOINT_TOKEN) /* token endpoint */
        );

        AuthorizationService authorizationService = new AuthorizationService(context);
        Uri redirectUri = Uri.parse("com.hopebaytech.hcfsmgmt:/oauth2callback");

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                GoogleDriveAPI.GOOGLE_CLIENT_ID,
                AuthorizationRequest.RESPONSE_TYPE_CODE,
                redirectUri
        );
        builder.setScopes("profile",
                "email",
                "https://www.googleapis.com/auth/drive",
                "https://www.googleapis.com/auth/drive.file",
                "https://www.googleapis.com/auth/drive.appdata");

        AuthorizationRequest request = builder.build();
        Intent postAuthorizationIntent = new Intent(GoogleDriveAPI.ACTION_AUTHORIZATION_RESPONSE);
        postAuthorizationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, request.hashCode(), postAuthorizationIntent, 0);
        authorizationService.performAuthorizationRequest(request, pendingIntent);
    }
}
