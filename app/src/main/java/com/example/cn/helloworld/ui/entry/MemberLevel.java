package com.example.cn.helloworld.ui.entry;

public class MemberLevel {
    public final String title;
    public final int nextTarget;

    private MemberLevel(String title, int nextTarget) {
        this.title = title;
        this.nextTarget = nextTarget;
    }

    public static MemberLevel resolve(int points) {
        if (points < 200) {
            return new MemberLevel("晨曦会员", 200);
        }
        if (points < 600) {
            return new MemberLevel("星河会员", 600);
        }
        if (points < 1200) {
            return new MemberLevel("云耀会员", 1200);
        }
        if (points < 2000) {
            return new MemberLevel("金曜会员", 2000);
        }
        return new MemberLevel("曜钻会员", 3000);
    }
}
