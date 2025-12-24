package com.example.cn.helloworld.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.cn.helloworld.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AuthApiClient {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private AuthApiClient() { }

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

        public String getToken() { return token; }
        public String getUserId() { return userId; }
        public String getRole() { return role; }
        public String getUsername() { return username; }
    }

    // ✅ 拼接 URL：避免 baseUrl 末尾带 / 导致双斜杠
    private static String joinUrl(String baseUrl, String path) {
        if (baseUrl == null) baseUrl = "";
        if (path == null) path = "";
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        if (!path.startsWith("/")) path = "/" + path;
        return baseUrl + path;
    }

    public static void login(final Context context, final String username, final String password,
                             final String role,
                             final Callback<LoginResponse> callback) {

        String baseUrl = context.getString(R.string.api_base_url);

        // ✅ 改成 .php
        final String endpoint = joinUrl(baseUrl, "/api/auth/login.php");

        final JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            payload.put("password", password);
            payload.put("role", role);
        } catch (JSONException e) {
            callback.onError(context.getString(R.string.login_error_invalid));
            return;
        }

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                executeAuthRequest(context, endpoint, payload, new ResponseHandler() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.optString("token", "");
                            String userId = response.optString("userId", "");
                            String role = response.optString("role", "");

                            // ✅ 兼容你当前 PHP 没返回 role 的情况：给一个默认值
                            if (role == null || role.trim().isEmpty()) {
                                role = "user";
                            }

                            String displayName = response.optString("username", username);

                            if (token == null || token.trim().isEmpty()) {
                                callback.onError(context.getString(R.string.login_error_invalid));
                                return;
                            }

                            callback.onSuccess(new LoginResponse(token, userId, role, displayName));
                        } catch (Exception e) {
                            callback.onError(context.getString(R.string.login_error_invalid));
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(context.getString(R.string.login_error_network));
                    }
                });
            }
        });
    }

    public static void register(final Context context, final String username, final String password,
                                final Callback<Void> callback) {

        String baseUrl = context.getString(R.string.api_base_url);

        // ✅ 改成 .php
        final String endpoint = joinUrl(baseUrl, "/api/auth/register.php");

        final JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            payload.put("password", password);
        } catch (JSONException e) {
            callback.onError(context.getString(R.string.register_error_failed));
            return;
        }

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                executeAuthRequest(context, endpoint, payload, new ResponseHandler() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(null);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(context.getString(R.string.register_error_network));
                    }
                });
            }
        });
    }

    private static void executeAuthRequest(Context context, String endpoint, JSONObject payload,
                                           final ResponseHandler handler, final Runnable onError) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        HttpURLConnection connection = null;

        try {
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            byte[] body = payload.toString().getBytes("UTF-8");
            OutputStream outputStream = null;
            try {
                outputStream = connection.getOutputStream();
                outputStream.write(body);
            } finally {
                closeQuietly(outputStream);
            }

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String responseText = readStream(stream);

            if (responseCode >= 200 && responseCode < 300) {
                final JSONObject responseJson = responseText.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseText);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handler.onResponse(responseJson);
                    }
                });
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

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } finally {
            closeQuietly(reader);
        }
        return builder.toString();
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignored) { }
    }

    private interface ResponseHandler {
        void onResponse(JSONObject response);
    }
}
