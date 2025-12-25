package com.example.cn.helloworld.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cn.helloworld.R;

public final class ImageLoader {
    private static final String PEXELS_IMAGE_HOST = "https://images.pexels.com/photos/";
    private static final String PEXELS_IMAGE_QUERY = "?auto=compress&cs=tinysrgb&dpr=1&w=800";

    private ImageLoader() {
    }

    public static void load(Context context, ImageView imageView, String path) {
        if (TextUtils.isEmpty(path)) {
            imageView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        String trimmed = path.trim();
        String pexelsId = extractPexelsId(trimmed);
        RequestBuilder<?> requestBuilder;
        if (!TextUtils.isEmpty(pexelsId)) {
            String primaryUrl = buildPexelsUrl(pexelsId, "jpeg");
            String fallbackUrl = buildPexelsUrl(pexelsId, "jpg");
            requestBuilder = Glide.with(context)
                    .load(primaryUrl)
                    .error(Glide.with(context).load(fallbackUrl));
        } else {
            requestBuilder = Glide.with(context).load(trimmed);
        }
        requestBuilder
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageView);
    }

    private static String buildPexelsUrl(String pexelsId, String extension) {
        return PEXELS_IMAGE_HOST + pexelsId + "/pexels-photo-" + pexelsId + "." + extension + PEXELS_IMAGE_QUERY;
    }

    private static String extractPexelsId(String url) {
        int photoIndex = url.indexOf("/photo/");
        if (photoIndex == -1) {
            return null;
        }
        int start = photoIndex + "/photo/".length();
        int end = start;
        while (end < url.length()) {
            char ch = url.charAt(end);
            if (ch < '0' || ch > '9') {
                break;
            }
            end++;
        }
        if (end > start) {
            return url.substring(start, end);
        }
        int lastDash = url.lastIndexOf('-', url.length() - 1);
        if (lastDash == -1 || lastDash + 1 >= url.length()) {
            return null;
        }
        int idEnd = url.indexOf('/', lastDash);
        if (idEnd == -1) {
            idEnd = url.length();
        }
        String candidate = url.substring(lastDash + 1, idEnd);
        if (TextUtils.isDigitsOnly(candidate)) {
            return candidate;
        }
        return null;
    }
}
