package com.example.cn.helloworld.data.model;

public class Address {
    private final int id;
    private final int userId;
    private final String contactName;
    private final String contactPhone;
    private final String province;
    private final String city;
    private final String district;
    private final String detail;
    private final boolean isDefault;
    private final long createdAt;

    public Address(int id, int userId, String contactName, String contactPhone,
                   String province, String city, String district, String detail,
                   boolean isDefault, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
