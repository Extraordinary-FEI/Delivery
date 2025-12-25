package com.example.cn.helloworld.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.example.cn.helloworld.R;

public final class AvatarLoader {
    private static final String RESOURCE_PREFIX = "res:";

    private AvatarLoader() {
    }

    public static void load(Context context, ImageView imageView, String avatar) {
        if (TextUtils.isEmpty(avatar)) {
            imageView.setImageResource(R.drawable.ic_launcher);
            return;
        }
        String trimmed = avatar.trim();
        if (trimmed.startsWith(RESOURCE_PREFIX)) {
            imageView.setImageResource(mapAvatarRes(trimmed.substring(RESOURCE_PREFIX.length())));
            return;
        }
        ImageLoader.loadAvatar(context, imageView, trimmed);
    }

    private static int mapAvatarRes(String value) {
        int index = 0;
        try {
            index = Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            index = 0;
        }
        switch (index) {
            case 1:
            case 2:
            case 3:
                return R.drawable.ic_launcher;
            default:
                return R.drawable.ic_launcher;
        }
    }
}
