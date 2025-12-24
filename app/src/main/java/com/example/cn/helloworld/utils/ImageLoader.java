package com.example.cn.helloworld.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.cn.helloworld.R;

public final class ImageLoader {
    private ImageLoader() {
    }

    public static void load(Context context, ImageView imageView, String path) {
        if (TextUtils.isEmpty(path)) {
            imageView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        Glide.with(context)
                .load(path)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageView);
    }
}
