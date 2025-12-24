package com.example.cn.helloworld.data;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;

import com.example.cn.helloworld.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AuthApiClient {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private AuthApiClient() {
    }

    public interface Callback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    public static final class LoginResponse {
        private final String token;
        private final String userId;
        private final String role;
        private final String username;

        public LoginResponse(String token, String userId, String role, String username) {
            this.token = token;
            this.userId = userId;
            this.role = role;
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public String getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }

        public String getUsername() {
            return username;
        }
    }

    public static void login(Context context, String username, String password, Callback<LoginResponse> callback) {
        String baseUrl = context.getString(R.string.api_base_url);
        String endpoint = baseUrl + "/api/auth/login";
        JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            payload.put("password", password);
        } catch (JSONException e) {
            callback.onError(context.getString(R.string.login_error_invalid));
            return;
        }
        EXECUTOR.execute(() -> executeAuthRequest(context, endpoint, payload, (response) -> {
            try {
                String token = response.optString("token", "");
                String userId = response.optString("userId", "");
                String role = response.optString("role", "");
                String displayName = response.optString("username", username);
                if (token.isEmpty() || role.isEmpty()) {
                    callback.onError(context.getString(R.string.login_error_invalid));
                    return;
                }
                callback.onSuccess(new LoginResponse(token, userId, role, displayName));
            } catch (Exception e) {
                callback.onError(context.getString(R.string.login_error_invalid));
            }
        }, () -> callback.onError(context.getString(R.string.login_error_network))));
    }

    public static void register(Context context, String username, String password, Callback<Void> callback) {
        String baseUrl = context.getString(R.string.api_base_url);
        String endpoint = baseUrl + "/api/auth/register";
        JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            payload.put("password", password);
        } catch (JSONException e) {
            callback.onError(context.getString(R.string.register_error_failed));
            return;
        }
        EXECUTOR.execute(() -> executeAuthRequest(context, endpoint, payload, (response) -> callback.onSuccess(null),
                () -> callback.onError(context.getString(R.string.register_error_network))));
    }

    private static void executeAuthRequest(Context context, String endpoint, JSONObject payload,
                                           ResponseHandler handler, Runnable onError) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        HttpURLConnection connection = null;
        try {
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body);
            }

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseText = readStream(stream);
            if (responseCode >= 200 && responseCode < 300) {
                JSONObject responseJson = responseText.isEmpty() ? new JSONObject() : new JSONObject(responseText);
                mainHandler.post(() -> handler.onResponse(responseJson));
            } else {
                mainHandler.post(onError);
            }
        } catch (IOException | JSONException e) {
            mainHandler.post(onError);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private interface ResponseHandler {
        void onResponse(JSONObject response);
    }
}
