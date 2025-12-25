package com.example.cn.helloworld.data.model;

public class Address {
    private final int id;
    private final int userId;
    private final String contactName;
    private final String contactPhone;
    private final String detail;
    private final boolean isDefault;
    private final long createdAt;

    public Address(int id, int userId, String contactName, String contactPhone, String detail,
                   boolean isDefault, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
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
