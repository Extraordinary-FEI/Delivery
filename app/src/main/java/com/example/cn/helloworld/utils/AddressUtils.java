package com.example.cn.helloworld.utils;

import com.example.cn.helloworld.data.model.Address;

public final class AddressUtils {
    private AddressUtils() {
    }

    public static String buildFullAddress(Address address) {
        if (address == null) {
            return "";
        }
        return safe(address.getProvince())
                + safe(address.getCity())
                + safe(address.getDistrict())
                + safe(address.getDetail());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
