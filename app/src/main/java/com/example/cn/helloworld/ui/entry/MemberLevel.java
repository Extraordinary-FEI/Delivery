package com.example.cn.helloworld.ui.entry;

public class MemberLevel {
    public final String title;
    public final String displayTitle;
    public final int nextTarget;

    private MemberLevel(String title, String displayTitle, int nextTarget) {
        this.title = title;
        this.displayTitle = displayTitle;
        this.nextTarget = nextTarget;
    }

    public static MemberLevel resolve(int points) {
        if (points < 200) {
            return new MemberLevel("晨曦会员", "晨曦会员 · Lv1", 200);
        }
        if (points < 600) {
            return new MemberLevel("星河会员", "星河会员 · Lv2", 600);
        }
        if (points < 1200) {
            return new MemberLevel("云耀会员", "云耀会员 · Lv3", 1200);
        }
        if (points < 2000) {
            return new MemberLevel("金曜会员", "金曜会员 · Lv4", 2000);
        }
        return new MemberLevel("曜钻会员", "曜钻会员 · Lv5", 3000);
    }
}
