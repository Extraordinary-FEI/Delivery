package com.example.cn.helloworld.data;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class LocalJsonStore {
    private static final String DATA_DIR = "data";

    private LocalJsonStore() {
    }

    public static File ensureLocalFile(Context context, String filename) throws IOException {
        File dataDir = new File(context.getFilesDir(), DATA_DIR);
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new IOException("Failed to create data directory");
        }
        File targetFile = new File(dataDir, filename);
        if (!targetFile.exists()) {
            copyFromAssets(context, filename, targetFile);
        }
        return targetFile;
    }

    public static String readJson(Context context, String filename) throws IOException {
        File file = ensureLocalFile(context, filename);
        InputStream inputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    public static void writeJson(Context context, String filename, String json) throws IOException {
        File file = ensureLocalFile(context, filename);
        FileOutputStream outputStream = new FileOutputStream(file, false);
        outputStream.write(json.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    private static void copyFromAssets(Context context, String filename, File targetFile) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(filename);
        FileOutputStream outputStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[4096];
        int count;
        while ((count = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, count);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
}

